package com.iffly.compose.markdown.multiplatform.config

import com.iffly.compose.markdown.multiplatform.render.IBlockRenderer
import com.iffly.compose.markdown.multiplatform.render.IInlineNodeStringBuilder
import org.intellij.markdown.IElementType
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider
import org.intellij.markdown.parser.sequentialparsers.SequentialParser

/**
 * Plugin interface for extending the Markdown rendering pipeline.
 *
 * Implementations can provide custom marker block providers, sequential parsers,
 * block renderers, and inline node string builders to extend parsing and rendering
 * capabilities.
 *
 * @see AbstractMarkdownRenderPlugin
 * @see MarkdownRenderConfig.Builder.addPlugin
 */
interface IMarkdownRenderPlugin {
    /** Returns custom [MarkerBlockProvider]s for extending block-level parsing. */
    fun markerBlockProviders(): List<MarkerBlockProvider<MarkerProcessor.StateInfo>> = emptyList()

    /** Returns custom [SequentialParser]s for extending inline-level parsing. */
    fun sequentialParsers(): List<SequentialParser> = emptyList()

    /** Returns a mapping of element types to their custom [IBlockRenderer] implementations. */
    fun blockRenderers(): Map<IElementType, IBlockRenderer> = emptyMap()

    /** Returns a mapping of element types to their custom [IInlineNodeStringBuilder] implementations. */
    fun inlineNodeStringBuilders(): Map<IElementType, IInlineNodeStringBuilder> = emptyMap()
}
