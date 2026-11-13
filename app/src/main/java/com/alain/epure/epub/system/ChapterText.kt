package com.alain.epure.epub.system

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import java.io.ByteArrayInputStream

/**
 * Reads one chapter (XHTML bytes from the EPUB archive) as a document that is safe to show and to search.
 *
 * The book is untrusted (SPEC.md S-1, DEC-EE-0047): scripts, frames, base addresses, event handlers and
 * script links are removed before anything else. The chapter is only parsed here, never executed, and
 * the empty base URI means nothing is resolved or fetched.
 */
object ChapterText {

    /** Elements whose content is not part of the readable text. */
    private val NON_TEXT_TAGS = setOf("script", "style", "head", "title", "noscript", "template")

    /** Elements removed outright: they run code, load a page, or redirect. */
    private const val UNSAFE_ELEMENTS = "script, iframe, frame, frameset, object, embed, applet, base, meta[http-equiv]"

    /** Attributes that may carry a script address. */
    private val LINK_ATTRIBUTES = setOf("href", "src", "xlink:href", "action", "formaction", "data", "poster")

    private val UNSAFE_ADDRESS = listOf("javascript:", "vbscript:", "data:text/html")

    /** Parses and cleans a chapter. */
    fun parseSafe(xhtml: ByteArray): Document {
        val document = ByteArrayInputStream(xhtml).use { input -> Jsoup.parse(input, null, "") }
        document.select(UNSAFE_ELEMENTS).remove()
        document.allElements.forEach { element -> removeUnsafeAttributes(element) }
        return document
    }

    /**
     * The readable text of a chapter: the text nodes of the body, in order, without scripts and styles.
     * Search and display use these same nodes, so that the occurrences counted are the occurrences shown.
     */
    fun textNodes(document: Document): List<TextNode> {
        val body = document.body() ?: return emptyList()
        val nodes = ArrayList<TextNode>()
        collectTextNodes(body, nodes)
        return nodes
    }

    /** The text of a chapter, as searched (F-11.3). */
    fun plainText(xhtml: ByteArray): String =
        textNodes(parseSafe(xhtml)).joinToString("") { it.wholeText }

    private fun collectTextNodes(node: Node, into: MutableList<TextNode>) {
        for (child in node.childNodes()) {
            when (child) {
                is TextNode -> into.add(child)
                is Element -> if (child.tagName() !in NON_TEXT_TAGS) collectTextNodes(child, into)
            }
        }
    }

    /** Removes event handlers (on*) and script addresses from the attributes of [element]. */
    private fun removeUnsafeAttributes(element: Element) {
        val attributes = element.attributes()
        val remove = attributes.asList().filter { attribute ->
            val key = attribute.key.lowercase()
            // Browsers ignore white space inside a scheme (« java&#9;script: »), so it is removed before the check.
            val value = attribute.value.filterNot { it.isWhitespace() }.lowercase()
            key.startsWith("on") ||
                (key in LINK_ATTRIBUTES && UNSAFE_ADDRESS.any { value.startsWith(it) })
        }.map { it.key }
        remove.forEach { attributes.remove(it) }
    }
}
