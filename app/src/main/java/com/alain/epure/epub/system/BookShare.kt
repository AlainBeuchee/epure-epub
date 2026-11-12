package com.alain.epure.epub.system

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri

/** Sharing a book with another app (SPEC.md F-13.1). The app itself never uses the network. */
object BookShare {

    /**
     * Opens the system share sheet for the book at [uri], with a temporary right to read it.
     * Returns false when the book cannot be shared (SPEC.md F-13.2).
     */
    fun share(context: Context, uri: Uri): Boolean = runCatching {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "application/epub+zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            // The clip data carries the read grant to the receiving app.
            clipData = ClipData.newUri(context.contentResolver, "EPUB", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, null))
        true
    }.getOrDefault(false)
}
