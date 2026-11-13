package com.alain.epure.epub.ui

import android.app.Activity
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import com.alain.epure.epub.domain.ReadingTheme
import com.alain.epure.epub.domain.SearchState
import com.alain.epure.epub.domain.TextSizeRules
import com.alain.epure.epub.system.ChapterHighlight
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
 * - A short tap in the bottom band, or a swipe down anywhere, opens the command bar (DEC-EE-0021, DEC-EE-0028).
 *   While it is open, a tap on the text closes it; « < » and « > » keep it open; scrolling does not change it.
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
    onNextTextSize: () -> Unit,
    leftHanded: Boolean,
    tocAvailable: Boolean,
    onTable: () -> Unit,
    onAddBookmark: () -> Unit,
    onOpenBookmarks: () -> Unit,
    theme: ReadingTheme,
    onNextTheme: () -> Unit,
    keepScreenOn: Boolean,
    onToggleKeepScreenOn: () -> Unit,
    onShare: () -> Unit,
    search: SearchState,
    onOpenSearch: () -> Unit,
    onSearchStart: (String) -> Unit,
    onSearchPrevious: () -> Unit,
    onSearchNext: () -> Unit,
    onCloseSearch: () -> Unit,
) {
    val path = book.book.spine[index]
    val last = book.book.spine.size - 1
    // The search occurrence to mark in this chapter, if the one shown is here (SPEC.md F-11.4).
    val highlight = search.activeHitInChapter(index)?.let { ChapterHighlight(search.query, it) }
    var commandsVisible by remember { mutableStateOf(false) }
    var moreOpen by remember { mutableStateOf(false) }
    // The « * » request asking to add a bookmark here (DEC-EE-0040).
    var bookmarkAskOpen by remember { mutableStateOf(false) }
    // The text typed in the search panel; kept while the reader stays open (SPEC.md F-11.1).
    var searchQuery by remember { mutableStateOf("") }

    val currentOnTap by rememberUpdatedState(onTap)
    val currentOnLink by rememberUpdatedState(onLink)
    val currentOnPosition by rememberUpdatedState(onPosition)
    val currentIndex by rememberUpdatedState(index)
    val searchOpen by rememberUpdatedState(search.open)

    // Back closes the « Plus » panel first; the book itself is closed by the activity.
    BackHandler(enabled = moreOpen || bookmarkAskOpen) {
        moreOpen = false
        bookmarkAskOpen = false
    }
    // Back in the search panel stops the search and closes the panel (SPEC.md F-11.5).
    BackHandler(enabled = search.open) { onCloseSearch() }
    // Hiding the command bar also closes the search (the search panel lives in the bar).
    LaunchedEffect(commandsVisible) {
        if (!commandsVisible && searchOpen) onCloseSearch()
    }

    ImmersiveSystemBars(visible = commandsVisible)
    KeepScreenOn(on = keepScreenOn)

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
                // Margins of 10 dp on each side (SPEC.md F-2.3; DEC-EE-0027, reduced from 16 dp by the owner).
                modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
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
                        onRevealBar = { commandsVisible = true },
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
                        renderer.show(book, path, startScrollY, theme, highlight)
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
            if (moreOpen || bookmarkAskOpen) {
                // Dims the reading screen; a tap outside the panel closes it.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Black.copy(alpha = 0.6f))
                        .clickable {
                            moreOpen = false
                            bookmarkAskOpen = false
                        },
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
            ) {
                if (bookmarkAskOpen) {
                    BookmarkAskPanel(
                        onAdd = {
                            bookmarkAskOpen = false
                            onAddBookmark()
                        },
                        onOpenBookmarks = {
                            bookmarkAskOpen = false
                            onOpenBookmarks()
                        },
                        onSearch = {
                            bookmarkAskOpen = false
                            onOpenSearch()
                        },
                        returnAvailable = returnAvailable,
                        onShare = {
                            bookmarkAskOpen = false
                            onShare()
                        },
                        onReturn = {
                            bookmarkAskOpen = false
                            commandsVisible = false
                            onReturn()
                        },
                        onCancel = { bookmarkAskOpen = false },
                    )
                }
                if (moreOpen) {
                    MorePanel(
                        textStep = textStep,
                        onNextTextSize = onNextTextSize,
                        theme = theme,
                        onNextTheme = onNextTheme,
                        keepScreenOn = keepScreenOn,
                        onToggleKeepScreenOn = onToggleKeepScreenOn,
                        onClose = { moreOpen = false },
                    )
                }
                if (search.open) {
                    // The search panel takes the place of the command bar while a search is open (SPEC.md F-11).
                    SearchPanel(
                        search = search,
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onStart = onSearchStart,
                        onPrevious = onSearchPrevious,
                        onNext = onSearchNext,
                        onClose = onCloseSearch,
                        leftHanded = leftHanded,
                    )
                } else {
                    CommandBar(
                        progressPercent = progressPercent,
                        canPrevious = index > 0,
                        canNext = index < last,
                        // The bar stays open so that several chapters can be passed in a row.
                        onPrevious = onPrevious,
                        onNext = onNext,
                        onMore = { moreOpen = !moreOpen },
                        onAskBookmark = {
                            moreOpen = false
                            bookmarkAskOpen = true
                        },
                        leftHanded = leftHanded,
                        tocAvailable = tocAvailable,
                        onTable = onTable,
                    )
                }
            }
        }
    }
}

/**
 * The command bar at the bottom (DEC-EE-0021): « Table », progression, « < » and « > » for the
 * previous and next chapter, « * » to ask for a bookmark (DEC-EE-0040), and « Plus » at the far right.
 * Text only, immediate show and hide.
 */
@Composable
private fun CommandBar(
    progressPercent: Int,
    canPrevious: Boolean,
    canNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onMore: () -> Unit,
    onAskBookmark: () -> Unit,
    leftHanded: Boolean,
    tocAvailable: Boolean,
    onTable: () -> Unit,
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
            // Left-handed order (DEC-EE-0032) reverses the items.
            val items: List<@Composable () -> Unit> = listOf(
                {
                    // Greyed when the book has no table of contents; a tap then says so (F-12.2).
                    TextButton(
                        onClick = onTable,
                        modifier = Modifier.heightIn(min = 48.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (tocAvailable) TextPrimary else TextSecondary,
                        ),
                    ) {
                        Text(stringResource(R.string.reader_table), style = MaterialTheme.typography.labelLarge)
                    }
                },
                {
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
                },
                {
                    BarAction(
                        text = "<",
                        enabled = canPrevious,
                        description = stringResource(R.string.reader_previous_chapter),
                        onClick = onPrevious,
                    )
                },
                {
                    BarAction(
                        text = ">",
                        enabled = canNext,
                        description = stringResource(R.string.reader_next_chapter),
                        onClick = onNext,
                    )
                },
                {
                    BarAction(
                        text = stringResource(R.string.reader_bookmark_ask),
                        enabled = true,
                        description = stringResource(R.string.bookmark_add),
                        onClick = onAskBookmark,
                    )
                },
                {
                    BarAction(
                        text = stringResource(R.string.reader_more),
                        enabled = true,
                        onClick = onMore,
                    )
                },
            )
            (if (leftHanded) items.asReversed() else items).forEach { item -> item() }
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
 * The window opened by « * » (DEC-EE-0040, DEC-EE-0041): asks before adding a bookmark at the current
 * place, and gives access to the list of bookmarks. Same panel as « Plus », anchored above the bar.
 */
@Composable
private fun BookmarkAskPanel(
    onAdd: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onSearch: () -> Unit,
    returnAvailable: Boolean,
    onShare: () -> Unit,
    onReturn: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Black)
            .pointerInput(Unit) { detectTapGestures { } }
            .padding(vertical = 8.dp),
    ) {
        PanelAction(stringResource(R.string.bookmark_ask_yes), enabled = true, onAdd)
        PanelAction(stringResource(R.string.bookmarks_list), enabled = true, onOpenBookmarks)
        PanelAction(stringResource(R.string.search_entry), enabled = true, onSearch)
        PanelAction(stringResource(R.string.reader_return), enabled = returnAvailable, onReturn)
        PanelAction(stringResource(R.string.action_share), enabled = true, onShare)
        PanelAction(stringResource(R.string.action_cancel), enabled = true, onCancel)
    }
}

/**
 * The « Plus » panel, opened from the command bar: text size and the return to the previous place.
 * Anchored above the bar, text aligned to the left like the rest of the app.
 * Bookmarks and search are opened from « * » (DEC-EE-0041, DEC-EE-0042); settings and « À propos » are on the start screen.
 */
@Composable
private fun MorePanel(
    textStep: Int,
    onNextTextSize: () -> Unit,
    theme: ReadingTheme,
    onNextTheme: () -> Unit,
    keepScreenOn: Boolean,
    onToggleKeepScreenOn: () -> Unit,
    onClose: () -> Unit,
) {
    // Full width, no outline, same sizes as the settings list (DEC-EE-0034).
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Black)
            // Taps on the panel itself are swallowed here, so they do not reach the dimmed screen behind it.
            .pointerInput(Unit) { detectTapGestures { } }
            .padding(vertical = 8.dp),
    ) {
        // Each tap moves to the next size: 12, 14, … 24, then back to 12 sp (DEC-EE-0033).
        PanelAction(
            stringResource(R.string.reader_text_size, TextSizeRules.sizeSp(textStep)),
            enabled = true,
            onNextTextSize,
        )
        PanelAction(
            stringResource(R.string.reader_theme, stringResource(theme.nameRes())),
            enabled = true,
            onNextTheme,
        )
        PanelAction(
            stringResource(if (keepScreenOn) R.string.reader_screen_on_yes else R.string.reader_screen_on_no),
            enabled = true,
            onToggleKeepScreenOn,
        )
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
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled) TextPrimary else TextSecondary,
        )
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

/**
 * Keeps the screen on while a book is open, when the setting is on (SPEC.md F-9).
 * No wake lock permission is needed: this is a window flag.
 */
@Composable
private fun KeepScreenOn(on: Boolean) {
    val view = LocalView.current
    DisposableEffect(view, on) {
        val window = (view.context as? Activity)?.window
        if (on) window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }
}

/** Name of a reading theme, as shown in « Plus ». */
internal fun ReadingTheme.nameRes(): Int = when (this) {
    ReadingTheme.DAY -> R.string.theme_day
    ReadingTheme.SEPIA -> R.string.theme_sepia
    ReadingTheme.SLATE -> R.string.theme_slate
    ReadingTheme.AMOLED -> R.string.theme_amoled
    ReadingTheme.NOIR -> R.string.theme_noir
}

/** The renderer created by [newChapterRenderer] is the view itself. */
private fun View.renderer(): ChapterRenderer = this as ChapterRenderer
