package com.alain.epure.epub.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ArchivePolicyTest {

    private val limits = ArchiveLimits()

    @Test
    fun ordinaryEntryNamesAreSafe() {
        assertTrue(ArchivePolicy.isSafeEntryName("mimetype"))
        assertTrue(ArchivePolicy.isSafeEntryName("OEBPS/images/img1.png"))
    }

    @Test
    fun unsafeEntryNamesAreRefused() {
        assertFalse(ArchivePolicy.isSafeEntryName(""))
        assertFalse(ArchivePolicy.isSafeEntryName("/etc/hosts"))
        assertFalse(ArchivePolicy.isSafeEntryName("../evil.txt"))
        assertFalse(ArchivePolicy.isSafeEntryName("OEBPS/../../evil.txt"))
        assertFalse(ArchivePolicy.isSafeEntryName("OEBPS\\..\\evil.txt"))
        assertFalse(ArchivePolicy.isSafeEntryName("C:evil.txt"))
        assertFalse(ArchivePolicy.isSafeEntryName("file:evil.txt"))
        assertFalse(ArchivePolicy.isSafeEntryName("a\u0000b"))
    }

    @Test
    fun relativeLinkResolvesInsideArchive() {
        assertEquals("OEBPS/ch2.xhtml", ArchivePolicy.resolveHref("OEBPS", "ch2.xhtml"))
        assertEquals("OEBPS/text/ch2.xhtml", ArchivePolicy.resolveHref("OEBPS", "./text/ch2.xhtml"))
        assertEquals("ch1.xhtml", ArchivePolicy.resolveHref("OEBPS", "../ch1.xhtml"))
    }

    @Test
    fun fragmentAndQueryAreIgnored() {
        assertEquals("OEBPS/ch2.xhtml", ArchivePolicy.resolveHref("OEBPS", "ch2.xhtml#sec3"))
        assertEquals("OEBPS/ch2.xhtml", ArchivePolicy.resolveHref("OEBPS", "ch2.xhtml?x=1"))
    }

    @Test
    fun linksLeavingTheArchiveAreRefused() {
        assertNull(ArchivePolicy.resolveHref("OEBPS", "../../evil.txt"))
        assertNull(ArchivePolicy.resolveHref("", "../evil.txt"))
        assertNull(ArchivePolicy.resolveHref("OEBPS", "/etc/hosts"))
    }

    @Test
    fun externalLinksAreNotResolved() {
        assertNull(ArchivePolicy.resolveHref("OEBPS", "https://example.com/a.png"))
        assertNull(ArchivePolicy.resolveHref("OEBPS", "file:///etc/hosts"))
        assertNull(ArchivePolicy.resolveHref("OEBPS", "mailto:someone@example.com"))
        assertNull(ArchivePolicy.resolveHref("OEBPS", "#only-fragment"))
    }

    @Test
    fun entryCountLimit() {
        assertNull(ArchivePolicy.checkEntryCount(limits.maxEntries, limits))
        assertEquals(RefusalReason.TOO_MANY_ENTRIES, ArchivePolicy.checkEntryCount(limits.maxEntries + 1, limits))
    }

    @Test
    fun entrySizeLimit() {
        assertNull(ArchivePolicy.checkEntrySize(limits.maxEntryBytes, limits))
        assertEquals(RefusalReason.ENTRY_TOO_LARGE, ArchivePolicy.checkEntrySize(limits.maxEntryBytes + 1, limits))
    }

    @Test
    fun totalSizeLimit() {
        assertNull(ArchivePolicy.checkTotalSize(limits.maxTotalBytes, limits))
        assertEquals(RefusalReason.TOTAL_TOO_LARGE, ArchivePolicy.checkTotalSize(limits.maxTotalBytes + 1, limits))
    }

    @Test
    fun compressionRatioLimit() {
        // 512 MiB from 512 KiB is a ratio of 1024: refused (the zip bomb of test document 9).
        assertEquals(
            RefusalReason.COMPRESSION_RATIO_TOO_HIGH,
            ArchivePolicy.checkCompressionRatio(512L * 1024, 512L * 1024 * 1024, limits),
        )
        assertNull(ArchivePolicy.checkCompressionRatio(1_000L, 50_000L, limits))
        assertNull(ArchivePolicy.checkCompressionRatio(0L, 0L, limits))
        assertEquals(
            RefusalReason.COMPRESSION_RATIO_TOO_HIGH,
            ArchivePolicy.checkCompressionRatio(0L, 10L, limits),
        )
    }
}
