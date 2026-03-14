package com.iffly.compose.markdown.multiplatform.config

import com.iffly.compose.markdown.multiplatform.render.IBlockRenderer
import com.iffly.compose.markdown.multiplatform.render.IInlineNodeStringBuilder
import org.intellij.markdown.IElementType

interface IMarkdownRenderPlugin {
    fun blockRenderers(): Map<IElementType, IBlockRenderer> = emptyMap()

    fun inlineNodeStringBuilders(): Map<IElementType, IInlineNodeStringBuilder> = emptyMap()
}
