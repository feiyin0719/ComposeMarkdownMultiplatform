package com.iffly.compose.markdown.multiplatform.config

import com.iffly.compose.markdown.multiplatform.render.IBlockRenderer
import com.iffly.compose.markdown.multiplatform.render.IInlineNodeStringBuilder
import org.commonmark.Extension
import org.commonmark.node.Node
import kotlin.reflect.KClass

/**
 * Plugin interface for extending the Markdown rendering pipeline.
 *
 * Implementations can provide custom parser extensions, block renderers, and inline
 * node string builders to extend both parsing and rendering capabilities.
 *
 * @see AbstractMarkdownRenderPlugin
 * @see MarkdownRenderConfig.Builder.addPlugin
 */
interface IMarkdownRenderPlugin {
    /** Returns a list of commonmark parser extensions to register with the parser. */
    fun parserExtensions(): List<Extension> = emptyList()

    /** Returns a mapping of node types to their custom [IBlockRenderer] implementations. */
    fun blockRenderers(): Map<KClass<out Node>, IBlockRenderer<*>> = emptyMap()

    /** Returns a mapping of node types to their custom [IInlineNodeStringBuilder] implementations. */
    fun inlineNodeStringBuilders(): Map<KClass<out Node>, IInlineNodeStringBuilder<*>> = emptyMap()
}
