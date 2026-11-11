package com.alain.epure.epub.domain

/** Where a link clicked in a book leads. */
sealed interface LinkTarget {
    /** A chapter of the book, by its index in the reading order, with an optional fragment (F-14.1). */
    data class Chapter(val index: Int, val fragment: String?) : LinkTarget

    /** Any address outside the book: web, mail, file, or a different host (F-14.2). Never opened. */
    data object External : LinkTarget

    /** An address that points inside the book but to nothing readable, or an unsafe path. */
    data object Unavailable : LinkTarget
}

/** Pure rules for links clicked in a book. No Android dependency. */
object LinkPolicy {

    /**
     * Classifies the [url] of a clicked link.
     *
     * @param baseHost the only host under which the book's files are served (for example "epure.invalid")
     * @param spine the reading order, as entry paths inside the archive
     */
    fun classify(url: String, baseHost: String, spine: List<String>): LinkTarget {
        val prefix = "https://$baseHost/"
        if (!url.startsWith(prefix)) return LinkTarget.External
        val rest = url.removePrefix(prefix)
        val fragment = rest.substringAfter('#', "").takeIf { it.isNotEmpty() }
        val path = rest.substringBefore('#').substringBefore('?')
        if (path.isEmpty()) return LinkTarget.Unavailable
        val entry = ArchivePolicy.decodePercent(path) ?: return LinkTarget.Unavailable
        if (!ArchivePolicy.isSafeEntryName(entry)) return LinkTarget.Unavailable
        val index = spine.indexOf(entry)
        return if (index < 0) LinkTarget.Unavailable else LinkTarget.Chapter(index, fragment)
    }
}
