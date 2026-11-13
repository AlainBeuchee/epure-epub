package com.alain.epure.epub.data

import android.content.Context
import androidx.core.content.edit
import com.alain.epure.epub.domain.Bookmark
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Bookmarks of each book, stored in the private preferences of the app (SPEC.md F-10.4).
 * They are never written into the EPUB file. Serialized with org.json; backup is off.
 */
object BookmarkStore {

    private const val PREFERENCES = "epure_epub_state"
    private const val KEY_BOOKMARKS = "bookmarks"

    private fun preferences(context: Context) =
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    /** The bookmarks of the book at [uri], in reading order. Empty if none or unreadable. */
    fun load(context: Context, uri: String): List<Bookmark> {
        val all = readAll(context)
        val array = all.optJSONArray(uri) ?: return emptyList()
        return List(array.length()) { i ->
            val item = array.getJSONObject(i)
            Bookmark(
                spineIndex = item.getInt("spine"),
                scrollY = item.getInt("y"),
                title = item.optString("t").ifEmpty { null },
                // Bookmarks saved before this format have no "c": they show no percentage.
                chapterPercent = if (item.has("c")) item.getInt("c") else null,
            )
        }
    }

    /** Replaces the bookmarks of the book at [uri]. */
    fun save(context: Context, uri: String, bookmarks: List<Bookmark>) {
        val all = readAll(context)
        val array = JSONArray()
        bookmarks.forEach { b ->
            val item = JSONObject().put("spine", b.spineIndex).put("y", b.scrollY)
            b.title?.let { item.put("t", it) }
            b.chapterPercent?.let { item.put("c", it) }
            array.put(item)
        }
        all.put(uri, array)
        preferences(context).edit { putString(KEY_BOOKMARKS, all.toString()) }
    }

    private fun readAll(context: Context): JSONObject {
        val text = preferences(context).getString(KEY_BOOKMARKS, null) ?: return JSONObject()
        return try {
            JSONObject(text)
        } catch (e: JSONException) {
            JSONObject()
        }
    }
}
