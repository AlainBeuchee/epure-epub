package com.alain.epure.epub.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alain.epure.epub.R
import com.alain.epure.epub.domain.RecentBook
import com.alain.epure.epub.ui.theme.Black
import com.alain.epure.epub.ui.theme.TextPrimary

/**
 * Start screen: a message, the choice of a file, and the last book opened (SPEC.md F-4.1).
 * A long press on the last book opens a menu with « Retirer de la liste » (F-4.4).
 */
@Composable
fun HomeScreen(
    message: String?,
    opening: Boolean,
    recent: RecentBook?,
    onOpen: () -> Unit,
    onOpenRecent: () -> Unit,
    onRemoveRecent: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .systemBarsPadding()
            .padding(16.dp)
    ) {
        Text(
            text = when {
                opening -> stringResource(R.string.home_opening)
                message != null -> message
                else -> stringResource(R.string.home_start)
            },
            color = TextPrimary,
        )
        TextButton(
            onClick = onOpen,
            enabled = !opening,
            modifier = Modifier.heightIn(min = 48.dp),
        ) {
            Text(stringResource(R.string.home_open_file), color = TextPrimary)
        }
        if (recent != null && !opening) {
            val title = recent.title ?: recent.fileName ?: stringResource(R.string.recent_untitled)
            Text(
                text = stringResource(R.string.recent_progress, title, recent.progressPercent),
                color = TextPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .combinedClickable(onClick = onOpenRecent, onLongClick = { menuOpen = true })
                    .padding(vertical = 12.dp),
            )
        }
    }

    if (menuOpen && recent != null) {
        val title = recent.title ?: recent.fileName ?: stringResource(R.string.recent_untitled)
        AlertDialog(
            onDismissRequest = { menuOpen = false },
            containerColor = Black,
            title = { Text(title, color = TextPrimary) },
            text = null,
            confirmButton = {
                TextButton(
                    onClick = {
                        menuOpen = false
                        onRemoveRecent()
                    },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text(stringResource(R.string.recent_remove), color = TextPrimary)
                }
            },
        )
    }
}
