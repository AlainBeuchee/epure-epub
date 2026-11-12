package com.alain.epure.epub.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alain.epure.epub.R
import com.alain.epure.epub.domain.ReadingTheme
import com.alain.epure.epub.ui.theme.Black
import com.alain.epure.epub.ui.theme.TextPrimary
import com.alain.epure.epub.ui.theme.TextSecondary

/**
 * Settings (SPEC.md F-16.1), as in Épure Notes (DEC-EE-0029, DEC-EE-0030): a title, one line per
 * setting with its explanation underneath, « À propos » at the end, and a bottom bar with « Retour ».
 * Each tap on the text size or the theme moves to the next value.
 */
@Composable
fun SettingsScreen(
    bookListEnabled: Boolean,
    onToggleBookList: () -> Unit,
    leftHanded: Boolean,
    onToggleLeftHanded: () -> Unit,
    readingTheme: ReadingTheme,
    onNextTheme: () -> Unit,
    textSizeSp: Int,
    onNextTextSize: () -> Unit,
    onOpenAbout: () -> Unit,
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
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
        )
        SettingLine(
            label = stringResource(R.string.settings_book_list) + " : " +
                stringResource(if (bookListEnabled) R.string.settings_on else R.string.settings_off),
            note = stringResource(R.string.settings_book_list_hint),
            onClick = onToggleBookList,
        )
        SettingLine(
            label = stringResource(R.string.settings_hand) + " : " +
                stringResource(if (leftHanded) R.string.settings_hand_left else R.string.settings_hand_right),
            note = stringResource(R.string.settings_hand_hint),
            onClick = onToggleLeftHanded,
        )
        SettingLine(
            label = stringResource(R.string.reader_theme, stringResource(readingTheme.nameRes())),
            note = stringResource(R.string.settings_theme_hint),
            onClick = onNextTheme,
        )
        SettingLine(
            label = stringResource(R.string.reader_text_size, textSizeSp),
            note = stringResource(R.string.settings_text_size_hint),
            onClick = onNextTextSize,
        )
        SettingLine(
            label = stringResource(R.string.settings_about),
            note = null,
            onClick = onOpenAbout,
        )
        Spacer(modifier = Modifier.weight(1f))
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

/** A setting or an action of the list: a tap target of at least 48 dp, with its note underneath. */
@Composable
private fun SettingLine(label: String, note: String?, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .heightIn(min = 48.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
        if (note != null) {
            Text(
                text = note,
                style = TextStyle(fontSize = 16.sp, fontStyle = FontStyle.Italic),
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
