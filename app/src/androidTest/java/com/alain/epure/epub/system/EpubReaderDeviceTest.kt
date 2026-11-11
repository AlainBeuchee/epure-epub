package com.alain.epure.epub.system

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import com.alain.epure.epub.domain.BookRefusal
import com.alain.epure.epub.domain.RefusalReason
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Runs the reader on the test books of SPEC.md annex A, copied outside the repository to
 * /sdcard/Android/data/com.alain.epure.epub/files/tests/. Results are logged under "EpureReaderTest".
 */
class EpubReaderDeviceTest {

    private val dir: File
        get() = File(InstrumentationRegistry.getInstrumentation().targetContext.filesDir, "tests")

    private fun describe(name: String): String {
        val file = File(dir, name)
        return try {
            EpubReader.open(file).use { open ->
                "OK title=${open.book.title} spine=${open.book.spine.size} " +
                    "warning=${open.mimetypeWarning} first=${open.book.spine.firstOrNull()}"
            }
        } catch (e: BookRefusal) {
            "REFUSED ${e.reason}"
        } catch (e: Throwable) {
            "ERROR ${e.javaClass.simpleName}: ${e.message}"
        }
    }

    private fun assertOpens(name: String) {
        val result = describe(name)
        assertTrue("$name: $result", result.startsWith("OK"))
    }

    private fun assertRefused(name: String, reason: RefusalReason) {
        assertEquals("$name", "REFUSED $reason", describe(name))
    }

    @Test
    fun logsEveryTestBook() {
        val names = dir.listFiles()?.map { it.name }?.filter { it.endsWith(".epub") }?.sorted().orEmpty()
        assertTrue("no test book in $dir", names.isNotEmpty())
        for (name in names) Log.i(TAG, "$name -> ${describe(name)}")
    }

    @Test
    fun validBooksOpen() {
        assertOpens("01-roman-epub3-nav.epub")
        assertOpens("02-essai-epub2-ncx.epub")
        assertOpens("03-images-nombreuses.epub")
        assertOpens("04-tableaux-listes.epub")
        assertOpens("14-un-chapitre-sans-table.epub")
    }

    @Test
    fun wrongMimetypeOpensWithWarning() {
        // DEC-EE-0017: the book opens, and the warning flag is set.
        EpubReader.open(File(dir, "17-mimetype-faux.epub")).use { open ->
            assertTrue("warning expected", open.mimetypeWarning)
            assertTrue(open.book.spine.isNotEmpty())
        }
        EpubReader.open(File(dir, "16-s7-fichiers-locaux.epub")).use { open ->
            assertTrue("no warning expected for a valid mimetype", !open.mimetypeWarning)
        }
    }

    @Test
    fun hostileBooksAreRefused() {
        assertRefused("08-chemins-parent.epub", RefusalReason.UNSAFE_PATH)
        assertRefused("09-bombe-zip.epub", RefusalReason.ENTRY_TOO_LARGE)
        assertRefused("12-faux-epub.epub", RefusalReason.NOT_ARCHIVE)
    }

    private companion object {
        const val TAG = "EpureReaderTest"
    }
}
