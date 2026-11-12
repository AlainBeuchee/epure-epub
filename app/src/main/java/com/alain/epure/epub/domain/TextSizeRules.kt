package com.alain.epure.epub.domain

/**
 * Text size of the book (SPEC.md F-3.1, DEC-EE-0021).
 *
 * Seven steps in sp, from 12 to 24. The values are starting points, to be checked on the device.
 * Extended down to 12 and 14 sp on the owner's request (DEC-EE-0027).
 * Pure Kotlin: no Android dependency, so it can be unit-tested.
 */
object TextSizeRules {

    /** Text sizes in sp, from the smallest to the largest step. */
    val STEPS_SP: List<Int> = listOf(12, 14, 16, 18, 20, 22, 24)

    /** Size used when nothing has been saved yet: 16 sp (DEC-EE-0024). */
    const val DEFAULT_SP = 16

    /** Step of the default size. */
    val DEFAULT_STEP: Int = STEPS_SP.indexOf(DEFAULT_SP)

    /** Step for a size in sp (saved sizes); the default step when the size is unknown. */
    fun stepForSp(sp: Int?): Int = STEPS_SP.indexOf(sp).takeIf { it >= 0 } ?: DEFAULT_STEP

    /** Keeps a stored step inside the valid range; an unknown value falls back to the default. */
    fun validStep(stored: Int?): Int =
        if (stored == null) DEFAULT_STEP else stored.coerceIn(0, STEPS_SP.lastIndex)

    /** Size in sp for a step. */
    fun sizeSp(step: Int): Int = STEPS_SP[validStep(step)]

    /**
     * WebView text zoom in percent for a step, relative to the 16 sp base (100 % at 16 sp).
     * Used because the engine scales the book's own text with this value.
     */
    fun zoomPercent(step: Int): Int = sizeSp(step) * 100 / DEFAULT_SP

    /** Next larger step, stopping at the largest one. */
    fun larger(step: Int): Int = (validStep(step) + 1).coerceAtMost(STEPS_SP.lastIndex)

    /** Next step in a cycle: after the largest one, back to the smallest (used by a tap in Paramètres). */
    fun cycle(step: Int): Int =
        if (validStep(step) == STEPS_SP.lastIndex) 0 else validStep(step) + 1

    /** Next smaller step, stopping at the smallest one. */
    fun smaller(step: Int): Int = (validStep(step) - 1).coerceAtLeast(0)
}
