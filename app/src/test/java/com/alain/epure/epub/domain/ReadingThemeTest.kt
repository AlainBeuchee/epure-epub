package com.alain.epure.epub.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ReadingThemeTest {

    @Test
    fun defaultIsTheBlackTheme() {
        assertEquals(ReadingTheme.NOIR, ReadingTheme.DEFAULT)
        assertEquals(ReadingTheme.NOIR, ReadingTheme.fromName(null))
    }

    @Test
    fun savedNameIsRestored() {
        assertEquals(ReadingTheme.SEPIA, ReadingTheme.fromName("SEPIA"))
    }

    @Test
    fun unknownNameFallsBackToTheDefault() {
        assertEquals(ReadingTheme.NOIR, ReadingTheme.fromName("NIGHT_OLD"))
    }

    @Test
    fun nextCyclesThroughAllThemes() {
        assertEquals(ReadingTheme.SEPIA, ReadingTheme.DAY.next())
        assertEquals(ReadingTheme.SLATE, ReadingTheme.SEPIA.next())
        assertEquals(ReadingTheme.AMOLED, ReadingTheme.SLATE.next())
        assertEquals(ReadingTheme.NOIR, ReadingTheme.AMOLED.next())
        assertEquals(ReadingTheme.DAY, ReadingTheme.NOIR.next())
    }

    @Test
    fun coloursAreThoseOfTheProposal() {
        assertEquals("#F8F7F4", ReadingTheme.DAY.background)
        assertEquals("#222222", ReadingTheme.DAY.text)
        assertEquals("#121212", ReadingTheme.AMOLED.background)
        assertEquals("#C4C4C4", ReadingTheme.AMOLED.text)
        assertEquals("#000000", ReadingTheme.NOIR.background)
        assertEquals("#E6E6E6", ReadingTheme.NOIR.text)
    }
}
