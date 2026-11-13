package com.alain.epure.epub.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.alain.epure.epub.R
import com.alain.epure.epub.domain.SearchState
import com.alain.epure.epub.domain.TextSearch
import com.alain.epure.epub.ui.theme.Black
import com.alain.epure.epub.ui.theme.TextPrimary
import com.alain.epure.epub.ui.theme.TextSecondary

/**
 * The search panel that replaces the command bar while a search is open (SPEC.md F-11).
 * The keyboard opens on arrival (F-11.1); Enter starts the search. Text only, no animated indicator (F-11.5).
 */
@Composable
fun SearchPanel(
    search: SearchState,
    query: String,
    onQueryChange: (String) -> Unit,
    onStart: (String) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit,
    leftHanded: Boolean,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val currentQuery by rememberUpdatedState(query)
    val keyboardUp = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboard?.show()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Black)
            // Lifted above the keyboard while it is open; otherwise kept above the navigation bar.
            .then(if (keyboardUp) Modifier.imePadding() else Modifier.navigationBarsPadding()),
    ) {
        HorizontalDivider(color = TextSecondary)
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text(stringResource(R.string.search_field_label)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onStart(currentQuery) }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = TextPrimary,
                unfocusedBorderColor = TextSecondary,
                focusedLabelColor = TextSecondary,
                unfocusedLabelColor = TextSecondary,
                cursorColor = TextPrimary,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .focusRequester(focusRequester),
        )

        Text(
            text = statusText(search),
            style = MaterialTheme.typography.labelLarge,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )

        val canPrevious = search.hits.isNotEmpty() && search.current > 0
        val canNext = search.hits.isNotEmpty() && search.current < search.hits.size - 1
        val items: List<@Composable () -> Unit> = listOf(
            { SearchAction(stringResource(R.string.search_previous), canPrevious, onPrevious) },
            { SearchAction(stringResource(R.string.search_next), canNext, onNext) },
            { SearchAction(stringResource(R.string.search_start), !search.running, { onStart(query) }) },
            { SearchAction(stringResource(R.string.action_close), true, onClose) },
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            (if (leftHanded) items.asReversed() else items).forEach { item -> item() }
        }
    }
}

/** The text shown under the field: running, counter « 3 / 17 », « Aucun résultat », or « no text ». */
@Composable
private fun statusText(search: SearchState): String = when {
    search.running -> stringResource(R.string.search_running)
    !search.searched -> ""
    search.noText -> stringResource(R.string.search_no_text)
    search.hits.isEmpty() -> stringResource(R.string.search_none)
    else -> TextSearch.counterLabel(search.current, search.hits.size) ?: ""
}

@Composable
private fun SearchAction(text: String, enabled: Boolean, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            contentColor = TextPrimary,
            disabledContentColor = TextSecondary,
        ),
        modifier = Modifier.heightIn(min = 48.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}
