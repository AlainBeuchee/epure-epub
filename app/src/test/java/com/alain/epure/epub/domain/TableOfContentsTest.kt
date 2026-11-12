package com.alain.epure.epub.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class TableOfContentsTest {

    private val spine = listOf("OEBPS/one.xhtml", "OEBPS/two.xhtml", "OEBPS/sub/three.xhtml")

    @Test
    fun entriesKeepTheirOrderAndDepth() {
        val raw = listOf(
            RawTocEntry("Un", "one.xhtml", 1),
            RawTocEntry("Deux", "two.xhtml", 2),
        )
        assertEquals(
            listOf(TocEntry("Un", 0, 1), TocEntry("Deux", 1, 2)),
            TableOfContents.resolve(raw, "OEBPS", spine),
        )
    }

    @Test
    fun anchorInsideAChapterGoesToThatChapter() {
        val raw = listOf(RawTocEntry("Section", "two.xhtml#part3", 1))
        assertEquals(listOf(TocEntry("Section", 1, 1)), TableOfContents.resolve(raw, "OEBPS", spine))
    }

    @Test
    fun relativeTargetsAreReadFromTheFolderOfTheTableDocument() {
        val raw = listOf(RawTocEntry("Trois", "../sub/three.xhtml", 1))
        assertEquals(listOf(TocEntry("Trois", 2, 1)), TableOfContents.resolve(raw, "OEBPS/nav", spine))
    }

    @Test
    fun targetsOutsideTheBookAreIgnored() {
        val raw = listOf(
            RawTocEntry("Dehors", "../../etc/passwd", 1),
            RawTocEntry("Web", "https://example.com/", 1),
            RawTocEntry("Absent", "missing.xhtml", 1),
            RawTocEntry("Un", "one.xhtml", 1),
        )
        assertEquals(listOf(TocEntry("Un", 0, 1)), TableOfContents.resolve(raw, "OEBPS", spine))
    }

    @Test
    fun emptyTitleGetsTheChapterNumber() {
        val raw = listOf(RawTocEntry("  ", "two.xhtml", 1))
        assertEquals(listOf(TocEntry("Chapitre 2", 1, 1)), TableOfContents.resolve(raw, "OEBPS", spine))
    }
}
