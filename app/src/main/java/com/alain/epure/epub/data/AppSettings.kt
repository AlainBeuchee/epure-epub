package com.alain.epure.epub.data

import android.content.Context
import androidx.core.content.edit
import com.alain.epure.epub.domain.ReadingTheme

/**
 * Settings of the app (SPEC.md F-16.1). Stored in the private preferences of the app, never sent anywhere.
 */
object AppSettings {

    private const val PREFERENCES = "epure_epub_settings"
    private const val KEY_BOOK_LIST = "book_list_enabled"
    private const val KEY_TEXT_SIZE_SP = "text_size_sp"
    private const val KEY_READING_THEME = "reading_theme"
    private const val KEY_KEEP_SCREEN_ON = "keep_screen_on"
    private const val KEY_LEFT_HANDED = "left_handed"

    private fun preferences(context: Context) =
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    /** « Liste des livres » (SPEC.md F-4.2): off by default. */
    fun isBookListEnabled(context: Context): Boolean =
        preferences(context).getBoolean(KEY_BOOK_LIST, false)

    fun setBookListEnabled(context: Context, enabled: Boolean) {
        preferences(context).edit { putBoolean(KEY_BOOK_LIST, enabled) }
    }

    /** Colour theme of the book (DEC-EE-0026): the dark AMOLED theme by default. */
    fun readingTheme(context: Context): ReadingTheme =
        ReadingTheme.fromName(preferences(context).getString(KEY_READING_THEME, null))

    fun setReadingTheme(context: Context, theme: ReadingTheme) {
        preferences(context).edit { putString(KEY_READING_THEME, theme.name) }
    }

    /** Latéralité (DEC-EE-0032): false by default, meaning right-handed. */
    fun isLeftHanded(context: Context): Boolean =
        preferences(context).getBoolean(KEY_LEFT_HANDED, false)

    fun setLeftHanded(context: Context, leftHanded: Boolean) {
        preferences(context).edit { putBoolean(KEY_LEFT_HANDED, leftHanded) }
    }

    /** « Écran allumé pendant la lecture » (SPEC.md F-9): on by default. */
    fun isKeepScreenOn(context: Context): Boolean =
        preferences(context).getBoolean(KEY_KEEP_SCREEN_ON, true)

    fun setKeepScreenOn(context: Context, on: Boolean) {
        preferences(context).edit { putBoolean(KEY_KEEP_SCREEN_ON, on) }
    }

    /** Text size step (SPEC.md F-3.1), 16 sp by default (DEC-EE-0024). */
    fun textSizeStep(context: Context): Int {
        val prefs = preferences(context)
        val sp = if (prefs.contains(KEY_TEXT_SIZE_SP)) prefs.getInt(KEY_TEXT_SIZE_SP, 0) else null
        return com.alain.epure.epub.domain.TextSizeRules.stepForSp(sp)
    }

    /** Stores the size in sp (not the step), so that the saved value stays valid if the steps change. */
    fun setTextSizeStep(context: Context, step: Int) {
        val sp = com.alain.epure.epub.domain.TextSizeRules.sizeSp(step)
        preferences(context).edit { putInt(KEY_TEXT_SIZE_SP, sp) }
    }
}
