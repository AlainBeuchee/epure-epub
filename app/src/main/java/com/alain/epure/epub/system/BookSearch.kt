package com.alain.epure.epub.system

import com.alain.epure.epub.domain.TextSearch

/**
 * Searches the whole book, one chapter at a time (SPEC.md F-11.3, F-11.5). Only one chapter's text
 * is in memory at a time, and it is not stored. Runs outside the main thread.
 */
object BookSearch {

    /**
     * [hits] holds the chapter index of each occurrence, in reading order. [hasText] is false when
     * no chapter contains text (F-11.6). [completed] is false when the search was interrupted (F-11.5).
     */
    class Outcome(val hits: List<Int>, val hasText: Boolean, val completed: Boolean)

    fun run(book: OpenBook, query: String, cancelled: () -> Boolean): Outcome {
        val hits = ArrayList<Int>()
        var hasText = false
        for ((chapterIndex, path) in book.book.spine.withIndex()) {
            if (cancelled()) return Outcome(emptyList(), hasText, completed = false)
            val xhtml = book.readEntry(path) ?: continue
            // The same text nodes as the ones marked when the chapter is shown (DEC-EE-0047).
            val text = ChapterText.plainText(xhtml)
            if (text.isNotBlank()) hasText = true
            repeat(TextSearch.occurrences(text, query)) { hits.add(chapterIndex) }
        }
        return Outcome(hits, hasText, completed = true)
    }
}
