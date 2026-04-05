package com.iffly.compose.markdown.multiplatform.render

import org.commonmark.node.Document
import org.commonmark.node.Node
import kotlin.reflect.KClass

data class RenderRegistry(
    private val blockRenderers: Map<KClass<out Node>, IBlockRenderer<*>>,
    private val inlineNodeStringBuilders: Map<KClass<out Node>, IInlineNodeStringBuilder<*>>,
    val markdownContentRenderer: MarkdownContentRenderer? = null,
    val markdownInlineTextRenderer: MarkdownInlineTextRenderer? = null,
) {
    fun getBlockRenderer(nodeClass: KClass<out Node>): IBlockRenderer<*>? = blockRenderers[nodeClass]

    @Suppress("UNCHECKED_CAST")
    fun getBlockRenderer(node: Node): IBlockRenderer<Node>? = blockRenderers[node::class] as? IBlockRenderer<Node>

    /**
     * Checks if the given node should skip rendering.
     * Returns true if a renderer exists and its [IBlockRenderer.shouldSkipRender] returns true.
     */
    fun shouldSkipRender(node: Node): Boolean {
        val renderer = getBlockRenderer(node) ?: return false
        return renderer.shouldSkipRender(node)
    }

    fun getInlineNodeStringBuilder(nodeClass: KClass<out Node>): IInlineNodeStringBuilder<*>? = inlineNodeStringBuilders[nodeClass]

    fun getInlineNodeStringBuilder(node: Node): IInlineNodeStringBuilder<*>? = inlineNodeStringBuilders[node::class]

    /**
     * Creates an augmented [RenderRegistry] for Text-based rendering ([MarkdownText]).
     *
     * For each block renderer that has no corresponding [IInlineNodeStringBuilder],
     * a [BlockRendererInlineStringBuilder] wrapper is created and registered. A
     * [DocumentInlineStringBuilder] is also added if not already present.
     *
     * This allows [MarkdownText] to render all block nodes as inline content
     * without modifying the base registry at config-build time.
     */
    @Suppress("UNCHECKED_CAST")
    fun textModeRegistry(): RenderRegistry {
        val augmented = inlineNodeStringBuilders.toMutableMap()
        if (!augmented.containsKey(Document::class)) {
            augmented[Document::class] = DocumentInlineStringBuilder()
        }
        for ((nodeClass, renderer) in blockRenderers) {
            if (!augmented.containsKey(nodeClass)) {
                augmented[nodeClass] =
                    BlockRendererInlineStringBuilder(renderer as IBlockRenderer<Node>)
            }
        }
        return copy(inlineNodeStringBuilders = augmented.toMap())
    }
}
