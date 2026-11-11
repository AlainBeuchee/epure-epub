package com.alain.epure.epub.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class LinkPolicyTest {

    private val host = "epure.invalid"
    private val spine = listOf("OEBPS/ch1.xhtml", "OEBPS/ch 2.xhtml", "OEBPS/notes/ch3.xhtml")

    private fun classify(url: String) = LinkPolicy.classify(url, host, spine)

    @Test
    fun linkToChapterOfTheBookIsFollowed() {
        assertEquals(LinkTarget.Chapter(0, null), classify("https://$host/OEBPS/ch1.xhtml"))
        assertEquals(LinkTarget.Chapter(2, null), classify("https://$host/OEBPS/notes/ch3.xhtml"))
    }

    @Test
    fun fragmentIsKeptAndQueryIgnored() {
        assertEquals(LinkTarget.Chapter(0, "part2"), classify("https://$host/OEBPS/ch1.xhtml#part2"))
        assertEquals(LinkTarget.Chapter(0, null), classify("https://$host/OEBPS/ch1.xhtml?x=1"))
    }

    @Test
    fun percentEscapesAreDecoded() {
        assertEquals(LinkTarget.Chapter(1, null), classify("https://$host/OEBPS/ch%202.xhtml"))
    }

    @Test
    fun addressesOutsideTheBookAreExternal() {
        assertEquals(LinkTarget.External, classify("https://example.com/page"))
        assertEquals(LinkTarget.External, classify("http://$host/OEBPS/ch1.xhtml"))
        assertEquals(LinkTarget.External, classify("mailto:someone@example.com"))
        assertEquals(LinkTarget.External, classify("file:///etc/hosts"))
        assertEquals(LinkTarget.External, classify("https://$host.example.com/OEBPS/ch1.xhtml"))
        assertEquals(LinkTarget.External, classify("https://$host@example.com/OEBPS/ch1.xhtml"))
    }

    @Test
    fun pathNotInTheReadingOrderIsUnavailable() {
        assertEquals(LinkTarget.Unavailable, classify("https://$host/OEBPS/images/cover.jpg"))
        assertEquals(LinkTarget.Unavailable, classify("https://$host/"))
    }

    @Test
    fun pathLeavingTheArchiveIsUnavailable() {
        assertEquals(LinkTarget.Unavailable, classify("https://$host/../etc/passwd"))
        assertEquals(LinkTarget.Unavailable, classify("https://$host/OEBPS/%2e%2e/ch1.xhtml"))
    }

    @Test
    fun malformedEscapeIsUnavailable() {
        assertEquals(LinkTarget.Unavailable, classify("https://$host/OEBPS/ch%zz.xhtml"))
    }
}
