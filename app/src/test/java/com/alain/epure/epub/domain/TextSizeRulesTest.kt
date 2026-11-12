package com.alain.epure.epub.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class TextSizeRulesTest {

    @Test
    fun thereAreSevenStepsFromTwelveToTwentyFourSp() {
        assertEquals(listOf(12, 14, 16, 18, 20, 22, 24), TextSizeRules.STEPS_SP)
    }

    @Test
    fun withoutSavedValueTheDefaultIsSixteenSp() {
        assertEquals(TextSizeRules.DEFAULT_STEP, TextSizeRules.validStep(null))
        assertEquals(16, TextSizeRules.sizeSp(TextSizeRules.validStep(null)))
    }

    @Test
    fun storedStepOutOfRangeIsBroughtBackInside() {
        assertEquals(0, TextSizeRules.validStep(-3))
        assertEquals(6, TextSizeRules.validStep(9))
    }

    @Test
    fun largerAndSmallerMoveOneStepAndStopAtTheEnds() {
        assertEquals(3, TextSizeRules.larger(2))
        assertEquals(6, TextSizeRules.larger(6))
        assertEquals(1, TextSizeRules.smaller(2))
        assertEquals(0, TextSizeRules.smaller(0))
    }

    @Test
    fun zoomIsRelativeToTheSmallestStep() {
        assertEquals(75, TextSizeRules.zoomPercent(0))
        assertEquals(100, TextSizeRules.zoomPercent(2))
        assertEquals(150, TextSizeRules.zoomPercent(6))
    }

    @Test
    fun cycleGoesUpAndBackToTheSmallest() {
        assertEquals(3, TextSizeRules.cycle(2))
        assertEquals(0, TextSizeRules.cycle(6))
    }

    @Test
    fun sizeFollowsTheStep() {
        assertEquals(12, TextSizeRules.sizeSp(0))
        assertEquals(24, TextSizeRules.sizeSp(6))
    }
}
