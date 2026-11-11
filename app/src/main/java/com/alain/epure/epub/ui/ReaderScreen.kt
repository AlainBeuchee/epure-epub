package com.alain.epure.epub.ui

import android.app.Activity
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.alain.epure.epub.R
import com.alain.epure.epub.domain.LinkTarget
import com.alain.epure.epub.system.ChapterRenderer
import com.alain.epure.epub.system.OpenBook
import com.alain.epure.epub.system.TapSide
import com.alain.epure.epub.system.newChapterRenderer
import com.alain.epure.epub.ui.theme.Black
import com.alain.epure.epub.ui.theme.TextSecondary

/**
 * Reading screen: one chapter at a time, shown by the chapter renderer.
 * A tap on the right side asks for the next chapter, a tap on the left side for the previous one.
 * The system bars are hidden while the book is open (immersive mode, SPEC.md F-2.4).
 */
@Composable
fun ReaderScreen(
    book: OpenBook,
    index: Int,
    startScrollY: Int,
    notice: Int?,
    onTap: (TapSide) -> Unit,
    onLink: (LinkTarget) -> Unit,
    onPosition: (chapterIndex: Int, scrollY: Int, fraction: Float) -> Unit,
) {
    val path = book.book.spine[index]
    val currentOnTap by rememberUpdatedState(onTap)
    val currentOnLink by rememberUpdatedState(onLink)
    val currentOnPosition by rememberUpdatedState(onPosition)
    val currentIndex by rememberUpdatedState(index)
    ImmersiveSystemBars()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        if (book.mimetypeWarning) {
            Text(
                text = stringResource(R.string.warning_mimetype),
                color = TextSecondary,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            )
        }
        if (notice != null) {
            Text(
                text = stringResource(notice),
                color = TextSecondary,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            )
        }
        AndroidView(
            // Margins of 16 dp on each side (SPEC.md F-2.3; reduced from 24 dp after device test).
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            factory = { context ->
                newChapterRenderer(
                    context,
                    onTap = { side -> currentOnTap(side) },
                    onLink = { target -> currentOnLink(target) },
                    onPosition = { scrollY, fraction -> currentOnPosition(currentIndex, scrollY, fraction) },
                ).view
            },
            // Load the chapter only when its path changes, not at every recomposition.
            // Only the first chapter shown uses the saved offset; later chapters start at the top.
            update = { view ->
                if (view.tag != path) {
                    val first = view.tag == null
                    view.tag = path
                    view.renderer().show(book, path, if (first) startScrollY else 0)
                }
            },
            // Nothing of the book stays in the engine once the screen is left.
            onRelease = { view -> view.renderer().release() },
        )
    }
}

/** Hides the status and navigation bars while the reader is shown; they come back on exit. */
@Composable
private fun ImmersiveSystemBars() {
    val view = LocalView.current
    DisposableEffect(view) {
        // minSdk is 31, so the framework insets controller is always available.
        val controller = (view.context as? Activity)?.window?.insetsController
        controller?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller?.hide(WindowInsets.Type.systemBars())
        onDispose { controller?.show(WindowInsets.Type.systemBars()) }
    }
}

/** The renderer created by [newChapterRenderer] is the view itself. */
private fun View.renderer(): ChapterRenderer = this as ChapterRenderer
