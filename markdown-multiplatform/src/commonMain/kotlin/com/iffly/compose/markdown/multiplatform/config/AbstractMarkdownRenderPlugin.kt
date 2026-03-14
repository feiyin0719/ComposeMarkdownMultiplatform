package com.iffly.compose.markdown.multiplatform.config

import com.iffly.compose.markdown.multiplatform.render.IBlockRenderer
import com.iffly.compose.markdown.multiplatform.render.IInlineNodeStringBuilder
import org.intellij.markdown.IElementType

abstract class AbstractMarkdownRenderPlugin : IMarkdownRenderPlugin {
    override fun blockRenderers(): Map<IElementType, IBlockRenderer> = emptyMap()

    override fun inlineNodeStringBuilders(): Map<IElementType, IInlineNodeStringBuilder> = emptyMap()
}
