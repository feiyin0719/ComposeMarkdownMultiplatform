package com.iffly.compose.markdown.multiplatform.config

import com.iffly.compose.markdown.multiplatform.render.IBlockRenderer
import com.iffly.compose.markdown.multiplatform.render.IInlineNodeStringBuilder
import org.intellij.markdown.IElementType

/**
 * Abstract base implementation of [IMarkdownRenderPlugin] that provides empty defaults
 * for [blockRenderers] and [inlineNodeStringBuilders].
 *
 * Subclasses only need to override the methods relevant to their customization.
 *
 * @see IMarkdownRenderPlugin
 */
abstract class AbstractMarkdownRenderPlugin : IMarkdownRenderPlugin {
    override fun blockRenderers(): Map<IElementType, IBlockRenderer> = emptyMap()

    override fun inlineNodeStringBuilders(): Map<IElementType, IInlineNodeStringBuilder> = emptyMap()
}
