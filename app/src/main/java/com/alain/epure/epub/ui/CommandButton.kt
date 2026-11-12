package com.alain.epure.epub.ui

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.alain.epure.epub.ui.theme.TextPrimary

/**
 * A command of a bottom bar: a short text label, at least 48 dp high (CHARTE A.8).
 * The full name is given to screen readers through [description].
 */
@Composable
fun CommandButton(label: String, description: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .heightIn(min = 48.dp)
            .semantics { contentDescription = description },
        colors = ButtonDefaults.textButtonColors(contentColor = TextPrimary),
    ) {
        Text(text = label)
    }
}
