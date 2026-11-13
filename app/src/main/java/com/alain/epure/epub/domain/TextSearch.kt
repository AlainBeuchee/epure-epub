package com.alain.epure.epub.domain

/**
 * The search panel's state (SPEC.md F-11). [hits] holds, for each occurrence in reading order,
 * the chapter it is in; [current] is the index of the occurrence shown; [query] is the text searched.
 * [noText] is set when no chapter contains text (F-11.6). Pure data, no Android dependency.
 */
data class SearchState(
    val open: Boolean = false,
    val running: Boolean = false,
    val searched: Boolean = false,
    val hits: List<Int> = emptyList(),
    val current: Int = 0,
    val noText: Boolean = false,
    val query: String = "",
) {
    /**
     * Position, among the occurrences of chapter [chapterIndex], of the occurrence shown; null when the
     * occurrence shown is in another chapter (F-11.4).
     */
    fun activeHitInChapter(chapterIndex: Int): Int? {
        if (hits.isEmpty() || hits.getOrNull(current) != chapterIndex) return null
        return current - hits.indexOf(chapterIndex)
    }
}

/**
 * Rules for searching text in a book (SPEC.md F-11). Pure Kotlin, no Android dependency.
 *
 * The search ignores letter case and accents: « ete » finds « été » (F-11.2, DEC-EE-0038 a).
 * The text itself is never stored here; callers pass the text of one chapter at a time (F-11.3, F-11.5).
 */
object TextSearch {

    /** Number of occurrences of [query] in [chapterText] (F-11.4). Occurrences do not overlap. */
    fun occurrences(chapterText: String, query: String): Int = TextMatch.find(chapterText, query).size

    /** Label of the counter « 3 / 17 » (F-11.4), or null when there is no result. */
    fun counterLabel(current: Int, total: Int): String? =
        if (total <= 0) null else "${current.coerceIn(0, total - 1) + 1} / $total"

    /** Index after « Suivant » (F-11.4). Stops at the last result, no wrap-around. */
    fun next(current: Int, total: Int): Int = if (total <= 0) 0 else (current + 1).coerceAtMost(total - 1)

    /** Index after « Précédent » (F-11.4). Stops at the first result, no wrap-around. */
    fun previous(current: Int, total: Int): Int = if (total <= 0) 0 else (current - 1).coerceAtLeast(0)
}
