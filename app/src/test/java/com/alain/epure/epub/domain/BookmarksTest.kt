package com.alain.epure.epub.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class BookmarksTest {

    private val a = Bookmark(spineIndex = 1, scrollY = 0, title = "Un", chapterPercent = 10)
    private val b = Bookmark(spineIndex = 3, scrollY = 400, title = null, chapterPercent = 42)

    @Test
    fun addKeepsReadingOrder() {
        assertEquals(listOf(a, b), BookmarkRules.add(listOf(b), a))
    }

    @Test
    fun addAllowsSeveralBookmarksInTheSameChapter() {
        val second = Bookmark(spineIndex = 3, scrollY = 900, title = null, chapterPercent = 60)
        assertEquals(listOf(b, second), BookmarkRules.add(listOf(b), second))
    }

    @Test
    fun addDoesNotDuplicateTheSamePlace() {
        val same = Bookmark(spineIndex = 3, scrollY = 400, title = null, chapterPercent = 42)
        assertEquals(listOf(b), BookmarkRules.add(listOf(b), same))
    }

    @Test
    fun listIsKeptInReadingOrder() {
        val later = Bookmark(spineIndex = 3, scrollY = 900, title = "Trois", chapterPercent = 50)
        assertEquals(listOf(a, b, later), BookmarkRules.sorted(listOf(later, b, a)))
    }

    @Test
    fun removeDropsOnlyTheGivenBookmark() {
        assertEquals(listOf(a), BookmarkRules.remove(listOf(a, b), b))
    }
}
