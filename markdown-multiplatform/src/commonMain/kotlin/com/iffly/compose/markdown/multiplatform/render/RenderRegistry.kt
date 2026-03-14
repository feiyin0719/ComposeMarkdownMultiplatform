package com.iffly.compose.markdown.multiplatform.render

import org.intellij.markdown.IElementType

data class RenderRegistry(
    private val blockRenderers: Map<IElementType, IBlockRenderer>,
    private val inlineNodeStringBuilders: Map<IElementType, IInlineNodeStringBuilder>,
    val markdownContentRenderer: MarkdownContentRenderer? = null,
    val markdownTextRenderer: MarkdownTextRenderer? = null,
) {
    fun getBlockRenderer(elementType: IElementType): IBlockRenderer? = blockRenderers[elementType]

    fun getInlineNodeStringBuilder(elementType: IElementType): IInlineNodeStringBuilder? = inlineNodeStringBuilders[elementType]
}
