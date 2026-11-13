package com.alain.epure.epub.system

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import kotlin.math.abs
import android.webkit.CookieManager
import android.webkit.MimeTypeMap
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.alain.epure.epub.domain.ArchivePolicy
import com.alain.epure.epub.domain.LinkPolicy
import com.alain.epure.epub.domain.ReadingTheme
import com.alain.epure.epub.domain.LinkTarget
import java.io.ByteArrayInputStream

/** Where a single tap landed, as a fraction of the screen width (DEC-EE-0016). */
enum class TapSide { LEFT, RIGHT, CENTER }

/**
 * The rendering engine: a WebView configured as in DEC-EE-0015 (SPEC.md S-1 to S-7).
 * It shows one chapter of an open book. Only files of that archive can be loaded.
 */
@SuppressLint("ViewConstructor")
class BookWebView(
    context: Context,
    private val onTap: (TapSide) -> Unit,
    private val onLink: (LinkTarget) -> Unit,
    private val onPosition: (scrollY: Int, fraction: Float) -> Unit,
    private val onBandTap: () -> Unit,
    private val onRevealBar: () -> Unit,
) : WebView(context), ChapterRenderer {

    override val view: View get() = this

    private var book: OpenBook? = null

    /** Offset to apply once the chapter is loaded (reopening a book, F-5.3). */
    private var pendingScrollY = 0

    /** Id of the search occurrence to bring into view once the chapter is loaded (F-11.4), if any. */
    private var pendingFocusId: String? = null


    private val density = context.resources.displayMetrics.density
    private val minSwipeDistance = SWIPE_DISTANCE_DP * density
    private val minSwipeVelocity = ViewConfiguration.get(context).scaledMinimumFlingVelocity

    /** Text zoom currently applied, in percent. */
    private var textZoom = 100

    private val taps = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            val width = width.takeIf { it > 0 } ?: return false
            // A short tap in the bottom band opens the command bar (SPEC.md F-6.1, DEC-EE-0021).
            if (e.y >= height - BOTTOM_BAND_DP * density) {
                onBandTap()
                return true
            }
            val fraction = e.x / width
            onTap(
                when {
                    fraction < SIDE_FRACTION -> TapSide.LEFT
                    fraction > 1f - SIDE_FRACTION -> TapSide.RIGHT
                    else -> TapSide.CENTER
                }
            )
            return true
        }

        // A horizontal swipe changes chapter (F-6.5): swipe left goes forward, like a tap on the right side.
        // Vertical flings stay with the WebView, which scrolls the chapter.
        override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            val start = e1 ?: return false
            val dx = e2.x - start.x
            val dy = e2.y - start.y
            // A swipe down goes back up in the text and shows the command bar (DEC-EE-0028).
            // A swipe up keeps reading forward and leaves the bar as it is.
            if (dy >= minSwipeDistance && abs(dy) > abs(dx)) {
                onRevealBar()
                return true
            }
            if (abs(dx) < minSwipeDistance || abs(dx) <= abs(dy) || abs(velocityX) < minSwipeVelocity) return false
            onTap(if (dx < 0) TapSide.RIGHT else TapSide.LEFT)
            return true
        }
    })

    init {
        setBackgroundColor(Color.BLACK)
        settings.apply {
            // Only the app's own snippet runs (DEC-EE-0047); the book's scripts were removed before display.
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = false
            domStorageEnabled = false
            databaseEnabled = false
            allowFileAccess = false
            allowContentAccess = false
            blockNetworkLoads = true
            setSupportZoom(false)
            builtInZoomControls = false
            cacheMode = WebSettings.LOAD_NO_CACHE
        }
        CookieManager.getInstance().setAcceptCookie(false)
        webViewClient = BookClient()
        // The detector only observes touches: scrolling is still handled by the WebView.
        setOnTouchListener { _, event ->
            taps.onTouchEvent(event)
            false
        }
    }

    // Every scroll is reported; the caller decides when to save it (SPEC.md F-5.2).
    // Scrolling does not change the command bar.
    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        if (book != null) onPosition(scrollY, scrollFraction())
    }

    /** Fraction of the chapter already scrolled, from 0.0 to 1.0. */
    private fun scrollFraction(): Float {
        val range = (contentHeight * scale - height).coerceAtLeast(1f)
        return (scrollY / range).coerceIn(0f, 1f)
    }

    /**
     * Changes the text zoom. The new layout is laid out again, so the same fraction of the chapter
     * is scrolled back into view once it is ready (SPEC.md F-3.1: the passage read is kept).
     */
    override fun setTextZoom(percent: Int) {
        if (percent == textZoom) return
        textZoom = percent
        val fraction = scrollFraction()
        settings.textZoom = percent
        // Nothing read yet (or nothing loaded): there is no passage to keep.
        if (fraction <= 0f) return
        // The new layout is not measured yet; the offset is applied after a short delay.
        postDelayed({
            val range = (contentHeight * scale - height).coerceAtLeast(1f)
            scrollTo(0, (fraction * range).toInt())
        }, TEXT_ZOOM_SETTLE_MS)
    }

    /**
     * Shows the chapter at [chapterPath] of [openBook]. The chapter starts at the top,
     * or at [startScrollY] when the book is reopened at a saved place.
     */
    override fun show(
        openBook: OpenBook,
        chapterPath: String,
        startScrollY: Int,
        theme: ReadingTheme,
        highlight: ChapterHighlight?,
    ) {
        book = openBook
        pendingScrollY = startScrollY.coerceAtLeast(0)
        pendingFocusId = highlight?.let { ChapterMarkup.HIT_PREFIX + it.activeHit }
        val bytes = openBook.readEntry(chapterPath)
        if (bytes == null) {
            pendingFocusId = null
            loadDataWithBaseURL(BASE_URL, "", "text/html", "utf-8", null)
            return
        }
        // The chapter is cleaned first (DEC-EE-0047), then the search occurrences are marked, then the app's style is added.
        val html = ChapterMarkup.render(bytes, highlight, theme.background, theme.text)
        loadDataWithBaseURL(
            BASE_URL + chapterPath,
            withReadingStyle(html, theme),
            "text/html",
            "utf-8",
            null,
        )
        scrollTo(0, 0)
    }

    /**
     * Brings the element [id] (the search occurrence shown) to the top part of the screen.
     * Only the app's own snippet runs here (DEC-EE-0047); [id] is built by the app from a number.
     */
    private fun scrollToElement(id: String) {
        val script = "(function(){var e=document.getElementById('$id');" +
            "if(!e){return -1;}return e.getBoundingClientRect().top+window.pageYOffset;})()"
        evaluateJavascript(script) { result ->
            val top = result?.toDoubleOrNull() ?: return@evaluateJavascript
            if (top < 0) return@evaluateJavascript
            // The position is in CSS pixels; scale converts it to screen pixels.
            // The occurrence stays below the title bar and the status bar, which overlay the top of the text
            // while the search panel is open: the margin is the base margin plus four lines of text.
            val lineDp = FOCUS_LINE_DP * textZoom / 100f
            val marginDp = FOCUS_MARGIN_DP + FOCUS_LINES * lineDp
            val target = (top * scale).toInt() - (marginDp * density).toInt()
            scrollTo(0, target.coerceAtLeast(0))
        }
    }

    /**
     * Imposes the reading colours and font of the app on the book (DEC-EE-0011, F-3.3):
     * black background, light text, system sans-serif. Images keep their own colours.
     * The rule is added at the end of the chapter, so it wins over most of the book's own styles.
     */
    private fun withReadingStyle(html: String, theme: ReadingTheme): String {
        val style = readingStyle(theme)
        val closing = Regex("</body>", RegexOption.IGNORE_CASE).find(html)
        return if (closing != null) {
            html.substring(0, closing.range.first) + style + html.substring(closing.range.first)
        } else {
            html + style
        }
    }

    /**
     * Colours of the chosen reading theme (DEC-EE-0026) on the book's text and page.
     */
    private fun readingStyle(theme: ReadingTheme): String {
        return "<style>html body, html body * { background-color: ${theme.background} !important; color: ${theme.text} !important; " +
            // Line spacing of 1.4 times the text size (SPEC.md F-2.3, DEC-EE-0021).
            "line-height: 1.4 !important; " +
            "font-family: sans-serif !important; text-align: left !important; } " +
            // Line length: at most 75 characters, centred when the screen is wider (DEC-EE-0024).
            "html body { max-width: 75ch !important; margin-left: auto !important; margin-right: auto !important; } " +
            "html body img, html body svg, html body image { background-color: transparent !important; } " +
            "html body div { height: auto !important; } " +
            // Cover pages are often one SVG image sized in percent: give it a real size (the page stayed black).
            "html body svg { width: 100% !important; height: auto !important; } " +
            "html body img { height: auto !important; max-height: none !important; max-width: 100% !important; }</style>"
    }

    /** Forgets the book: nothing of it stays loaded in the WebView. */
    override fun release() {
        book = null
        loadUrl("about:blank")
    }

    private inner class BookClient : WebViewClient() {

        // Once the chapter is laid out, the saved offset can be applied (F-5.3).
        override fun onPageFinished(view: WebView?, url: String?) {
            val focus = pendingFocusId
            if (focus != null) {
                // A search occurrence is shown: it takes the place of the saved offset.
                pendingFocusId = null
                pendingScrollY = 0
                this@BookWebView.scrollToElement(focus)
            } else if (pendingScrollY > 0) {
                this@BookWebView.scrollTo(0, pendingScrollY)
                pendingScrollY = 0
            }
        }

        // The engine never navigates by itself. A clicked link is classified (LinkPolicy) and handed to the
        // reader: a chapter of the book moves the reading, anything else is not opened (F-14.1, F-14.2).
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            val url = request?.url?.toString() ?: return true
            val spine = book?.book?.spine ?: emptyList()
            onLink(LinkPolicy.classify(url, BASE_HOST, spine))
            return true
        }

        // Only files of the open archive are served (S-2, S-7). Everything else is refused.
        override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
            val uri = request?.url ?: return missing()
            if (uri.scheme == "data") return null
            if (uri.scheme != "https" || uri.host != BASE_HOST) return missing()
            val path = uri.path?.trimStart('/')?.takeIf { it.isNotEmpty() } ?: return missing()
            if (!ArchivePolicy.isSafeEntryName(path)) return missing()
            val bytes = try {
                book?.readEntry(path)
            } catch (e: Exception) {
                null
            } ?: return missing()
            return WebResourceResponse(mimeFor(path), "utf-8", ByteArrayInputStream(bytes))
        }

        private fun missing() = WebResourceResponse(
            "text/plain", "utf-8", 404, "Not found", emptyMap(), ByteArrayInputStream(ByteArray(0))
        )

        private fun mimeFor(path: String): String {
            val ext = path.substringAfterLast('.', "").lowercase()
            return when (ext) {
                "xhtml", "html" -> "application/xhtml+xml"
                "css" -> "text/css"
                else -> MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "application/octet-stream"
            }
        }
    }

    private companion object {
        const val BASE_HOST = "epure.invalid"
        const val BASE_URL = "https://$BASE_HOST/"
        // Side zones: the outer quarter of the width on each side (provisional, to confirm on device).
        const val SIDE_FRACTION = 0.25f
        // Bottom band, in dp, where a short tap opens the command bar: about 2 cm (to measure on device).
        const val BOTTOM_BAND_DP = 126f
        const val TEXT_ZOOM_SETTLE_MS = 150L
        // Space kept above a search occurrence brought into view, in dp.
        const val FOCUS_MARGIN_DP = 48f
        // Four lines of text, at 16 sp with a line spacing of 1.4 (100 % zoom), in dp.
        const val FOCUS_LINES = 4
        const val FOCUS_LINE_DP = 16f * 1.4f
        // A swipe must cover at least this distance, in dp, to change chapter.
        const val SWIPE_DISTANCE_DP = 96f
    }
}
