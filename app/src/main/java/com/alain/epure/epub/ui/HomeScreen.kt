package com.alain.epure.epub.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alain.epure.epub.R
import com.alain.epure.epub.ui.theme.Black
import com.alain.epure.epub.ui.theme.TextPrimary

/** Start screen: one message and one action, the choice of a file. */
@Composable
fun HomeScreen(message: String?, opening: Boolean, onOpen: () -> Unit) {
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
    }
}
