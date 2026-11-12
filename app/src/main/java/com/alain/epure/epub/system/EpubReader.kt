package com.alain.epure.epub.system

import android.util.Xml
import com.alain.epure.epub.domain.ArchiveLimits
import com.alain.epure.epub.domain.ArchivePolicy
import com.alain.epure.epub.domain.BookPackage
import com.alain.epure.epub.domain.BookRefusal
import com.alain.epure.epub.domain.PackageBuilder
import com.alain.epure.epub.domain.RawTocEntry
import com.alain.epure.epub.domain.RefusalReason
import com.alain.epure.epub.domain.TableOfContents
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.io.StringReader
import java.util.Collections
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile

/** An archive that has passed the checks of S-4, with its reading order. */
class OpenBook(
    private val zip: ZipFile,
    val book: BookPackage,
    /** True when the `mimetype` entry is missing, misplaced, compressed or wrong (DEC-EE-0017). */
    val mimetypeWarning: Boolean,
) : Closeable {

    /** Reads one entry of the archive. Returns null if it does not exist or its name is unsafe. */
    fun readEntry(path: String): ByteArray? = EpubReader.readEntry(zip, path)

    override fun close() {
        zip.close()
    }
}

/**
 * Opens an EPUB copy: validates the archive (S-4), then reads its package document (S-5).
 * Runs outside the main thread: callers must not call it from the UI thread.
 */
object EpubReader {

    private val limits = ArchiveLimits()
    private const val MIMETYPE = "mimetype"
    private const val MIMETYPE_VALUE = "application/epub+zip"
    private const val CONTAINER = "META-INF/container.xml"
    private const val NCX_TYPE = "application/x-dtbncx+xml"

    fun open(file: File): OpenBook {
        val zip = try {
            ZipFile(file)
        } catch (e: ZipException) {
            // Android's ZIP reader refuses unsafe paths when the archive is opened.
            throw BookRefusal(refusalFor(e))
        } catch (e: IOException) {
            throw BookRefusal(RefusalReason.NOT_ARCHIVE)
        }
        try {
            val entries = validate(zip)
            val mimetypeWarning = !hasValidMimetype(zip, entries.firstOrNull()?.name)
            val opfPath = readContainer(zip)
            val opfBytes = readEntry(zip, opfPath) ?: throw BookRefusal(RefusalReason.NO_PACKAGE)
            val opf = try {
                parseOpf(opfBytes)
            } catch (e: XmlPullParserException) {
                throw BookRefusal(RefusalReason.NO_PACKAGE)
            }
            val book = PackageBuilder.build(
                opfPath = opfPath,
                title = opf.title,
                manifest = opf.manifest,
                spineIds = opf.spine,
                entryNames = entries.map { it.name }.toSet(),
            )
            // The table of contents is optional: a missing or unreadable one gives an empty table (F-12.2).
            val toc = readToc(zip, opf, opfPath, book.spine)
            return OpenBook(zip, book.copy(toc = toc), mimetypeWarning)
        } catch (e: Throwable) {
            zip.close()
            throw e
        }
    }

    /** Checks names and sizes of every entry before anything is read. */
    private fun validate(zip: ZipFile): List<ZipEntry> {
        val entries = try {
            Collections.list(zip.entries())
        } catch (e: ZipException) {
            throw BookRefusal(refusalFor(e))
        }
        ArchivePolicy.checkEntryCount(entries.size, limits)?.let { throw BookRefusal(it) }
        var total = 0L
        for (entry in entries) {
            if (!ArchivePolicy.isSafeEntryName(entry.name)) throw BookRefusal(RefusalReason.UNSAFE_PATH)
            if (entry.isDirectory) continue
            if (entry.size < 0L || entry.compressedSize < 0L) throw BookRefusal(RefusalReason.NOT_ARCHIVE)
            ArchivePolicy.checkEntrySize(entry.size, limits)?.let { throw BookRefusal(it) }
            ArchivePolicy.checkCompressionRatio(entry.compressedSize, entry.size, limits)?.let { throw BookRefusal(it) }
            total += entry.size
        }
        ArchivePolicy.checkTotalSize(total, limits)?.let { throw BookRefusal(it) }
        return entries
    }

    private fun refusalFor(e: ZipException): RefusalReason =
        if (e.message?.contains("path", ignoreCase = true) == true) RefusalReason.UNSAFE_PATH
        else RefusalReason.NOT_ARCHIVE

    private fun hasValidMimetype(zip: ZipFile, firstName: String?): Boolean {
        val entry = zip.getEntry(MIMETYPE) ?: return false
        if (firstName != MIMETYPE || entry.method != ZipEntry.STORED) return false
        val value = readEntry(zip, MIMETYPE)?.toString(Charsets.UTF_8)?.trim()
        return value == MIMETYPE_VALUE
    }

    /** Reads an entry of the archive, bounded by the size already checked in [validate]. */
    internal fun readEntry(zip: ZipFile, path: String): ByteArray? {
        if (!ArchivePolicy.isSafeEntryName(path)) return null
        val entry = zip.getEntry(path) ?: return null
        return zip.getInputStream(entry).use { it.readBytes() }
    }

    private fun readContainer(zip: ZipFile): String {
        val bytes = readEntry(zip, CONTAINER) ?: throw BookRefusal(RefusalReason.NO_PACKAGE)
        val path = try {
            parseContainer(bytes)
        } catch (e: XmlPullParserException) {
            throw BookRefusal(RefusalReason.NO_PACKAGE)
        } ?: throw BookRefusal(RefusalReason.NO_PACKAGE)
        if (!ArchivePolicy.isSafeEntryName(path)) throw BookRefusal(RefusalReason.UNSAFE_PATH)
        return path
    }

    // S-5: the platform XML parser does not fetch DTDs or external entities. A document that
    // refers to an undeclared entity makes the parser fail, which is reported as a missing package.

    private fun parseContainer(bytes: ByteArray): String? {
        val parser = newParser(bytes)
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG && parser.name == "rootfile") {
                parser.getAttributeValue(null, "full-path")?.takeIf { it.isNotEmpty() }?.let { return it }
            }
            event = parser.next()
        }
        return null
    }

    private class Opf(
        val title: String?,
        val manifest: Map<String, String>,
        val spine: List<String>,
        /** Manifest id of the EPUB 3 navigation document, if any. */
        val navId: String?,
        /** Manifest id of the EPUB 2 NCX document, if any. */
        val ncxId: String?,
    )

    private fun parseOpf(bytes: ByteArray): Opf {
        val parser = newParser(bytes)
        val manifest = HashMap<String, String>()
        val spine = ArrayList<String>()
        var navId: String? = null
        var ncxId: String? = null
        var spineToc: String? = null
        var title: String? = null
        var inTitle = false
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> when (parser.name) {
                    "dc:title", "title" -> if (title == null) inTitle = true
                    "item" -> {
                        val id = parser.getAttributeValue(null, "id")
                        val href = parser.getAttributeValue(null, "href")
                        if (id != null && href != null) manifest[id] = href
                        if (id != null && parser.getAttributeValue(null, "properties")?.split(' ')?.contains("nav") == true) {
                            navId = id
                        }
                        if (id != null && parser.getAttributeValue(null, "media-type") == NCX_TYPE) ncxId = id
                    }
                    "spine" -> spineToc = parser.getAttributeValue(null, "toc")
                    "itemref" -> parser.getAttributeValue(null, "idref")?.let { spine.add(it) }
                }
                XmlPullParser.TEXT -> if (inTitle) {
                    title = (title ?: "") + parser.text
                }
                XmlPullParser.END_TAG -> if (parser.name == "dc:title" || parser.name == "title") {
                    inTitle = false
                }
            }
            event = parser.next()
        }
        return Opf(
            title = title?.trim()?.takeIf { it.isNotEmpty() },
            manifest = manifest,
            spine = spine,
            navId = navId,
            ncxId = spineToc ?: ncxId,
        )
    }

    /**
     * Reads the table of contents: the EPUB 3 navigation document if there is one, else the EPUB 2 NCX.
     * Returns an empty list if neither can be read. Errors in the table never stop the book from opening.
     */
    private fun readToc(zip: ZipFile, opf: Opf, opfPath: String, spine: List<String>): List<com.alain.epure.epub.domain.TocEntry> {
        val opfDir = opfPath.substringBeforeLast('/', "")
        return try {
            val navHref = opf.navId?.let { opf.manifest[it] }
            if (navHref != null) {
                val path = ArchivePolicy.resolveHref(opfDir, navHref) ?: return emptyList()
                val bytes = readEntry(zip, path) ?: return emptyList()
                TableOfContents.resolve(parseNav(bytes), path.substringBeforeLast('/', ""), spine)
            } else {
                val ncxHref = opf.ncxId?.let { opf.manifest[it] } ?: return emptyList()
                val path = ArchivePolicy.resolveHref(opfDir, ncxHref) ?: return emptyList()
                val bytes = readEntry(zip, path) ?: return emptyList()
                TableOfContents.resolve(parseNcx(bytes), path.substringBeforeLast('/', ""), spine)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** EPUB 3 navigation document: the `nav` element with epub:type "toc", nested lists give the depth. */
    private fun parseNav(bytes: ByteArray): List<RawTocEntry> {
        val parser = newParser(bytes)
        val out = ArrayList<RawTocEntry>()
        var inToc = false
        var depth = 0
        var href: String? = null
        var inLink = false
        val title = StringBuilder()
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> when (parser.name) {
                    "nav" -> if (parser.getAttributeValue(null, "epub:type") == "toc") inToc = true
                    "ol" -> if (inToc) depth++
                    "a" -> if (inToc && depth > 0) {
                        inLink = true
                        href = parser.getAttributeValue(null, "href")
                        title.clear()
                    }
                }
                XmlPullParser.TEXT -> if (inLink) title.append(parser.text)
                XmlPullParser.END_TAG -> when (parser.name) {
                    "a" -> if (inLink) {
                        href?.let { out.add(RawTocEntry(title.toString().trim(), it, depth)) }
                        inLink = false
                    }
                    "ol" -> if (inToc) depth--
                    "nav" -> inToc = false
                }
            }
            event = parser.next()
        }
        return out
    }

    /** EPUB 2 NCX: nested `navPoint` elements give the depth; each has a `navLabel` and a `content`. */
    private fun parseNcx(bytes: ByteArray): List<RawTocEntry> {
        val parser = newParser(bytes)
        val out = ArrayList<RawTocEntry>()
        var depth = 0
        var inLabel = false
        val title = StringBuilder()
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> when (parser.name) {
                    "navPoint" -> depth++
                    "navLabel" -> {
                        inLabel = true
                        title.clear()
                    }
                    "content" -> parser.getAttributeValue(null, "src")?.let {
                        out.add(RawTocEntry(title.toString().trim(), it, depth))
                    }
                }
                XmlPullParser.TEXT -> if (inLabel) title.append(parser.text)
                XmlPullParser.END_TAG -> when (parser.name) {
                    "navLabel" -> inLabel = false
                    "navPoint" -> depth--
                }
            }
            event = parser.next()
        }
        return out
    }

    private fun newParser(bytes: ByteArray): XmlPullParser {
        val parser = Xml.newPullParser()
        // S-5: no DTD and no entity declarations are processed, so no external entity can be read.
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_DOCDECL, false)
        parser.setInput(StringReader(bytes.toString(Charsets.UTF_8)))
        return parser
    }
}
