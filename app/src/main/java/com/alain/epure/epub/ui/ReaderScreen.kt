package com.alain.epure.epub.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.alain.epure.epub.R
import com.alain.epure.epub.system.BookWebView
import com.alain.epure.epub.system.OpenBook
import com.alain.epure.epub.system.TapSide
import com.alain.epure.epub.ui.theme.Black
import com.alain.epure.epub.ui.theme.TextSecondary

/**
 * Reading screen: one chapter at a time, in the WebView engine.
 * A tap on the right side asks for the next chapter, a tap on the left side for the previous one.
 */
@Composable
fun ReaderScreen(book: OpenBook, index: Int, onTap: (TapSide) -> Unit) {
    val path = book.book.spine[index]
    val currentOnTap by rememberUpdatedState(onTap)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .systemBarsPadding()
    ) {
        if (book.mimetypeWarning) {
            Text(
                text = stringResource(R.string.warning_mimetype),
                color = TextSecondary,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            )
        }
        AndroidView(
            // Margins of 24 dp on each side (SPEC.md F-2.3, proposal to confirm).
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp),
            factory = { context -> BookWebView(context) { side -> currentOnTap(side) } },
            // Load the chapter only when its path changes, not at every recomposition.
            update = { view ->
                if (view.tag != path) {
                    view.tag = path
                    view.show(book, path)
                }
            },
        )
    }
}
