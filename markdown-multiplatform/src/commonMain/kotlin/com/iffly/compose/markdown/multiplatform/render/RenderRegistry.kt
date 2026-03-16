package com.iffly.compose.markdown.multiplatform.render

import org.intellij.markdown.IElementType

/**
 * Registry that maps markdown element types to their corresponding renderers and inline string builders.
 * Provides lookup for both block-level renderers ([IBlockRenderer]) and inline node string builders
 * ([IInlineNodeStringBuilder]), as well as optional custom content and text renderers.
 *
 * @property markdownContentRenderer Optional custom renderer for top-level markdown content.
 * @property markdownTextRenderer Optional custom renderer for inline markdown text.
 */
data class RenderRegistry(
    private val blockRenderers: Map<IElementType, IBlockRenderer>,
    private val inlineNodeStringBuilders: Map<IElementType, IInlineNodeStringBuilder>,
    val markdownContentRenderer: MarkdownContentRenderer? = null,
    val markdownTextRenderer: MarkdownTextRenderer? = null,
) {
    /**
     * Returns the [IBlockRenderer] registered for the given element type, or null if none is registered.
     *
     * @param elementType The markdown element type to look up.
     * @return The registered block renderer, or null.
     */
    fun getBlockRenderer(elementType: IElementType): IBlockRenderer? = blockRenderers[elementType]

    /**
     * Returns the [IInlineNodeStringBuilder] registered for the given element type, or null if none is registered.
     *
     * @param elementType The markdown element type to look up.
     * @return The registered inline node string builder, or null.
     */
    fun getInlineNodeStringBuilder(elementType: IElementType): IInlineNodeStringBuilder? = inlineNodeStringBuilders[elementType]
}
