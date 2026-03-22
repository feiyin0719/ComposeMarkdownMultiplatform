package com.iffly.compose.markdown.multiplatform.config

import com.iffly.compose.markdown.multiplatform.render.IBlockRenderer
import com.iffly.compose.markdown.multiplatform.render.IInlineNodeStringBuilder
import org.commonmark.Extension
import org.commonmark.node.Node
import kotlin.reflect.KClass

/**
 * Abstract base implementation of [IMarkdownRenderPlugin] that provides empty defaults
 * for all methods.
 *
 * Subclasses only need to override the methods relevant to their customization.
 *
 * @see IMarkdownRenderPlugin
 */
abstract class AbstractMarkdownRenderPlugin : IMarkdownRenderPlugin {
    override fun parserExtensions(): List<Extension> = emptyList()

    override fun blockRenderers(): Map<KClass<out Node>, IBlockRenderer<*>> = emptyMap()

    override fun inlineNodeStringBuilders(): Map<KClass<out Node>, IInlineNodeStringBuilder<*>> = emptyMap()
}
