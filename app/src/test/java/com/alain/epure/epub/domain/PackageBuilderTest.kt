package com.alain.epure.epub.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test

class PackageBuilderTest {

    private val entries = setOf(
        "OEBPS/ch1.xhtml",
        "OEBPS/ch2.xhtml",
        "OEBPS/text/ch3.xhtml",
        "OEBPS/ch four.xhtml",
    )

    private val manifest = mapOf(
        "c1" to "ch1.xhtml",
        "c2" to "ch2.xhtml#top",
        "c3" to "text/ch3.xhtml",
        "c4" to "ch%20four.xhtml",
        "img" to "../../evil.png",
        "missing" to "absent.xhtml",
    )

    @Test
    fun spineFollowsReadingOrderInsideOpfFolder() {
        val book = PackageBuilder.build("OEBPS/content.opf", "Titre", manifest, listOf("c2", "c1", "c3"), entries)
        assertEquals(listOf("OEBPS/ch2.xhtml", "OEBPS/ch1.xhtml", "OEBPS/text/ch3.xhtml"), book.spine)
        assertEquals("Titre", book.title)
    }

    @Test
    fun percentEncodedHrefsAreDecoded() {
        val book = PackageBuilder.build("OEBPS/content.opf", null, manifest, listOf("c4"), entries)
        assertEquals(listOf("OEBPS/ch four.xhtml"), book.spine)
        assertNull(book.title)
    }

    @Test
    fun itemsOutsideArchiveOrMissingAreDropped() {
        val book = PackageBuilder.build(
            "OEBPS/content.opf", "  ", manifest, listOf("img", "missing", "c1", "unknown-id"), entries,
        )
        assertEquals(listOf("OEBPS/ch1.xhtml"), book.spine)
        assertNull(book.title)
    }

    @Test
    fun emptySpineIsRefused() {
        try {
            PackageBuilder.build("OEBPS/content.opf", "T", manifest, listOf("img", "missing"), entries)
            fail("expected refusal")
        } catch (e: BookRefusal) {
            assertEquals(RefusalReason.NO_TEXT, e.reason)
        }
    }

    @Test
    fun packageAtRootHasNoFolder() {
        val book = PackageBuilder.build(
            "content.opf", "T", mapOf("c1" to "OEBPS/ch1.xhtml"), listOf("c1"), entries,
        )
        assertEquals(listOf("OEBPS/ch1.xhtml"), book.spine)
    }
}
