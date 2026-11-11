package com.alain.epure.epub.data

import android.content.Context
import androidx.core.content.edit
import com.alain.epure.epub.domain.ReadingPosition
import com.alain.epure.epub.domain.RecentBook
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Stores the books opened, most recent first, in the private preferences of the app
 * (SPEC.md §7, DEC-EE-0020). The data is serialized with org.json. Nothing leaves the device (T-2),
 * and backup is off.
 */
object RecentStore {

    private const val PREFERENCES = "epure_epub_state"
    private const val KEY_RECENTS = "recents"

    /** Key used by the first version (one book only), read once to keep what was stored. */
    private const val KEY_RECENT_SINGLE = "recent"

    private fun preferences(context: Context) =
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    /** The stored books, most recent first. Empty if none, or if the stored data cannot be read. */
    fun load(context: Context): List<RecentBook> {
        val prefs = preferences(context)
        val text = prefs.getString(KEY_RECENTS, null)
        if (text != null) {
            return try {
                val array = JSONArray(text)
                List(array.length()) { fromJson(array.getJSONObject(it)) }
            } catch (e: JSONException) {
                emptyList()
            }
        }
        // Data written by the first version: one book, kept as a list of one.
        val single = prefs.getString(KEY_RECENT_SINGLE, null) ?: return emptyList()
        return try {
            listOf(fromJson(JSONObject(single)))
        } catch (e: JSONException) {
            emptyList()
        }
    }

    fun save(context: Context, books: List<RecentBook>) {
        val array = JSONArray()
        books.forEach { array.put(toJson(it)) }
        preferences(context).edit {
            putString(KEY_RECENTS, array.toString())
            remove(KEY_RECENT_SINGLE)
        }
    }

    /** Forgets every stored book. The files themselves are never touched. */
    fun clear(context: Context) {
        preferences(context).edit {
            remove(KEY_RECENTS)
            remove(KEY_RECENT_SINGLE)
        }
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
