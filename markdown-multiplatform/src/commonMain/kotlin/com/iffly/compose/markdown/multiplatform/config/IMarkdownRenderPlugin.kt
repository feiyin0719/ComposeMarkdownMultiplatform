package com.iffly.compose.markdown.multiplatform.config

import com.iffly.compose.markdown.multiplatform.render.IBlockRenderer
import com.iffly.compose.markdown.multiplatform.render.IInlineNodeStringBuilder
import org.intellij.markdown.IElementType
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider
import org.intellij.markdown.parser.sequentialparsers.SequentialParser

interface IMarkdownRenderPlugin {
    fun markerBlockProviders(): List<MarkerBlockProvider<MarkerProcessor.StateInfo>> = emptyList()

    fun sequentialParsers(): List<SequentialParser> = emptyList()

    fun blockRenderers(): Map<IElementType, IBlockRenderer> = emptyMap()

    fun inlineNodeStringBuilders(): Map<IElementType, IInlineNodeStringBuilder> = emptyMap()
}
