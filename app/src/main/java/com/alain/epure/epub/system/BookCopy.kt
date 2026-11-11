package com.alain.epure.epub.system

import android.content.Context
import android.net.Uri
import com.alain.epure.epub.domain.BookRefusal
import com.alain.epure.epub.domain.RefusalReason
import java.io.File
import java.util.UUID

/**
 * Temporary copy of a book in the app's private cache (SPEC.md F-1.4, DEC-EE-0018).
 * The copy is deleted when the book is closed, and all copies are deleted at startup.
 */
object BookCopy {

    private fun folder(context: Context): File =
        File(context.cacheDir, "books").apply { mkdirs() }

    /** Copies the book behind [uri] into a new private file and returns it. */
    fun copy(context: Context, uri: Uri): File {
        val target = File(folder(context), UUID.randomUUID().toString() + ".epub")
        val input = context.contentResolver.openInputStream(uri)
            ?: throw BookRefusal(RefusalReason.NOT_ARCHIVE)
        try {
            input.use { source -> target.outputStream().use { source.copyTo(it) } }
        } catch (e: Exception) {
            target.delete()
            throw e
        }
        return target
    }

    fun delete(file: File) {
        file.delete()
    }

    /** Removes every copy left by a previous run (for example after a forced stop). */
    fun deleteAll(context: Context) {
        folder(context).listFiles()?.forEach { it.delete() }
    }
}
