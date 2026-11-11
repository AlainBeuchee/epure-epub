package com.alain.epure.epub.domain

/**
 * The last book opened (SPEC.md F-4.1, F-4.2). Only what is needed to show it and reopen it
 * at the same place is kept: never the content of the book.
 */
data class RecentBook(
    val uri: String,
    val fileName: String?,
    val title: String?,
    val position: ReadingPosition,
    val progressPercent: Int,
) {
    /**
     * The same book, by its URI or, if the URI changed, by its file name (SPEC.md F-4.5).
     * Two books with different names but the same URI are the same book.
     */
    fun matches(otherUri: String, otherFileName: String?): Boolean =
        uri == otherUri || (fileName != null && fileName == otherFileName)
}
