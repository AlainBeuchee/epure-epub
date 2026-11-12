package com.alain.epure.epub.system

import android.content.Context
import android.view.View
import com.alain.epure.epub.domain.LinkTarget
import com.alain.epure.epub.domain.ReadingTheme

/**
 * Rendering engine of the app (DEC-EE-0015). The UI depends only on this interface,
 * so another engine can replace the WebView without changing the screens.
 */
interface ChapterRenderer {

    /** The Android view that shows the chapter. */
    val view: View

    /**
     * Shows the chapter at [chapterPath] of [book]. It starts at the top, or at [startScrollY]
     * when the book is reopened at a saved place.
     */
    fun show(book: OpenBook, chapterPath: String, startScrollY: Int = 0, theme: ReadingTheme = ReadingTheme.DEFAULT)

    /**
     * Sets the text zoom in percent (SPEC.md F-3.1). The passage being read stays on screen.
     */
    fun setTextZoom(percent: Int)

    /** Forgets the book: nothing of it stays loaded in the engine. */
    fun release()
}

/**
 * Creates the engine used by the reading screen.
 * [onPosition] receives the vertical offset and the fraction of the chapter read, once scrolling settles.
 */
fun newChapterRenderer(
    context: Context,
    onTap: (TapSide) -> Unit,
    onLink: (LinkTarget) -> Unit,
    onPosition: (scrollY: Int, fraction: Float) -> Unit,
    onBandTap: () -> Unit,
    onRevealBar: () -> Unit,
): ChapterRenderer = BookWebView(context, onTap, onLink, onPosition, onBandTap, onRevealBar)
