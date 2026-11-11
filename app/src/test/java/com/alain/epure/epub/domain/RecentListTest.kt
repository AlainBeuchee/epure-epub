package com.alain.epure.epub.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class RecentListTest {

    private fun book(name: String, uri: String = "content://a/$name") =
        RecentBook(uri, "$name.epub", name, ReadingPosition(0, 0), 0)

    @Test
    fun limitDependsOnTheListSetting() {
        assertEquals(1, RecentList.limit(listEnabled = false))
        assertEquals(10, RecentList.limit(listEnabled = true))
    }

    @Test
    fun mostRecentBookComesFirst() {
        val list = RecentList.upsert(listOf(book("a")), book("b"), limit = 10)
        assertEquals(listOf("b", "a"), list.map { it.title })
    }

    @Test
    fun reopeningABookMovesItToTheTopWithoutDuplicate() {
        val list = listOf(book("a"), book("b"), book("c"))
        val updated = RecentList.upsert(list, book("b"), limit = 10)
        assertEquals(listOf("b", "a", "c"), updated.map { it.title })
    }

    @Test
    fun sameFileNameUnderAnotherUriReplacesTheEntry() {
        val list = listOf(book("a"))
        val other = RecentBook("content://other/9", "a.epub", "a", ReadingPosition(2, 80), 40)
        val updated = RecentList.upsert(list, other, limit = 10)
        assertEquals(listOf("content://other/9"), updated.map { it.uri })
    }

    @Test
    fun listIsLimited() {
        var list: List<RecentBook> = emptyList()
        for (i in 1..12) list = RecentList.upsert(list, book("b$i"), limit = RecentList.MAX_WITH_LIST)
        assertEquals(10, list.size)
        assertEquals("b12", list.first().title)
        assertEquals("b3", list.last().title)
    }

    @Test
    fun withoutTheListOnlyTheLastBookIsKept() {
        val list = RecentList.upsert(listOf(book("a")), book("b"), limit = RecentList.MAX_WITHOUT_LIST)
        assertEquals(listOf("b"), list.map { it.title })
    }

    @Test
    fun removeKeepsTheOthers() {
        val list = listOf(book("a"), book("b"))
        assertEquals(listOf("b"), RecentList.remove(list, "content://a/a").map { it.title })
    }

    @Test
    fun trimKeepsTheMostRecent() {
        val list = listOf(book("a"), book("b"), book("c"))
        assertEquals(listOf("a"), RecentList.trim(list, 1).map { it.title })
    }
}
