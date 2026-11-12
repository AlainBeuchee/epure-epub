package com.alain.epure.epub.domain

/** The reading order of a book, as paths of entries inside the archive. */
data class BookPackage(
    val title: String?,
    val spine: List<String>,
    /** The table of contents, empty if the book has none or it cannot be read (F-12.2). */
    val toc: List<TocEntry> = emptyList(),
)

/** Pure logic that turns the parsed package document into archive paths. No Android dependency. */
object PackageBuilder {

    /**
     * @param opfPath path of the package document inside the archive (e.g. "OEBPS/content.opf")
     * @param manifest item id to href, as written in the package document
     * @param spineIds ids of the reading order, in order
     * @param entryNames names of all entries of the archive
     *
     * Spine items that are missing from the manifest, point outside the archive, or do not
     * exist in the archive are dropped. Throws [BookRefusal] if nothing readable remains.
     */
    fun build(
        opfPath: String,
        title: String?,
        manifest: Map<String, String>,
        spineIds: List<String>,
        entryNames: Set<String>,
    ): BookPackage {
        val baseDir = opfPath.substringBeforeLast('/', "")
        val spine = spineIds
            .mapNotNull { id -> manifest[id] }
            .mapNotNull { href -> ArchivePolicy.resolveHref(baseDir, href) }
            .filter { it in entryNames }
            .distinct()
        if (spine.isEmpty()) throw BookRefusal(RefusalReason.NO_TEXT)
        return BookPackage(title = title?.trim()?.takeIf { it.isNotEmpty() }, spine = spine)
    }
}
