package com.alain.epure.epub.system

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChapterTextTest {

    private fun bytes(xhtml: String): ByteArray = xhtml.toByteArray(Charsets.UTF_8)

    @Test
    fun plainTextKeepsTheBodyTextOnly() {
        val text = ChapterText.plainText(
            bytes(
                """<?xml version="1.0" encoding="utf-8"?>
                <html xmlns="http://www.w3.org/1999/xhtml"><head><title>Titre</title></head>
                <body><h1>Chapitre un</h1><p>Il était une fois.</p></body></html>"""
            )
        )
        assertTrue(text.contains("Chapitre un"))
        assertTrue(text.contains("Il était une fois."))
        assertFalse(text.contains("Titre"))
    }

    @Test
    fun plainTextLeavesOutScriptAndStyleContent() {
        val text = ChapterText.plainText(
            bytes(
                "<html><head><style>p { color: red; }</style></head>" +
                    "<body><p>Avant</p><script>alert('x')</script><p>Après</p></body></html>"
            )
        )
        assertFalse(text.contains("alert"))
        assertFalse(text.contains("color"))
        assertTrue(text.contains("Avant"))
        assertTrue(text.contains("Après"))
    }

    @Test
    fun plainTextDecodesEntities() {
        val text = ChapterText.plainText(bytes("<html><body><p>Tom &amp; Jerry &eacute;</p></body></html>"))
        assertTrue(text.contains("Tom & Jerry é"))
    }

    @Test
    fun imageOnlyChapterHasNoText() {
        assertEquals("", ChapterText.plainText(bytes("<html><body><img src=\"cover.jpg\" alt=\"\"/></body></html>")).trim())
    }

    @Test
    fun removesScriptsFramesAndEventHandlers() {
        val document = ChapterText.parseSafe(
            bytes(
                "<html><body onload=\"evil()\"><p onclick=\"evil()\">Texte</p>" +
                    "<iframe src=\"https://example.org\"></iframe>" +
                    "<a href=\"javascript:evil()\">lien</a>" +
                    "<a href=\" java&#9;script:evil() \">autre</a>" +
                    "<meta http-equiv=\"refresh\" content=\"0;url=https://example.org\"/>" +
                    "<base href=\"https://example.org/\"/>" +
                    "<script>evil()</script></body></html>"
            )
        )
        val html = document.outerHtml().lowercase()
        assertFalse(html.contains("script"))
        assertFalse(html.contains("onclick"))
        assertFalse(html.contains("onload"))
        assertFalse(html.contains("iframe"))
        assertFalse(html.contains("refresh"))
        assertFalse(html.contains("<base"))
        assertFalse(html.contains("javascript:"))
        assertTrue(html.contains("texte"))
    }
}
