package com.alain.epure.epub.ui

import android.app.Activity
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets as LayoutWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.alain.epure.epub.R
import com.alain.epure.epub.domain.LinkTarget
import com.alain.epure.epub.domain.TextSizeRules
import com.alain.epure.epub.system.ChapterRenderer
import com.alain.epure.epub.system.OpenBook
import com.alain.epure.epub.system.TapSide
import com.alain.epure.epub.system.newChapterRenderer
import com.alain.epure.epub.ui.theme.Black
import com.alain.epure.epub.ui.theme.TextPrimary
import com.alain.epure.epub.ui.theme.TextSecondary

/**
 * Reading screen: one chapter at a time, shown by the chapter renderer.
 *
 * - A tap on the outer sides changes chapter (DEC-EE-0016); a horizontal swipe does too (DEC-EE-0019).
 * - A short tap in the bottom band opens the command bar (DEC-EE-0021). While it is open, a tap
 *   on the text closes it; « < » and « > » keep it open; a scroll closes it.
 * - The system bars are hidden while reading, and shown while the command bar is open.
 */
@Composable
fun ReaderScreen(
    book: OpenBook,
    index: Int,
    startScrollY: Int,
    navToken: Int,
    notice: Int?,
    progressPercent: Int,
    textStep: Int,
    returnAvailable: Boolean,
    onTap: (TapSide) -> Unit,
    onLink: (LinkTarget) -> Unit,
    onPosition: (chapterIndex: Int, scrollY: Int, fraction: Float) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onReturn: () -> Unit,
    onTextSmaller: () -> Unit,
    onTextLarger: () -> Unit,
    pureWhite: Boolean,
    onToggleWhite: () -> Unit,
) {
    val path = book.book.spine[index]
    val last = book.book.spine.size - 1
    var commandsVisible by remember { mutableStateOf(false) }
    var moreOpen by remember { mutableStateOf(false) }

    val currentOnTap by rememberUpdatedState(onTap)
    val currentOnLink by rememberUpdatedState(onLink)
    val currentOnPosition by rememberUpdatedState(onPosition)
    val currentIndex by rememberUpdatedState(index)

    // Back closes the « Plus » panel first; the book itself is closed by the activity.
    BackHandler(enabled = moreOpen) { moreOpen = false }

    ImmersiveSystemBars(visible = commandsVisible)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                        onTap = { side ->
                            // While the command bar is open, a tap on the text only closes it.
                            if (commandsVisible) commandsVisible = false else currentOnTap(side)
                        },
                        onLink = { target -> currentOnLink(target) },
                        onPosition = { scrollY, fraction -> currentOnPosition(currentIndex, scrollY, fraction) },
                        onBandTap = { commandsVisible = !commandsVisible },
                        onScroll = { commandsVisible = false },
                    ).view
                },
                // Load the chapter only when its chapter or navigation changes, not at every recomposition.
                // The navigation counter makes a return to the same chapter reload it at the saved offset.
                update = { view ->
                    val renderer = view.renderer()
                    renderer.setTextZoom(TextSizeRules.zoomPercent(textStep))
                    val key = "$path#$navToken"
                    if (view.tag != key) {
                        view.tag = key
                        renderer.show(book, path, startScrollY, pureWhite)
                    }
                },
                // Nothing of the book stays in the engine once the screen is left.
                onRelease = { view -> view.renderer().release() },
            )
        }

        if (commandsVisible) {
            // Top: title of the book (charter: title at the top of the command bar).
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Black)
                    .windowInsetsPadding(LayoutWindowInsets.statusBars)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Text(
                    text = book.book.title ?: "",
                    color = TextPrimary,
                )
            }
        }

        if (commandsVisible) {
            if (moreOpen) {
                // Dims the reading screen; a tap outside the panel closes it.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Black.copy(alpha = 0.6f))
                        .clickable { moreOpen = false },
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
            ) {
                if (moreOpen) {
                    MorePanel(
                        textStep = textStep,
                        returnAvailable = returnAvailable,
                        onTextSmaller = onTextSmaller,
                        onTextLarger = onTextLarger,
                        pureWhite = pureWhite,
                        onToggleWhite = onToggleWhite,
                        onReturn = {
                            moreOpen = false
                            commandsVisible = false
                            onReturn()
                        },
                        onClose = { moreOpen = false },
                    )
                }
                CommandBar(
                    progressPercent = progressPercent,
                    canPrevious = index > 0,
                    canNext = index < last,
                    // The bar stays open so that several chapters can be passed in a row.
                    onPrevious = onPrevious,
                    onNext = onNext,
                    onMore = { moreOpen = !moreOpen },
                )
            }
        }
    }
}

/**
 * The command bar at the bottom (DEC-EE-0021): « Table », progression, « < » and « > » for the
 * previous and next chapter, and « Plus » at the far right. Text only, immediate show and hide.
 */
@Composable
private fun CommandBar(
    progressPercent: Int,
    canPrevious: Boolean,
    canNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onMore: () -> Unit,
) {
    val progressDescription = stringResource(R.string.reader_progress_description, progressPercent)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Black)
            .navigationBarsPadding(),
    ) {
        HorizontalDivider(color = TextSecondary)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Table is shown now to check the spacing; it opens the table of contents in session 4a.
            BarAction(
                text = stringResource(R.string.reader_table),
                enabled = false,
                onClick = {},
            )
            // Same box and text style as the actions, so the label sits on the same line.
            Box(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .padding(horizontal = 12.dp)
                    .semantics { contentDescription = progressDescription },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.reader_progress, progressPercent),
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary,
                )
            }
            BarAction(
                text = "<",
                enabled = canPrevious,
                description = stringResource(R.string.reader_previous_chapter),
                onClick = onPrevious,
            )
            BarAction(
                text = ">",
                enabled = canNext,
                description = stringResource(R.string.reader_next_chapter),
                onClick = onNext,
            )
            BarAction(
                text = stringResource(R.string.reader_more),
                enabled = true,
                onClick = onMore,
            )
        }
    }
}

/** A text action of the command bar, at least 48 dp high and wide. */
@Composable
private fun BarAction(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    description: String? = null,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            contentColor = TextPrimary,
            disabledContentColor = TextSecondary,
        ),
        modifier = Modifier
            .heightIn(min = 48.dp)
            .semantics { if (description != null) contentDescription = description },
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/**
 * The « Plus » panel, opened from the command bar: text size and the return to the previous place.
 * Anchored above the bar, text aligned to the left like the rest of the app.
 * « Rechercher » and « Marque-page » arrive in sessions 4a and 4b; settings and « À propos » are on the start screen.
 */
@Composable
private fun MorePanel(
    textStep: Int,
    returnAvailable: Boolean,
    onTextSmaller: () -> Unit,
    onTextLarger: () -> Unit,
    pureWhite: Boolean,
    onToggleWhite: () -> Unit,
    onReturn: () -> Unit,
    onClose: () -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(shape)
            .border(1.dp, TextSecondary, shape)
            .background(Black)
            // Taps on the panel itself are swallowed here, so they do not reach the dimmed screen behind it.
            .pointerInput(Unit) { detectTapGestures { } }
            .padding(vertical = 8.dp),
    ) {
        Text(
            text = stringResource(R.string.reader_text_size, TextSizeRules.sizeSp(textStep)),
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
        PanelAction(stringResource(R.string.reader_text_smaller), enabled = textStep > 0, onTextSmaller)
        PanelAction(
            stringResource(R.string.reader_text_larger),
            enabled = textStep < TextSizeRules.STEPS_SP.lastIndex,
            onTextLarger,
        )
        HorizontalDivider(color = TextSecondary, modifier = Modifier.padding(vertical = 4.dp))
        PanelAction(
            stringResource(if (pureWhite) R.string.reader_white_soften else R.string.reader_white_pure),
            enabled = true,
            onToggleWhite,
        )
        PanelAction(stringResource(R.string.reader_return), enabled = returnAvailable, onReturn)
        HorizontalDivider(color = TextSecondary, modifier = Modifier.padding(vertical = 4.dp))
        PanelAction(stringResource(R.string.action_close), enabled = true, onClose)
    }
}

/** A full-width text row, aligned to the left, at least 48 dp high. Grey when not available. */
@Composable
private fun PanelAction(text: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(text, color = if (enabled) TextPrimary else TextSecondary)
    }
}

/**
 * Shows the status and navigation bars only while the command bar is open (DEC-EE-0021);
 * they are hidden while reading and come back on exit.
 */
@Composable
private fun ImmersiveSystemBars(visible: Boolean) {
    val view = LocalView.current
    // minSdk is 31, so the framework insets controller is always available.
    val controller = remember(view) { (view.context as? Activity)?.window?.insetsController }
    val currentVisible by rememberUpdatedState(visible)
    LaunchedEffect(visible, controller) {
        controller?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (currentVisible) {
            controller?.show(WindowInsets.Type.systemBars())
        } else {
            controller?.hide(WindowInsets.Type.systemBars())
        }
    }
    DisposableEffect(controller) {
        onDispose { controller?.show(WindowInsets.Type.systemBars()) }
    }
}

/** The renderer created by [newChapterRenderer] is the view itself. */
private fun View.renderer(): ChapterRenderer = this as ChapterRenderer
