package com.iffly.compose.markdown.multiplatform.config

import com.iffly.compose.markdown.multiplatform.core.plugins.CorePlugin
import com.iffly.compose.markdown.multiplatform.render.IBlockRenderer
import com.iffly.compose.markdown.multiplatform.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.render.MarkdownContentRenderer
import com.iffly.compose.markdown.multiplatform.render.MarkdownParser
import com.iffly.compose.markdown.multiplatform.render.MarkdownTextRenderer
import com.iffly.compose.markdown.multiplatform.render.RenderRegistry
import com.iffly.compose.markdown.multiplatform.style.MarkdownTheme
import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.flavours.gfm.GFMConstraints
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMTokenTypes
import org.intellij.markdown.flavours.gfm.table.GitHubTableMarkerProvider
import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.MarkerProcessorFactory
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.constraints.getCharsEaten
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.SequentialParserManager
import org.intellij.markdown.parser.MarkdownParser as IntelliJMarkdownParser

class MarkdownRenderConfig {
    var markdownTheme: MarkdownTheme
        private set

    var markdownParser: MarkdownParser
        private set

    var renderRegistry: RenderRegistry
        private set

    private constructor(
        markdownTheme: MarkdownTheme,
        markdownParser: MarkdownParser,
        renderRegistry: RenderRegistry,
    ) {
        this.markdownTheme = markdownTheme
        this.markdownParser = markdownParser
        this.renderRegistry = renderRegistry
    }

    companion object {
        private val internalPlugins =
            listOf<IMarkdownRenderPlugin>(
                CorePlugin(),
            )
    }

    class Builder {
        private val plugins =
            mutableListOf(
                *internalPlugins.toTypedArray(),
            )

        private var markdownTheme: MarkdownTheme? = null

        private var markdownTextRenderer: MarkdownTextRenderer? = null

        private var markdownContentRenderer: MarkdownContentRenderer? = null

        private val inlineNodeStringBuilders =
            mutableMapOf<IElementType, IInlineNodeStringBuilder>()

        private val blockRenderers = mutableMapOf<IElementType, IBlockRenderer>()

        private val markerBlockProviders =
            mutableListOf<MarkerBlockProvider<MarkerProcessor.StateInfo>>()

        private val sequentialParsers = mutableListOf<SequentialParser>()

        fun markdownTheme(markdownTheme: MarkdownTheme): Builder {
            this.markdownTheme = markdownTheme
            return this
        }

        fun addPlugin(plugin: IMarkdownRenderPlugin): Builder {
            plugins.add(plugin)
            return this
        }

        fun addInlineNodeStringBuilder(
            elementType: IElementType,
            builder: IInlineNodeStringBuilder,
        ): Builder {
            inlineNodeStringBuilders[elementType] = builder
            return this
        }

        fun addBlockRenderer(
            elementType: IElementType,
            renderer: IBlockRenderer,
        ): Builder {
            blockRenderers[elementType] = renderer
            return this
        }

        fun addMarkerBlockProvider(
            provider: MarkerBlockProvider<MarkerProcessor.StateInfo>,
        ): Builder {
            markerBlockProviders.add(provider)
            return this
        }

        fun addSequentialParser(parser: SequentialParser): Builder {
            sequentialParsers.add(parser)
            return this
        }

        fun markdownTextRenderer(renderer: MarkdownTextRenderer): Builder {
            this.markdownTextRenderer = renderer
            return this
        }

        fun markdownContentRenderer(renderer: MarkdownContentRenderer): Builder {
            this.markdownContentRenderer = renderer
            return this
        }

        fun build(): MarkdownRenderConfig {
            plugins.forEach { plugin ->
                plugin.markerBlockProviders().forEach { provider ->
                    addMarkerBlockProvider(provider)
                }
                plugin.sequentialParsers().forEach { parser ->
                    addSequentialParser(parser)
                }
                plugin.inlineNodeStringBuilders().forEach { (elementType, builder) ->
                    inlineNodeStringBuilders[elementType] = builder
                }
                plugin.blockRenderers().forEach { (elementType, renderer) ->
                    blockRenderers[elementType] = renderer
                }
            }

            return MarkdownRenderConfig(
                markdownTheme ?: MarkdownTheme(),
                MarkdownParser { sourceText ->
                    IntelliJMarkdownParser(
                        ExtensibleGFMFlavourDescriptor(
                            extraMarkerBlockProviders = markerBlockProviders.toList(),
                            extraSequentialParsers = sequentialParsers.toList(),
                        ),
                    ).buildMarkdownTreeFromString(sourceText)
                },
                RenderRegistry(
                    blockRenderers.toMap(),
                    inlineNodeStringBuilders.toMap(),
                    markdownContentRenderer,
                    markdownTextRenderer,
                ),
            )
        }
    }
}

private class ExtensibleGFMFlavourDescriptor(
    private val extraMarkerBlockProviders: List<MarkerBlockProvider<MarkerProcessor.StateInfo>>,
    private val extraSequentialParsers: List<SequentialParser>,
) : GFMFlavourDescriptor() {

    override val sequentialParserManager =
        object : SequentialParserManager() {
            override fun getParserSequence(): List<SequentialParser> =
                extraSequentialParsers + super@ExtensibleGFMFlavourDescriptor.sequentialParserManager.getParserSequence()
        }

    override val markerProcessorFactory: MarkerProcessorFactory =
        ExtensibleGFMMarkerProcessorFactory(extraMarkerBlockProviders)
}

/**
 * A [MarkerProcessorFactory] that creates [ExtensibleGFMMarkerProcessor] instances.
 *
 * Follows the same pattern as [GFMMarkerProcessor.Factory], but extends the
 * provider list with custom providers.
 */
private class ExtensibleGFMMarkerProcessorFactory(
    private val extraMarkerBlockProviders: List<MarkerBlockProvider<MarkerProcessor.StateInfo>>,
) : MarkerProcessorFactory {
    override fun createMarkerProcessor(productionHolder: ProductionHolder): MarkerProcessor<*> {
        return ExtensibleGFMMarkerProcessor(
            productionHolder,
            GFMConstraints.BASE,
            extraMarkerBlockProviders,
        )
    }
}

/**
 * Extends [CommonMarkMarkerProcessor][org.intellij.markdown.flavours.commonmark.CommonMarkMarkerProcessor]
 * with GFM features (table support, checkbox handling) plus custom [MarkerBlockProvider]s.
 *
 * This class replicates [GFMMarkerProcessor]'s behavior since that class is final
 * and cannot be subclassed directly.
 */
private class ExtensibleGFMMarkerProcessor(
    productionHolder: ProductionHolder,
    constraintsBase: GFMConstraints,
    extraMarkerBlockProviders: List<MarkerBlockProvider<StateInfo>>,
) : org.intellij.markdown.flavours.commonmark.CommonMarkMarkerProcessor(productionHolder, constraintsBase) {

    // Mirrors GFMMarkerProcessor: super providers + table provider, with extras prepended
    private val allMarkerBlockProviders =
        extraMarkerBlockProviders + super.getMarkerBlockProviders() + GitHubTableMarkerProvider()

    override fun getMarkerBlockProviders(): List<MarkerBlockProvider<StateInfo>> =
        allMarkerBlockProviders

    override fun populateConstraintsTokens(
        pos: LookaheadText.Position,
        constraints: MarkdownConstraints,
        productionHolder: ProductionHolder,
    ) {
        if (constraints !is GFMConstraints || !constraints.hasCheckbox()) {
            super.populateConstraintsTokens(pos, constraints, productionHolder)
            return
        }

        val line = pos.currentLine
        var offset = pos.offsetInCurrentLine
        while (offset < line.length && line[offset] != '[') {
            offset++
        }
        if (offset == line.length) {
            super.populateConstraintsTokens(pos, constraints, productionHolder)
            return
        }

        val type = when (constraints.types.lastOrNull()) {
            '>' -> MarkdownTokenTypes.BLOCK_QUOTE
            '.', ')' -> MarkdownTokenTypes.LIST_NUMBER
            else -> MarkdownTokenTypes.LIST_BULLET
        }
        val middleOffset = pos.offset - pos.offsetInCurrentLine + offset
        val endOffset = kotlin.math.min(
            pos.offset - pos.offsetInCurrentLine + constraints.getCharsEaten(pos.currentLine),
            pos.nextLineOrEofOffset,
        )

        productionHolder.addProduction(
            listOf(
                SequentialParser.Node(pos.offset..middleOffset, type),
                SequentialParser.Node(middleOffset..endOffset, GFMTokenTypes.CHECK_BOX),
            ),
        )
    }
}
