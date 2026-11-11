package com.alain.epure.epub

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.alain.epure.epub.domain.BookRefusal
import com.alain.epure.epub.domain.RefusalReason
import com.alain.epure.epub.system.BookCopy
import com.alain.epure.epub.system.EpubReader
import com.alain.epure.epub.system.OpenBook
import com.alain.epure.epub.system.TapSide
import com.alain.epure.epub.ui.HomeScreen
import com.alain.epure.epub.ui.ReaderScreen
import com.alain.epure.epub.ui.theme.EpureTheme
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Copies left by a previous run (for example after a forced stop) are removed here.
        BookCopy.deleteAll(this)
        setContent {
            EpureTheme {
                EpureApp()
            }
        }
    }
}

/** A book being read: its open archive and the private copy it is read from. */
private class ReadingSession(val book: OpenBook, val copy: File)

@Composable
private fun EpureApp() {
    val context = LocalContext.current
    val handler = remember { Handler(Looper.getMainLooper()) }
    var session by remember { mutableStateOf<ReadingSession?>(null) }
    var index by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf<String?>(null) }
    var opening by remember { mutableStateOf(false) }

    fun closeBook() {
        session?.let {
            it.book.close()
            BookCopy.delete(it.copy)
        }
        session = null
        index = 0
    }

    // Leaving the activity closes the book and removes its copy.
    DisposableEffect(Unit) {
        onDispose { closeBook() }
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        // Asks for a lasting read right on the file; a source that refuses it is still read (copy).
        runCatching {
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        message = null
        opening = true
        // Copy and validation take time: they run outside the main thread.
        Thread {
            val result = runCatching { openCopy(context, uri) }
            handler.post {
                opening = false
                result.onSuccess {
                    session = it
                    index = 0
                }.onFailure {
                    message = errorText(context, it)
                }
            }
        }.start()
    }

    // Back from a book returns to the start screen instead of closing the app.
    BackHandler(enabled = session != null) { closeBook() }

    val current = session
    if (current == null) {
        HomeScreen(
            message = message,
            opening = opening,
            onOpen = { picker.launch(arrayOf("application/epub+zip")) },
        )
    } else {
        ReaderScreen(
            book = current.book,
            index = index,
            onTap = { side ->
                val last = current.book.book.spine.size - 1
                when (side) {
                    TapSide.RIGHT -> if (index < last) index++
                    TapSide.LEFT -> if (index > 0) index--
                    TapSide.CENTER -> Unit
                }
            },
        )
    }
}

/** Copies the book, then validates it. On failure the copy is removed. */
private fun openCopy(context: Context, uri: Uri): ReadingSession {
    val copy = BookCopy.copy(context, uri)
    return try {
        ReadingSession(EpubReader.open(copy), copy)
    } catch (e: Throwable) {
        BookCopy.delete(copy)
        throw e
    }
}

private fun errorText(context: Context, error: Throwable): String {
    val id = when ((error as? BookRefusal)?.reason) {
        RefusalReason.NOT_ARCHIVE, RefusalReason.NO_PACKAGE -> R.string.error_not_epub
        RefusalReason.UNSAFE_PATH -> R.string.error_unsafe_path
        RefusalReason.TOO_MANY_ENTRIES,
        RefusalReason.ENTRY_TOO_LARGE,
        RefusalReason.TOTAL_TOO_LARGE,
        RefusalReason.COMPRESSION_RATIO_TOO_HIGH -> R.string.error_too_large
        RefusalReason.NO_TEXT -> R.string.error_no_text
        null -> R.string.error_file_unreadable
    }
    return context.getString(id)
}
