package com.alain.epure.epub.domain

/** An entry of the table of contents, as written in the book: a title, its target and its depth (1 = top). */
data class RawTocEntry(val title: String, val href: String, val depth: Int)

/** An entry of the table, resolved to a chapter of the reading order (SPEC.md F-12.1). */
data class TocEntry(val title: String, val spineIndex: Int, val depth: Int)

/** Rules for the table of contents (SPEC.md F-12). Pure logic, no Android dependency. */
object TableOfContents {

    /**
     * Keeps the entries that point into the book, in their order. Each one goes to the start of its
     * chapter: a target inside a chapter (`#anchor`) is not resolved, since the engine runs no script (DEC-EE-0037).
     * Entries whose target is outside the archive or not in the reading order are ignored (F-12.3).
     *
     * @param baseDir folder of the table document: relative targets are read from there
     * @param spine paths of the chapters, in reading order
     */
    /** Title of the first table entry that goes to the chapter [spineIndex], or null if there is none. */
    fun titleOf(toc: List<TocEntry>, spineIndex: Int): String? =
        toc.firstOrNull { it.spineIndex == spineIndex }?.title

    fun resolve(raw: List<RawTocEntry>, baseDir: String, spine: List<String>): List<TocEntry> =
        raw.mapNotNull { entry ->
            val path = ArchivePolicy.resolveHref(baseDir, entry.href) ?: return@mapNotNull null
            val index = spine.indexOf(path)
            if (index < 0) return@mapNotNull null
            TocEntry(
                title = entry.title.trim().ifEmpty { "Chapitre ${index + 1}" },
                spineIndex = index,
                depth = entry.depth.coerceAtLeast(1),
            )
        }
}
