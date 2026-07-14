package com.iffly.compose.markdown.multiplatform.render

/**
 * Stores inline views produced while building annotated Markdown text.
 *
 * Keeping ID allocation and storage behind this type allows the rendering pipeline to add
 * controlled injection behavior without passing the backing map to node string builders.
 */
class MarkdownInlineViewMap private constructor(
    private val inlineViews: MutableMap<String, MarkdownInlineView>,
) : MutableMap<String, MarkdownInlineView> by inlineViews {
    private val occurrenceCache = mutableMapOf<String, Int>()

    constructor() : this(mutableMapOf())

    /**
     * Resolves the ID used to register new inline content.
     *
     * Existing IDs receive a deterministic occurrence suffix unless [overwrite] is enabled.
     */
    fun resolveId(
        id: String,
        overwrite: Boolean = false,
    ): String {
        if (overwrite) return id
        if (id !in inlineViews) {
            occurrenceCache.remove(id)
            return id
        }

        var occurrence = occurrenceCache[id] ?: 1
        var candidate = "${id}_$occurrence"
        while (candidate in inlineViews) {
            occurrence++
            candidate = "${id}_$occurrence"
        }
        occurrenceCache[id] = occurrence
        return candidate
    }

    override fun equals(other: Any?): Boolean = inlineViews == other

    override fun hashCode(): Int = inlineViews.hashCode()

    override fun toString(): String = inlineViews.toString()
}
