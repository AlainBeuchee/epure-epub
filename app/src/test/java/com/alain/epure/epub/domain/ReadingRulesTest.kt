package com.alain.epure.epub.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ReadingRulesTest {

    @Test
    fun withoutSavedPositionTheBookStartsAtTheBeginning() {
        assertEquals(ReadingPosition(0, 0), ReadingRules.restore(null, spineSize = 5))
    }

    @Test
    fun validPositionIsRestoredAsSaved() {
        val saved = ReadingPosition(3, 1200)
        assertEquals(saved, ReadingRules.restore(saved, spineSize = 5))
    }

    @Test
    fun chapterThatNoLongerExistsOpensTheBookAtTheStart() {
        assertEquals(ReadingPosition(0, 0), ReadingRules.restore(ReadingPosition(7, 300), spineSize = 5))
        assertEquals(ReadingPosition(0, 0), ReadingRules.restore(ReadingPosition(-1, 300), spineSize = 5))
    }

    @Test
    fun offsetOutsideTheChapterOpensItsStart() {
        assertEquals(ReadingPosition(2, 0), ReadingRules.restore(ReadingPosition(2, -40), spineSize = 5))
    }

    @Test
    fun emptyBookOpensAtTheStart() {
        assertEquals(ReadingPosition(0, 0), ReadingRules.restore(ReadingPosition(0, 10), spineSize = 0))
    }

    @Test
    fun progressIsTheChapterRankPlusTheFractionRead() {
        assertEquals(0, ReadingRules.progressPercent(0, 10, 0f))
        assertEquals(50, ReadingRules.progressPercent(5, 10, 0f))
        assertEquals(55, ReadingRules.progressPercent(5, 10, 0.5f))
        assertEquals(100, ReadingRules.progressPercent(9, 10, 1f))
    }

    @Test
    fun progressStaysBetweenZeroAndHundred() {
        assertEquals(0, ReadingRules.progressPercent(0, 10, -3f))
        assertEquals(100, ReadingRules.progressPercent(9, 10, 4f))
        assertEquals(0, ReadingRules.progressPercent(0, 0, 0.5f))
    }

    @Test
    fun bookIsIdentifiedByUriOrByFileName() {
        val recent = RecentBook("content://a/1", "roman.epub", "Roman", ReadingPosition(0, 0), 0)
        assertEquals(true, recent.matches("content://a/1", null))
        assertEquals(true, recent.matches("content://b/9", "roman.epub"))
        assertEquals(false, recent.matches("content://b/9", "autre.epub"))
        assertEquals(false, recent.matches("content://b/9", null))
    }
}
