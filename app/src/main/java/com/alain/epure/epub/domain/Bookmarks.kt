package com.alain.epure.epub.domain

/**
 * A bookmark: an anchor in the book (chapter rank and offset), with the progression at that point
 * (SPEC.md F-10.2). Pure data, no Android dependency.
 */
data class Bookmark(val spineIndex: Int, val scrollY: Int, val progressPercent: Int)

/** Rules for bookmarks (SPEC.md F-10). */
object BookmarkRules {

    /** True if a bookmark is set in the chapter [spineIndex] (F-10.1: the action then removes it). */
    fun isMarked(bookmarks: List<Bookmark>, spineIndex: Int): Boolean =
        bookmarks.any { it.spineIndex == spineIndex }

    /**
     * The list after the action of F-10.1: adds [current] if its chapter is not marked, otherwise
     * removes the bookmarks of that chapter. Kept sorted by position in the book (F-10.3).
     */
    fun toggle(bookmarks: List<Bookmark>, current: Bookmark): List<Bookmark> {
        val updated = if (isMarked(bookmarks, current.spineIndex)) {
            bookmarks.filter { it.spineIndex != current.spineIndex }
        } else {
            bookmarks + current
        }
        return sorted(updated)
    }

    /** Bookmarks in reading order: by chapter, then by offset. */
    fun sorted(bookmarks: List<Bookmark>): List<Bookmark> =
        bookmarks.sortedWith(compareBy({ it.spineIndex }, { it.scrollY }))

    /** Bookmarks without the one at [position] (used by « Retirer » in the list, F-10.3). */
    fun remove(bookmarks: List<Bookmark>, position: Bookmark): List<Bookmark> =
        bookmarks.filter { it != position }
}
