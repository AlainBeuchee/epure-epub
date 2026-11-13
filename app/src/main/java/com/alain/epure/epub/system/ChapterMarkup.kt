package com.alain.epure.epub.system

import com.alain.epure.epub.domain.TextMatch
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode

/**
 * Marks the occurrences of a search in a chapter (SPEC.md F-11.4, DEC-EE-0047).
 *
 * Every occurrence is wrapped in a `span` with the class [HIT_CLASS] and, for the first piece of each,
 * the id [HIT_PREFIX] followed by its number in the chapter. The occurrence shown is also given
 * inline colours that reverse the page colours (« négatif »). Only the app writes these attributes.
 */
object ChapterMarkup {

    const val HIT_PREFIX = "epure-hit-"
    private const val HIT_CLASS = "epure-hit"

    /** A part of one occurrence, inside one text node: characters [from] (included) to [to] (excluded). */
    private class Piece(val hit: Int, val from: Int, val to: Int, val first: Boolean)

    /**
     * The chapter as HTML, cleaned (see [ChapterText.parseSafe]). When [highlight] is given, its occurrences
     * are marked; the one at [ChapterHighlight.activeHit] is shown with the page colours reversed,
     * using [pageBackground] and [pageText] (the colours of the reading theme).
     */
    fun render(xhtml: ByteArray, highlight: ChapterHighlight?, pageBackground: String, pageText: String): String {
        val document = ChapterText.parseSafe(xhtml)
        if (highlight == null || highlight.query.isBlank()) return document.outerHtml()

        val nodes = ChapterText.textNodes(document)
        // Text of each node, taken before the tree changes, and where each node starts in the whole text.
        val texts = nodes.map { it.wholeText }
        val starts = IntArray(nodes.size)
        var position = 0
        for (n in texts.indices) {
            starts[n] = position
            position += texts[n].length
        }
        val matches = TextMatch.find(texts.joinToString(""), highlight.query)

        val pieces = Array(nodes.size) { ArrayList<Piece>() }
        matches.forEachIndexed { hit, range ->
            for (n in nodes.indices) {
                val nodeStart = starts[n]
                val nodeEnd = nodeStart + texts[n].length
                if (texts[n].isEmpty() || nodeEnd <= range.start || nodeStart >= range.end) continue
                val from = maxOf(range.start, nodeStart) - nodeStart
                val to = minOf(range.end, nodeEnd) - nodeStart
                val first = range.start >= nodeStart && range.start < nodeEnd
                pieces[n].add(Piece(hit, from, to, first))
            }
        }

        for (n in nodes.indices) {
            val node: TextNode = nodes[n]
            // From the last piece to the first: splitting the node never moves the text before a piece.
            for (piece in pieces[n].asReversed()) {
                val target = if (piece.from > 0) node.splitText(piece.from) else node
                val length = piece.to - piece.from
                if (length < target.wholeText.length) target.splitText(length)
                val span = Element("span").addClass(HIT_CLASS)
                if (piece.first) span.attr("id", HIT_PREFIX + piece.hit)
                if (piece.hit == highlight.activeHit) {
                    // Reversed colours: the page text becomes the background of the occurrence.
                    span.attr(
                        "style",
                        "background-color: $pageText !important; color: $pageBackground !important;",
                    )
                }
                target.before(span)
                span.appendChild(target)
            }
        }
        return document.outerHtml()
    }
}
