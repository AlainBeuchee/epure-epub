package com.alain.epure.epub.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alain.epure.epub.R
import com.alain.epure.epub.ui.theme.Black
import com.alain.epure.epub.ui.theme.TextPrimary
import com.alain.epure.epub.ui.theme.TextSecondary

/** « À propos » (SPEC.md F-16.2): name, version, licence, repository, privacy and security, licences. */
@Composable
fun AboutScreen(versionName: String, onBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(stringResource(R.string.app_name), color = TextPrimary)
        Text(stringResource(R.string.about_version, versionName), color = TextSecondary)
        Text(stringResource(R.string.about_license), color = TextSecondary)
        Text(
            text = stringResource(R.string.about_repository),
            color = TextPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clickable { runCatching { uriHandler.openUri(REPOSITORY_URL) } }
                .padding(vertical = 12.dp),
        )

        Text(stringResource(R.string.about_privacy_title), color = TextPrimary, modifier = Modifier.padding(top = 16.dp))
        Text(stringResource(R.string.about_privacy_text), color = TextSecondary)

        Text(stringResource(R.string.about_licenses_title), color = TextPrimary, modifier = Modifier.padding(top = 16.dp))
        Text(stringResource(R.string.about_licenses_text), color = TextSecondary)

        // A plain text row, not a TextButton: its minimum width centres the text and pushes it right.
        Text(
            text = stringResource(R.string.action_back),
            color = TextPrimary,
            modifier = Modifier
                .heightIn(min = 48.dp)
                .clickable(role = Role.Button, onClick = onBack)
                .padding(vertical = 12.dp),
        )
    }
}

private const val REPOSITORY_URL = "https://github.com/AlainBeuchee/epure-epub"
