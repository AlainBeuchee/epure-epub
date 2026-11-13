package com.alain.epure.epub.system

import com.alain.epure.epub.domain.TextMatch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChapterMarkupTest {

    private fun bytes(xhtml: String): ByteArray = xhtml.toByteArray(Charsets.UTF_8)

    private fun render(xhtml: String, query: String?, active: Int?): String =
        ChapterMarkup.render(
            bytes(xhtml),
            query?.let { ChapterHighlight(it, active ?: 0) },
            pageBackground = "#000000",
            pageText = "#E6E6E6",
        )

    private fun countOf(text: String, part: String): Int = text.split(part).size - 1

    @Test
    fun withoutQueryTheChapterIsOnlyCleaned() {
        val html = render("<html><body><p>Bonjour</p><script>evil()</script></body></html>", null, null)
        assertTrue(html.contains("Bonjour"))
        assertFalse(html.contains("evil"))
    }

    @Test
    fun everyOccurrenceGetsItsNumberAndCountMatchesTheSearch() {
        val xhtml = "<html><body><p>Le chat et le chien. LE CHAT dort.</p><p>Un chat.</p></body></html>"
        val html = render(xhtml, "chat", 0)
        val expected = TextMatch.find(ChapterText.plainText(bytes(xhtml)), "chat").size
        assertEquals(3, expected)
        assertEquals(expected, countOf(html, "id=\"${ChapterMarkup.HIT_PREFIX}"))
        for (n in 0 until expected) {
            assertTrue(html.contains("id=\"${ChapterMarkup.HIT_PREFIX}$n\""))
        }
    }

    @Test
    fun accentsDoNotPreventMarking() {
        val html = render("<html><body><p>Il a été là.</p></body></html>", "ete", 0)
        assertTrue(html.contains("id=\"${ChapterMarkup.HIT_PREFIX}0\""))
    }

    @Test
    fun occurrenceAcrossTagsIsMarkedInEveryPiece() {
        val html = render("<html><body><p>Il était <em>une</em> fois</p></body></html>", "une fois", 0)
        // The match starts inside the text before <em> and ends after it: two pieces, one id.
        assertEquals(1, countOf(html, "id=\"${ChapterMarkup.HIT_PREFIX}0\""))
        assertEquals(2, countOf(html, "class=\"epure-hit\""))
    }

    @Test
    fun onlyTheShownOccurrenceIsReversed() {
        val html = render("<html><body><p>chat chat</p></body></html>", "chat", 1)
        assertEquals(1, countOf(html, "background-color: #E6E6E6 !important; color: #000000 !important;"))
    }

    @Test
    fun scriptsAreNotKeptWhenMarking() {
        val html = render(
            "<html><body><p onclick=\"evil()\">chat</p><script>evil()</script></body></html>",
            "chat",
            0,
        )
        assertFalse(html.contains("onclick"))
        assertFalse(html.contains("<script"))
        assertTrue(html.contains("id=\"${ChapterMarkup.HIT_PREFIX}0\""))
    }
}
