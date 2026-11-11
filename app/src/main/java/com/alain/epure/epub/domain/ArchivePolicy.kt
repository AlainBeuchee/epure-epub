package com.alain.epure.epub.domain

/**
 * Limits applied to an EPUB archive before it is read (SPEC.md S-4).
 * The default values are the proposals of S-4 and must be confirmed by measurement (SPEC.md §9.1).
 */
data class ArchiveLimits(
    val maxEntries: Int = 5_000,
    val maxEntryBytes: Long = 50L * 1024 * 1024,
    val maxTotalBytes: Long = 200L * 1024 * 1024,
    val maxCompressionRatio: Long = 100L,
)

/** Why an archive or one of its entries is refused. */
enum class RefusalReason {
    UNSAFE_PATH,
    TOO_MANY_ENTRIES,
    ENTRY_TOO_LARGE,
    TOTAL_TOO_LARGE,
    COMPRESSION_RATIO_TOO_HIGH,
    NOT_ARCHIVE,
    NO_PACKAGE,
    NO_TEXT,
}

/** Thrown when a book must not be opened. The reason is mapped to a message in strings.xml. */
class BookRefusal(val reason: RefusalReason) : Exception(reason.name)

/** Pure checks on archive names and sizes. No Android dependency. */
object ArchivePolicy {

    /**
     * An entry name is safe when it stays inside the archive: no absolute path, no drive or
     * scheme (':'), no backslash, no NUL character and no '..' segment.
     */
    fun isSafeEntryName(name: String): Boolean {
        if (name.isEmpty()) return false
        if (name.startsWith('/')) return false
        if (name.any { it == '\\' || it == ':' || it == '\u0000' }) return false
        return name.split('/').none { it == ".." }
    }

    /**
     * Resolves a link `href` written in a document located in `baseDir` (a folder path inside the
     * archive, empty for the root). Fragments and queries are removed.
     * Returns the normalized entry path, or null if the link leaves the archive or is unsafe.
     */
    fun resolveHref(baseDir: String, href: String): String? {
        val target = decodePercent(href.substringBefore('#').substringBefore('?')) ?: return null
        if (target.isEmpty()) return null
        if (target.startsWith('/') || target.contains("://") || target.contains(':')) return null
        val segments = ArrayList<String>()
        val combined = if (baseDir.isEmpty()) target else "$baseDir/$target"
        for (part in combined.split('/')) {
            when (part) {
                "", "." -> Unit
                ".." -> {
                    if (segments.isEmpty()) return null
                    segments.removeAt(segments.lastIndex)
                }
                else -> segments.add(part)
            }
        }
        if (segments.isEmpty()) return null
        return segments.joinToString("/")
    }

    /**
     * Decodes %XX escapes of a link. Returns null if an escape is malformed.
     * '+' is kept as is: in EPUB links it is a plus sign, not a space.
     */
    fun decodePercent(text: String): String? {
        if (!text.contains('%')) return text
        val source = text.toByteArray(Charsets.UTF_8)
        val out = java.io.ByteArrayOutputStream()
        var i = 0
        while (i < source.size) {
            val b = source[i]
            if (b == '%'.code.toByte()) {
                if (i + 2 >= source.size) return null
                val hex = String(source, i + 1, 2, Charsets.US_ASCII)
                if (!hex.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }) return null
                val value = hex.toInt(16)
                out.write(value)
                i += 3
            } else {
                out.write(b.toInt())
                i++
            }
        }
        return out.toByteArray().toString(Charsets.UTF_8)
    }

    fun checkEntryCount(count: Int, limits: ArchiveLimits): RefusalReason? =
        if (count > limits.maxEntries) RefusalReason.TOO_MANY_ENTRIES else null

    fun checkEntrySize(bytes: Long, limits: ArchiveLimits): RefusalReason? =
        if (bytes > limits.maxEntryBytes) RefusalReason.ENTRY_TOO_LARGE else null

    fun checkTotalSize(bytes: Long, limits: ArchiveLimits): RefusalReason? =
        if (bytes > limits.maxTotalBytes) RefusalReason.TOTAL_TOO_LARGE else null

    /**
     * Compares the uncompressed size with the compressed size of one entry.
     * A non-empty entry with a zero compressed size is refused.
     */
    fun checkCompressionRatio(
        compressedBytes: Long,
        uncompressedBytes: Long,
        limits: ArchiveLimits,
    ): RefusalReason? {
        if (uncompressedBytes == 0L) return null
        if (compressedBytes <= 0L) return RefusalReason.COMPRESSION_RATIO_TOO_HIGH
        val ratio = uncompressedBytes / compressedBytes
        return if (ratio > limits.maxCompressionRatio) RefusalReason.COMPRESSION_RATIO_TOO_HIGH else null
    }
}
