package com.alain.epure.epub.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alain.epure.epub.R
import com.alain.epure.epub.domain.Bookmark
import com.alain.epure.epub.ui.theme.Black
import com.alain.epure.epub.ui.theme.TextPrimary
import com.alain.epure.epub.ui.theme.TextSecondary

/**
 * « Marque-pages » (SPEC.md F-10.3): the bookmarks of the book, in reading order.
 * A tap goes to the place; a long press removes the bookmark at once, without confirmation (DEC-0035).
 * Same layout as the settings page: title, rows, bottom bar with « Retour ».
 */
@Composable
fun BookmarksScreen(
    bookmarks: List<Bookmark>,
    leftHanded: Boolean,
    onOpen: (Bookmark) -> Unit,
    onRemove: (Bookmark) -> Unit,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .systemBarsPadding()
    ) {
        Text(
            text = stringResource(R.string.bookmarks_title),
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
        )
        if (bookmarks.isEmpty()) {
            Text(
                text = stringResource(R.string.bookmarks_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }
        // The rows scroll in the space left above the divider and the back button.
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            bookmarks.forEach { bookmark ->
                val name = bookmark.title ?: stringResource(R.string.bookmark_chapter_untitled, bookmark.spineIndex + 1)
                Text(
                    text = bookmark.chapterPercent
                        ?.let { stringResource(R.string.bookmark_label, name, it) }
                        ?: name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .combinedClickable(onClick = { onOpen(bookmark) }, onLongClick = { onRemove(bookmark) })
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
        }
        HorizontalDivider(color = TextSecondary)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = if (leftHanded) Arrangement.Start else Arrangement.End,
        ) {
            CommandButton(
                label = stringResource(R.string.action_back),
                description = stringResource(R.string.action_back),
                onClick = onBack,
            )
        }
    }
}
