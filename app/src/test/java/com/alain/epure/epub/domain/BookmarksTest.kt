package com.alain.epure.epub.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BookmarksTest {

    private val a = Bookmark(spineIndex = 1, scrollY = 0, progressPercent = 10)
    private val b = Bookmark(spineIndex = 3, scrollY = 400, progressPercent = 42)

    @Test
    fun chapterIsMarkedWhenABookmarkIsInIt() {
        assertTrue(BookmarkRules.isMarked(listOf(a, b), 3))
        assertFalse(BookmarkRules.isMarked(listOf(a, b), 2))
    }

    @Test
    fun toggleAddsWhenTheChapterIsNotMarked() {
        assertEquals(listOf(a, b), BookmarkRules.toggle(listOf(b), a))
    }

    @Test
    fun toggleRemovesWhenTheChapterIsAlreadyMarked() {
        assertEquals(listOf(a), BookmarkRules.toggle(listOf(a, b), b))
    }

    @Test
    fun listIsKeptInReadingOrder() {
        val later = Bookmark(spineIndex = 3, scrollY = 900, progressPercent = 50)
        assertEquals(listOf(a, b, later), BookmarkRules.sorted(listOf(later, b, a)))
    }

    @Test
    fun removeDropsOnlyTheGivenBookmark() {
        assertEquals(listOf(a), BookmarkRules.remove(listOf(a, b), b))
    }
}
