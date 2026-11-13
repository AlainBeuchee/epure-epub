package com.alain.epure.epub.domain

import java.text.Normalizer

/** A match in a text: characters from [start] (included) to [end] (excluded), in the original text. */
data class TextRange(val start: Int, val end: Int)

/**
 * Finds a query in a text, ignoring case, accents and the amount of white space (SPEC.md F-11.2).
 * Positions are given in the original text, so that the same matches can be marked in the page.
 * Pure Kotlin, no Android dependency.
 */
object TextMatch {

    /** Matches of [query] in [text], in order, without overlap. A blank query matches nothing. */
    fun find(text: String, query: String): List<TextRange> {
        val needle = fold(query).text.trim()
        if (needle.isEmpty()) return emptyList()
        val folded = fold(text)
        val result = ArrayList<TextRange>()
        var from = folded.text.indexOf(needle)
        while (from >= 0) {
            val last = from + needle.length - 1
            val start = folded.origin[from]
            val end = extendOverMarks(text, folded.origin[last] + 1, trailingSpace = folded.text[last] == ' ')
            result.add(TextRange(start, end))
            from = folded.text.indexOf(needle, from + needle.length)
        }
        return result
    }

    /** A folded text, and for each of its characters the position of the original character it comes from. */
    private class Folded(val text: String, val origin: IntArray)

    /**
     * Lower case, without accents, and each run of white space becomes one space.
     * Leading white space is dropped.
     */
    private fun fold(text: String): Folded {
        val out = StringBuilder(text.length)
        val origin = ArrayList<Int>(text.length)
        for ((i, c) in text.withIndex()) {
            if (c.isWhitespace()) {
                if (out.isNotEmpty() && out.last() != ' ') {
                    out.append(' ')
                    origin.add(i)
                }
                continue
            }
            // Plain ASCII letters need no decomposition: this keeps long chapters fast.
            val base = if (c.code < 128) {
                c.lowercaseChar().toString()
            } else {
                Normalizer.normalize(c.toString(), Normalizer.Form.NFD)
                    .filter { !isMark(it) }
                    .lowercase()
            }
            for (ch in base) {
                out.append(ch)
                origin.add(i)
            }
        }
        return Folded(out.toString(), origin.toIntArray())
    }

    /** Extends a match over the combining marks and white space that follow it in the original text. */
    private fun extendOverMarks(text: String, from: Int, trailingSpace: Boolean): Int {
        var end = from
        while (end < text.length && (isMark(text[end]) || (trailingSpace && text[end].isWhitespace()))) {
            end++
        }
        return end
    }

    /** True for a combining mark (accent carried by the previous letter). */
    private fun isMark(c: Char): Boolean = Character.getType(c) == Character.NON_SPACING_MARK.toInt()
}
