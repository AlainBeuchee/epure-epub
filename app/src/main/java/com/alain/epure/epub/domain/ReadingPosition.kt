package com.alain.epure.epub.domain

import kotlin.math.roundToInt

/**
 * Where the reader was in a book (DEC-EE-0010, SPEC.md F-5.1): the rank of the chapter in the
 * reading order and the vertical offset inside that chapter, in pixels.
 */
data class ReadingPosition(val spineIndex: Int, val scrollY: Int)

/** Pure rules for reading positions. No Android dependency. */
object ReadingRules {

    /**
     * The position to open a book at (SPEC.md F-5.3, F-5.4, F-5.5).
     * No saved position, or a chapter that no longer exists: the start of the book.
     * An offset outside the chapter: the start of that chapter.
     */
    fun restore(saved: ReadingPosition?, spineSize: Int): ReadingPosition {
        if (saved == null || saved.spineIndex !in 0 until spineSize) return ReadingPosition(0, 0)
        return ReadingPosition(saved.spineIndex, saved.scrollY.coerceAtLeast(0))
    }

    /**
     * Progress in the book, from 0 to 100, from the chapter rank and the fraction of that chapter
     * already scrolled (0.0 to 1.0).
     */
    fun progressPercent(spineIndex: Int, spineSize: Int, fractionInChapter: Float): Int {
        if (spineSize <= 0) return 0
        val done = (spineIndex + fractionInChapter.coerceIn(0f, 1f)) / spineSize
        return (done * 100f).roundToInt().coerceIn(0, 100)
    }
}
