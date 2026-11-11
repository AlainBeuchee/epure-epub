package com.alain.epure.epub.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.alain.epure.epub.ui.theme.TextSecondary

/**
 * Settings list (SPEC.md F-16.1). A row is a tap target of at least 48 dp, with its state written in text.
 */
@Composable
fun SettingsScreen(
    bookListEnabled: Boolean,
    onToggleBookList: () -> Unit,
    onOpenAbout: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .systemBarsPadding()
            .padding(16.dp)
    ) {
        Text(stringResource(R.string.settings_title), color = TextPrimary)
        SettingRow(
            label = stringResource(R.string.settings_book_list),
            value = stringResource(if (bookListEnabled) R.string.settings_on else R.string.settings_off),
            onClick = onToggleBookList,
        )
        SettingRow(
            label = stringResource(R.string.settings_about),
            value = null,
            onClick = onOpenAbout,
        )
        TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(stringResource(R.string.action_back), color = TextPrimary)
        }
    }
}

@Composable
private fun SettingRow(label: String, value: String?, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
    ) {
        Text(label, color = TextPrimary)
        if (value != null) {
            Text(" : $value", color = TextSecondary)
        }
    }
}
