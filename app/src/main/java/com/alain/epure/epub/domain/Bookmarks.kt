package com.alain.epure.epub.domain

/**
 * A bookmark: an anchor in the book (chapter rank and offset), with the chapter title and the
 * position in that chapter at the time it was set (SPEC.md F-10.2). [title] is null when the chapter
 * has no table entry; [chapterPercent] is null for bookmarks saved before this format (no percentage shown).
 * Pure data, no Android dependency.
 */
data class Bookmark(
    val spineIndex: Int,
    val scrollY: Int,
    val title: String?,
    val chapterPercent: Int?,
)

/** Rules for bookmarks (SPEC.md F-10). */
object BookmarkRules {

    /**
     * The list after the action « Ajouter marque-page » (F-10.1, DEC-EE-0039): adds [current], several
     * bookmarks per chapter are allowed. A bookmark already at the same place is not added twice.
     * Kept sorted by position in the book (F-10.3).
     */
    fun add(bookmarks: List<Bookmark>, current: Bookmark): List<Bookmark> {
        if (bookmarks.any { it.spineIndex == current.spineIndex && it.scrollY == current.scrollY }) {
            return bookmarks
        }
        return sorted(bookmarks + current)
    }

    /** Bookmarks in reading order: by chapter, then by offset. */
    fun sorted(bookmarks: List<Bookmark>): List<Bookmark> =
        bookmarks.sortedWith(compareBy({ it.spineIndex }, { it.scrollY }))

    /** Bookmarks without the one at [position] (used by « Retirer » in the list, F-10.3). */
    fun remove(bookmarks: List<Bookmark>, position: Bookmark): List<Bookmark> =
        bookmarks.filter { it != position }
}
