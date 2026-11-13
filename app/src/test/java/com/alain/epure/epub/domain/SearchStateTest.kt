package com.alain.epure.epub.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SearchStateTest {

    // Occurrences: two in chapter 2, one in chapter 5, two in chapter 7.
    private val state = SearchState(open = true, searched = true, hits = listOf(2, 2, 5, 7, 7), current = 0)

    @Test
    fun firstOccurrenceOfAChapterIsNumberedZero() {
        assertEquals(0, state.activeHitInChapter(2))
    }

    @Test
    fun laterOccurrenceInTheSameChapterIsNumberedWithinIt() {
        assertEquals(1, state.copy(current = 1).activeHitInChapter(2))
        assertEquals(1, state.copy(current = 4).activeHitInChapter(7))
    }

    @Test
    fun noHighlightInAChapterWithoutTheShownOccurrence() {
        assertNull(state.activeHitInChapter(5))
        assertNull(state.copy(current = 2).activeHitInChapter(2))
    }

    @Test
    fun noHighlightWithoutResults() {
        assertNull(SearchState().activeHitInChapter(0))
    }
}
