package com.alain.epure.epub.system

import android.content.Context
import android.view.View

/**
 * Rendering engine of the app (DEC-EE-0015). The UI depends only on this interface,
 * so another engine can replace the WebView without changing the screens.
 */
interface ChapterRenderer {

    /** The Android view that shows the chapter. */
    val view: View

    /** Shows the chapter at [chapterPath] of [book], from the top. */
    fun show(book: OpenBook, chapterPath: String)

    /** Forgets the book: nothing of it stays loaded in the engine. */
    fun release()
}

/** Creates the engine used by the reading screen. */
fun newChapterRenderer(context: Context, onTap: (TapSide) -> Unit): ChapterRenderer =
    BookWebView(context, onTap)
