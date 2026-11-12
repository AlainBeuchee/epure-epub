package com.alain.epure.epub.data

import android.content.Context
import androidx.core.content.edit

/**
 * Settings of the app (SPEC.md F-16.1). Stored in the private preferences of the app, never sent anywhere.
 */
object AppSettings {

    private const val PREFERENCES = "epure_epub_settings"
    private const val KEY_BOOK_LIST = "book_list_enabled"
    private const val KEY_TEXT_STEP = "text_size_step"
    private const val KEY_PURE_WHITE = "pure_white"
    private const val KEY_KEEP_SCREEN_ON = "keep_screen_on"

    private fun preferences(context: Context) =
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    /** « Liste des livres » (SPEC.md F-4.2): off by default. */
    fun isBookListEnabled(context: Context): Boolean =
        preferences(context).getBoolean(KEY_BOOK_LIST, false)

    fun setBookListEnabled(context: Context, enabled: Boolean) {
        preferences(context).edit { putBoolean(KEY_BOOK_LIST, enabled) }
    }

    /**
     * Colour of the book text: softened white by default, pure white on request (DEC-EE-0024).
     */
    fun isPureWhite(context: Context): Boolean =
        preferences(context).getBoolean(KEY_PURE_WHITE, false)

    fun setPureWhite(context: Context, pure: Boolean) {
        preferences(context).edit { putBoolean(KEY_PURE_WHITE, pure) }
    }

    /** « Écran allumé pendant la lecture » (SPEC.md F-9): on by default. */
    fun isKeepScreenOn(context: Context): Boolean =
        preferences(context).getBoolean(KEY_KEEP_SCREEN_ON, true)

    fun setKeepScreenOn(context: Context, on: Boolean) {
        preferences(context).edit { putBoolean(KEY_KEEP_SCREEN_ON, on) }
    }

    /** Text size step (SPEC.md F-3.1), 16 sp by default (DEC-EE-0024). */
    fun textSizeStep(context: Context): Int =
        com.alain.epure.epub.domain.TextSizeRules.validStep(
            preferences(context).takeIf { it.contains(KEY_TEXT_STEP) }?.getInt(KEY_TEXT_STEP, 0)
        )

    fun setTextSizeStep(context: Context, step: Int) {
        preferences(context).edit { putInt(KEY_TEXT_STEP, com.alain.epure.epub.domain.TextSizeRules.validStep(step)) }
    }
}
