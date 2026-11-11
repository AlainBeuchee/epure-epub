package com.alain.epure.epub.system

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.GestureDetector
import android.view.MotionEvent
import android.webkit.CookieManager
import android.webkit.MimeTypeMap
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.alain.epure.epub.domain.ArchivePolicy
import java.io.ByteArrayInputStream

/** Where a single tap landed, as a fraction of the screen width (DEC-EE-0016). */
enum class TapSide { LEFT, RIGHT, CENTER }

/**
 * The rendering engine: a WebView configured as in DEC-EE-0015 (SPEC.md S-1 to S-7).
 * It shows one chapter of an open book. Only files of that archive can be loaded.
 */
@SuppressLint("ViewConstructor")
class BookWebView(context: Context, private val onTap: (TapSide) -> Unit) : WebView(context) {

    private var book: OpenBook? = null

    private val taps = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            val width = width.takeIf { it > 0 } ?: return false
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
    })

    init {
        setBackgroundColor(Color.BLACK)
        settings.apply {
            javaScriptEnabled = false
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

    /** Shows the chapter at [chapterPath] of [openBook] from the top. */
    fun show(openBook: OpenBook, chapterPath: String) {
        book = openBook
        val bytes = openBook.readEntry(chapterPath)
        if (bytes == null) {
            loadDataWithBaseURL(BASE_URL, "", "text/html", "utf-8", null)
            return
        }
        loadDataWithBaseURL(
            BASE_URL + chapterPath,
            withReadingStyle(bytes.toString(Charsets.UTF_8)),
            "text/html",
            "utf-8",
            null,
        )
        scrollTo(0, 0)
    }

    /**
     * Imposes the reading colours and font of the app on the book (DEC-EE-0011, F-3.3):
     * black background, light text, system sans-serif. Images keep their own colours.
     * The rule is added at the end of the chapter, so it wins over most of the book's own styles.
     */
    private fun withReadingStyle(html: String): String {
        val closing = Regex("</body>", RegexOption.IGNORE_CASE).find(html)
        return if (closing != null) {
            html.substring(0, closing.range.first) + READING_STYLE + html.substring(closing.range.first)
        } else {
            html + READING_STYLE
        }
    }

    /** Forgets the book: nothing of it stays loaded in the WebView. */
    fun release() {
        book = null
        loadUrl("about:blank")
    }

    private inner class BookClient : WebViewClient() {

        // Links are never followed here: internal links are added later (session 1c).
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean = true

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
        const val READING_STYLE =
            "<style>html body, html body * { background-color: #000000 !important; color: #ffffff !important; " +
                "font-family: sans-serif !important; text-align: left !important; } " +
                "html body img, html body svg, html body image { background-color: transparent !important; }</style>"
    }
}
