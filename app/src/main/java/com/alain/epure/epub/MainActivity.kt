package com.alain.epure.epub

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.IntentCompat
import androidx.core.net.toUri
import com.alain.epure.epub.domain.BookRefusal
import com.alain.epure.epub.domain.LinkTarget
import com.alain.epure.epub.domain.ReadingPosition
import com.alain.epure.epub.domain.ReadingRules
import com.alain.epure.epub.domain.RecentBook
import com.alain.epure.epub.domain.RecentList
import com.alain.epure.epub.domain.RefusalReason
import com.alain.epure.epub.domain.TextSizeRules
import com.alain.epure.epub.data.AppSettings
import com.alain.epure.epub.data.RecentStore
import com.alain.epure.epub.system.BookCopy
import com.alain.epure.epub.system.BookShare
import com.alain.epure.epub.system.EpubReader
import com.alain.epure.epub.system.OpenBook
import com.alain.epure.epub.system.TapSide
import com.alain.epure.epub.ui.AboutScreen
import com.alain.epure.epub.ui.HomeScreen
import com.alain.epure.epub.ui.ReaderScreen
import com.alain.epure.epub.ui.SettingsScreen
import com.alain.epure.epub.ui.theme.EpureTheme
import java.io.File

/** Delay after the last scroll before the position is written to disk (SPEC.md F-5.2). */
private const val SAVE_DELAY_MS = 1000L

class MainActivity : ComponentActivity() {

    /** A book received from another app (VIEW or SEND), waiting to be opened. */
    private var incoming by mutableStateOf<Uri?>(null)

    /** True when this activity was started by another app: back from the book then returns to it. */
    private var openedFromOutside = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Copies left by a previous run (for example after a forced stop) are removed here.
        BookCopy.deleteAll(this)
        openedFromOutside = intent.action == Intent.ACTION_VIEW || intent.action == Intent.ACTION_SEND
        incoming = incomingUri(intent)
        setContent {
            EpureTheme {
                EpureApp(
                    incoming = incoming,
                    onIncomingConsumed = { incoming = null },
                    openedFromOutside = openedFromOutside,
                    onFinish = { finish() },
                )
            }
        }
    }

    /** The book carried by a VIEW (the file opened) or a SEND (the file shared), or null. */
    private fun incomingUri(intent: Intent): Uri? = when (intent.action) {
        Intent.ACTION_VIEW -> intent.data
        Intent.ACTION_SEND -> IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
        else -> null
    }
}

/** Screens shown when no book is open. */
private enum class Page { HOME, SETTINGS, ABOUT }

/** The app version name, as written in the build (for « À propos »). */
private fun versionNameOf(context: Context): String =
    runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: ""

/** A book being read: its open archive, the private copy it is read from, and where it was read to. */
private class ReadingSession(
    val book: OpenBook,
    val copy: File,
    val uri: String,
    val fileName: String?,
) {
    var position = ReadingPosition(0, 0)
    var progress = 0

    fun toRecent() = RecentBook(
        uri = uri,
        fileName = fileName,
        title = book.book.title,
        position = position,
        progressPercent = progress,
    )
}

@Composable
private fun EpureApp(
    incoming: Uri?,
    onIncomingConsumed: () -> Unit,
    openedFromOutside: Boolean,
    onFinish: () -> Unit,
) {
    val context = LocalContext.current
    val recentUnreadable = stringResource(R.string.recent_unreadable)
    val shareFailed = stringResource(R.string.share_failed)
    val handler = remember { Handler(Looper.getMainLooper()) }
    var session by remember { mutableStateOf<ReadingSession?>(null) }
    var index by remember { mutableIntStateOf(0) }
    var startScrollY by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf<String?>(null) }
    var opening by remember { mutableStateOf(false) }
    // Short message about the last link clicked in the book (string resource), or null.
    var notice by remember { mutableStateOf<Int?>(null) }
    // The books opened, most recent first, shown on the start screen (F-4.1, F-4.2).
    var recents by remember { mutableStateOf(RecentStore.load(context)) }
    var listEnabled by remember { mutableStateOf(AppSettings.isBookListEnabled(context)) }
    var page by remember { mutableStateOf(Page.HOME) }
    val versionName = remember { versionNameOf(context) }
    // Reading state shown by the command bar: progression, text size, and the place left by the last link.
    var progressPercent by remember { mutableIntStateOf(0) }
    var textStep by remember { mutableIntStateOf(AppSettings.textSizeStep(context)) }
    var returnTo by remember { mutableStateOf<ReadingPosition?>(null) }
    var pureWhite by remember { mutableStateOf(AppSettings.isPureWhite(context)) }
    var keepScreenOn by remember { mutableStateOf(AppSettings.isKeepScreenOn(context)) }
    // Counts each navigation, so that a return to a chapter already shown reloads it (see ReaderScreen).
    var navToken by remember { mutableIntStateOf(0) }

    fun goTo(chapterIndex: Int, scrollY: Int) {
        index = chapterIndex
        startScrollY = scrollY
        navToken++
        notice = null
    }

    fun save(reading: ReadingSession) {
        val updated = RecentList.upsert(recents, reading.toRecent(), RecentList.limit(listEnabled))
        RecentStore.save(context, updated)
        recents = updated
    }

    val saveLater = remember {
        Runnable { session?.let { save(it) } }
    }

    fun closeBook() {
        session?.let {
            handler.removeCallbacks(saveLater)
            save(it)
            it.book.close()
            BookCopy.delete(it.copy)
        }
        session = null
        index = 0
        startScrollY = 0
        notice = null
        returnTo = null
        progressPercent = 0
    }

    // Leaving the activity closes the book and removes its copy.
    DisposableEffect(Unit) {
        onDispose { closeBook() }
    }

    // Copies the book, validates it, then shows it at the saved place if it is the same book (F-5.3, F-4.5).
    // Copy and validation run outside the main thread.
    fun openBook(uri: Uri, fromRecent: Boolean) {
        message = null
        opening = true
        Thread {
            val result = runCatching { openCopy(context, uri) }
            handler.post {
                opening = false
                result.onSuccess { reading ->
                    val known = recents.firstOrNull { it.matches(reading.uri, reading.fileName) }
                    val spineSize = reading.book.book.spine.size
                    val place = ReadingRules.restore(known?.position, spineSize)
                    reading.position = place
                    reading.progress = ReadingRules.progressPercent(place.spineIndex, spineSize, 0f)
                    session = reading
                    returnTo = null
                    progressPercent = reading.progress
                    goTo(place.spineIndex, place.scrollY)
                    save(reading)
                }.onFailure { error ->
                    // A book from the list that can no longer be read keeps its entry (F-1.3).
                    message = if (fromRecent && error !is BookRefusal) {
                        recentUnreadable
                    } else {
                        errorText(context, error)
                    }
                }
            }
        }.start()
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        // Asks for a lasting read right on the file; a source that refuses it is still read (copy).
        runCatching {
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        openBook(uri, fromRecent = false)
    }

    // A book sent by another app is opened once, as soon as the screen is shown.
    LaunchedEffect(incoming) {
        val uri = incoming ?: return@LaunchedEffect
        onIncomingConsumed()
        openBook(uri, fromRecent = false)
    }

    // Back from a book returns to the start screen instead of closing the app.
    // A book opened by another app returns to that app instead (F-1.2).
    BackHandler(enabled = session != null) {
        closeBook()
        if (openedFromOutside) onFinish()
    }

    // Settings and « À propos » are reached from the start screen; back returns to it.
    BackHandler(enabled = session == null && page != Page.HOME) {
        page = Page.HOME
    }

    val current = session
    if (current == null) {
        when (page) {
            Page.HOME -> HomeScreen(
                message = message,
                opening = opening,
                recents = recents,
                listEnabled = listEnabled,
                onOpen = { picker.launch(arrayOf("application/epub+zip")) },
                onOpenRecent = { openBook(it.uri.toUri(), fromRecent = true) },
                onRemoveRecent = { book ->
                    // Removes the entry and the right to read the file; the file itself is never deleted (F-4.4).
                    runCatching {
                        context.contentResolver.releasePersistableUriPermission(
                            book.uri.toUri(),
                            Intent.FLAG_GRANT_READ_URI_PERMISSION,
                        )
                    }
                    recents = RecentList.remove(recents, book.uri)
                    RecentStore.save(context, recents)
                },
                onOpenSettings = { page = Page.SETTINGS },
                onOpenAbout = { page = Page.ABOUT },
                onShareRecent = { book ->
                    if (!BookShare.share(context, book.uri.toUri())) message = shareFailed
                },
            )
            Page.SETTINGS -> SettingsScreen(
                bookListEnabled = listEnabled,
                onToggleBookList = {
                    // Turning the list off keeps only the last book (F-4.2).
                    listEnabled = !listEnabled
                    AppSettings.setBookListEnabled(context, listEnabled)
                    recents = RecentList.trim(recents, RecentList.limit(listEnabled))
                    RecentStore.save(context, recents)
                },
                onBack = { page = Page.HOME },
            )
            Page.ABOUT -> AboutScreen(
                versionName = versionName,
                onBack = { page = Page.HOME },
            )
        }
    } else {
        ReaderScreen(
            book = current.book,
            index = index,
            startScrollY = startScrollY,
            navToken = navToken,
            notice = notice,
            progressPercent = progressPercent,
            textStep = textStep,
            returnAvailable = returnTo != null,
            onTap = { side ->
                val last = current.book.book.spine.size - 1
                when (side) {
                    TapSide.RIGHT -> if (index < last) goTo(index + 1, 0)
                    TapSide.LEFT -> if (index > 0) goTo(index - 1, 0)
                    TapSide.CENTER -> Unit
                }
            },
            onLink = { target ->
                when (target) {
                    // A link to another chapter moves the reading there, and remembers the place left
                    // for « Retour au passage » (one step, F-14.1). A link inside the current
                    // chapter (fragment only) is not followed yet: the text stays where it is.
                    is LinkTarget.Chapter -> {
                        if (target.index != index) {
                            returnTo = current.position
                            goTo(target.index, 0)
                        }
                    }
                    LinkTarget.External -> notice = R.string.link_external
                    LinkTarget.Unavailable -> notice = R.string.link_unavailable
                }
            },
            onPosition = { chapterIndex, scrollY, fraction ->
                session?.let { reading ->
                    reading.position = ReadingPosition(chapterIndex, scrollY)
                    reading.progress = ReadingRules.progressPercent(
                        chapterIndex,
                        reading.book.book.spine.size,
                        fraction,
                    )
                    progressPercent = reading.progress
                    handler.removeCallbacks(saveLater)
                    handler.postDelayed(saveLater, SAVE_DELAY_MS)
                }
            },
            onPrevious = { if (index > 0) goTo(index - 1, 0) },
            onNext = { if (index < current.book.book.spine.size - 1) goTo(index + 1, 0) },
            onReturn = {
                returnTo?.let { place ->
                    returnTo = null
                    goTo(place.spineIndex, place.scrollY)
                }
            },
            onTextSmaller = {
                textStep = TextSizeRules.smaller(textStep)
                AppSettings.setTextSizeStep(context, textStep)
            },
            onTextLarger = {
                textStep = TextSizeRules.larger(textStep)
                AppSettings.setTextSizeStep(context, textStep)
            },
            keepScreenOn = keepScreenOn,
            onToggleKeepScreenOn = {
                keepScreenOn = !keepScreenOn
                AppSettings.setKeepScreenOn(context, keepScreenOn)
            },
            onShare = {
                if (!BookShare.share(context, current.uri.toUri())) notice = R.string.share_failed
            },
            pureWhite = pureWhite,
            onToggleWhite = {
                pureWhite = !pureWhite
                AppSettings.setPureWhite(context, pureWhite)
                // The chapter is shown again in the new colour, at the same place.
                goTo(index, current.position.scrollY)
            },
        )
    }
}

/** Copies the book, then validates it. On failure the copy is removed. */
private fun openCopy(context: Context, uri: Uri): ReadingSession {
    val copy = BookCopy.copy(context, uri)
    return try {
        ReadingSession(
            book = EpubReader.open(copy),
            copy = copy,
            uri = uri.toString(),
            fileName = displayNameOf(context, uri),
        )
    } catch (e: Throwable) {
        BookCopy.delete(copy)
        throw e
    }
}

/** The file name the source gives to [uri], or null if it gives none. */
private fun displayNameOf(context: Context, uri: Uri): String? = runCatching {
    context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) cursor.getString(0) else null
    }
}.getOrNull()

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
