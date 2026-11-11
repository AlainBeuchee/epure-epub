package com.alain.epure.epub.data

import android.content.Context
import androidx.core.content.edit
import com.alain.epure.epub.domain.ReadingPosition
import com.alain.epure.epub.domain.RecentBook
import org.json.JSONException
import org.json.JSONObject

/**
 * Stores the last book opened, in the private preferences of the app (SPEC.md §7, DEC-EE-0020).
 * The data is serialized with org.json. Nothing leaves the device (T-2), and backup is off.
 */
object RecentStore {

    private const val PREFERENCES = "epure_epub_state"
    private const val KEY_RECENT = "recent"

    private fun preferences(context: Context) =
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    /** The last book opened, or null if none, or if the stored data cannot be read. */
    fun load(context: Context): RecentBook? {
        val text = preferences(context).getString(KEY_RECENT, null) ?: return null
        return try {
            fromJson(JSONObject(text))
        } catch (e: JSONException) {
            null
        }
    }

    fun save(context: Context, book: RecentBook) {
        preferences(context).edit { putString(KEY_RECENT, toJson(book).toString()) }
    }

    /** Forgets the stored book. The file itself is never touched. */
    fun clear(context: Context) {
        preferences(context).edit { remove(KEY_RECENT) }
    }

    private fun toJson(book: RecentBook): JSONObject = JSONObject()
        .put("uri", book.uri)
        .put("fileName", book.fileName ?: JSONObject.NULL)
        .put("title", book.title ?: JSONObject.NULL)
        .put("spineIndex", book.position.spineIndex)
        .put("scrollY", book.position.scrollY)
        .put("progress", book.progressPercent)

    private fun fromJson(json: JSONObject): RecentBook = RecentBook(
        uri = json.getString("uri"),
        fileName = json.optionalString("fileName"),
        title = json.optionalString("title"),
        position = ReadingPosition(json.getInt("spineIndex"), json.getInt("scrollY")),
        progressPercent = json.getInt("progress"),
    )

    private fun JSONObject.optionalString(key: String): String? =
        if (isNull(key)) null else getString(key)
}
