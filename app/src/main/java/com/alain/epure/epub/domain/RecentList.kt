package com.alain.epure.epub.domain

/**
 * The list of books opened, most recent first (SPEC.md F-4.2). Without the « Liste des livres »
 * setting only one book is kept; with it, up to [MAX_WITH_LIST] books. No Android dependency.
 */
object RecentList {

    const val MAX_WITH_LIST = 10
    const val MAX_WITHOUT_LIST = 1

    fun limit(listEnabled: Boolean): Int = if (listEnabled) MAX_WITH_LIST else MAX_WITHOUT_LIST

    /** Puts [book] first, replacing an earlier entry for the same book, and keeps at most [limit] entries. */
    fun upsert(list: List<RecentBook>, book: RecentBook, limit: Int): List<RecentBook> =
        (listOf(book) + list.filterNot { it.matches(book.uri, book.fileName) }).take(limit)

    /** Removes the entry with this URI. The file itself is not concerned. */
    fun remove(list: List<RecentBook>, uri: String): List<RecentBook> =
        list.filterNot { it.uri == uri }

    /** Keeps at most [limit] entries, most recent first. */
    fun trim(list: List<RecentBook>, limit: Int): List<RecentBook> = list.take(limit)
}
