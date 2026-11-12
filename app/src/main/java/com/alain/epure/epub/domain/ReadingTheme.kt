package com.alain.epure.epub.domain

/**
 * Colour themes of the book text and page (DEC-EE-0026). The app interface keeps its own colours.
 * Pure data: no Android dependency, so it can be unit-tested.
 */
enum class ReadingTheme(val background: String, val text: String) {
    DAY(background = "#F8F7F4", text = "#222222"),
    SEPIA(background = "#F4ECD8", text = "#3B3126"),
    SLATE(background = "#1E1F22", text = "#D4D4D4"),
    AMOLED(background = "#121212", text = "#C4C4C4"),
    NOIR(background = "#000000", text = "#E6E6E6");

    /** The theme after this one, going back to the first after the last. */
    fun next(): ReadingTheme = values()[(ordinal + 1) % values().size]

    companion object {
        /** Pure black with softened white text, the look the app had before themes existed (DEC-EE-0026). */
        val DEFAULT = NOIR

        /** The theme saved under [name], or the default when the name is unknown. */
        fun fromName(name: String?): ReadingTheme =
            values().firstOrNull { it.name == name } ?: DEFAULT
    }
}
