package com.alain.epure.epub.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.alain.epure.epub.R
import com.alain.epure.epub.domain.TocEntry
import com.alain.epure.epub.ui.theme.Black
import com.alain.epure.epub.ui.theme.TextPrimary
import com.alain.epure.epub.ui.theme.TextSecondary

/**
 * « Table des matières » (SPEC.md F-12.1): the chapters of the book, indented by level.
 * A tap goes to the start of the chapter (DEC-EE-0037). Same layout as the settings page.
 */
@Composable
fun TocScreen(
    entries: List<TocEntry>,
    leftHanded: Boolean,
    onOpen: (TocEntry) -> Unit,
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
            text = stringResource(R.string.toc_title),
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
        )
        // The entries scroll in the space left above the divider and the back button.
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            if (entries.isEmpty()) {
                Text(
                    text = stringResource(R.string.toc_none),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
            entries.forEach { entry ->
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clickable(role = Role.Button, onClick = { onOpen(entry) })
                        .padding(start = (16 + 16 * (entry.depth - 1)).dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
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
