package com.alain.epure.epub.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TextSearchTest {

    @Test
    fun ignoresCase() {
        assertEquals(2, TextSearch.occurrences("Paris et paris", "PARIS"))
    }

    @Test
    fun ignoresAccentsInBothDirections() {
        assertEquals(1, TextSearch.occurrences("Il a été là", "ete"))
        assertEquals(1, TextSearch.occurrences("Il a ete la", "été"))
    }

    @Test
    fun countsNonOverlappingOccurrences() {
        assertEquals(2, TextSearch.occurrences("aaaa", "aa"))
    }

    @Test
    fun blankOrAbsentQueryMatchesNothing() {
        assertEquals(0, TextSearch.occurrences("texte", "   "))
        assertEquals(0, TextSearch.occurrences("texte", ""))
        assertEquals(0, TextSearch.occurrences("texte", "absent"))
    }

    @Test
    fun trimsTheQuery() {
        assertEquals(1, TextSearch.occurrences("le chat", " chat "))
    }

    @Test
    fun counterLabelShowsPositionAndTotal() {
        assertEquals("3 / 17", TextSearch.counterLabel(2, 17))
        assertNull(TextSearch.counterLabel(0, 0))
    }

    @Test
    fun navigationStopsAtTheEnds() {
        assertEquals(1, TextSearch.next(0, 3))
        assertEquals(2, TextSearch.next(2, 3))
        assertEquals(0, TextSearch.previous(0, 3))
        assertEquals(1, TextSearch.previous(2, 3))
        assertEquals(0, TextSearch.next(0, 0))
    }
}
