package com.alain.epure.epub.domain

/**
 * Text size of the book (SPEC.md F-3.1, DEC-EE-0021).
 *
 * Five steps in sp. The values are starting points, to be checked on the device.
 * Pure Kotlin: no Android dependency, so it can be unit-tested.
 */
object TextSizeRules {

    /** Text sizes in sp, from the smallest to the largest step. */
    val STEPS_SP: List<Int> = listOf(16, 18, 20, 22, 24)

    /** Step used when nothing has been saved yet: 16 sp (DEC-EE-0024). */
    const val DEFAULT_STEP = 0

    /** Keeps a stored step inside the valid range; an unknown value falls back to the default. */
    fun validStep(stored: Int?): Int =
        if (stored == null) DEFAULT_STEP else stored.coerceIn(0, STEPS_SP.lastIndex)

    /** Size in sp for a step. */
    fun sizeSp(step: Int): Int = STEPS_SP[validStep(step)]

    /**
     * WebView text zoom in percent for a step, relative to the 16 sp base.
     * Used because the engine scales the book's own text with this value.
     */
    fun zoomPercent(step: Int): Int = sizeSp(step) * 100 / STEPS_SP.first()

    /** Next larger step, stopping at the largest one. */
    fun larger(step: Int): Int = (validStep(step) + 1).coerceAtMost(STEPS_SP.lastIndex)

    /** Next smaller step, stopping at the smallest one. */
    fun smaller(step: Int): Int = (validStep(step) - 1).coerceAtLeast(0)
}
