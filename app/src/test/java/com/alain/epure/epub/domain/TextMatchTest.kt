package com.alain.epure.epub.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TextMatchTest {

    @Test
    fun rangesPointAtTheOriginalText() {
        val text = "Le Été est là"
        val ranges = TextMatch.find(text, "ete")
        assertEquals(listOf(TextRange(3, 6)), ranges)
        assertEquals("Été", text.substring(ranges[0].start, ranges[0].end))
    }

    @Test
    fun accentsAreIgnoredInTheQueryToo() {
        assertEquals(1, TextMatch.find("il a ete là", "été").size)
    }

    @Test
    fun whiteSpaceRunsCountAsOneSpace() {
        val text = "Il était\n   une\tfois"
        val ranges = TextMatch.find(text, "une fois")
        assertEquals(1, ranges.size)
        assertEquals("une\tfois", text.substring(ranges[0].start, ranges[0].end))
    }

    @Test
    fun combiningMarksStayInsideTheMatch() {
        // « é » written as e followed by a combining accent.
        val text = "café noir"
        val ranges = TextMatch.find(text, "cafe")
        assertEquals(listOf(TextRange(0, 5)), ranges)
    }

    @Test
    fun blankQueryMatchesNothing() {
        assertTrue(TextMatch.find("texte", "   ").isEmpty())
    }
}
