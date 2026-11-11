package com.alain.epure.epub.data

import android.content.Context
import androidx.core.content.edit

/**
 * Settings of the app (SPEC.md F-16.1). Stored in the private preferences of the app, never sent anywhere.
 */
object AppSettings {

    private const val PREFERENCES = "epure_epub_settings"
    private const val KEY_BOOK_LIST = "book_list_enabled"

    private fun preferences(context: Context) =
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    /** « Liste des livres » (SPEC.md F-4.2): off by default. */
    fun isBookListEnabled(context: Context): Boolean =
        preferences(context).getBoolean(KEY_BOOK_LIST, false)

    fun setBookListEnabled(context: Context, enabled: Boolean) {
        preferences(context).edit { putBoolean(KEY_BOOK_LIST, enabled) }
    }
}
