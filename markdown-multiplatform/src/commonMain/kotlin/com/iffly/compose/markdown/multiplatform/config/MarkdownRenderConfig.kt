package com.iffly.compose.markdown.multiplatform.config

import androidx.compose.runtime.Immutable
import com.iffly.compose.markdown.multiplatform.core.plugins.CorePlugin
import com.iffly.compose.markdown.multiplatform.render.IBlockRenderer
import com.iffly.compose.markdown.multiplatform.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.render.MarkdownContentRenderer
import com.iffly.compose.markdown.multiplatform.render.MarkdownInlineTextRenderer
import com.iffly.compose.markdown.multiplatform.render.MarkdownParser
import com.iffly.compose.markdown.multiplatform.render.RenderRegistry
import com.iffly.compose.markdown.multiplatform.streaming.StreamingMarkdownParser
import com.iffly.compose.markdown.multiplatform.style.MarkdownTheme
import org.commonmark.Extension
import org.commonmark.node.Node
import org.commonmark.parser.IncludeSourceSpans
import org.commonmark.parser.Parser
import kotlin.reflect.KClass

/**
 * Configuration holder for the Markdown rendering pipeline, encapsulating the theme,
 * parser, and render registry. Instances are created via the [Builder].
 *
 * @see Builder
 */
@Immutable
class MarkdownRenderConfig private constructor(
    /** The theme used for styling rendered Markdown content. */
    val markdownTheme: MarkdownTheme,
    /** The registry that maps node types to their corresponding renderers. */
    val renderRegistry: RenderRegistry,
    /** Source spans included by the parser used for regular rendering. */
    val includeSourceSpans: IncludeSourceSpans,
    private val parserExtensions: List<Extension>,
    private val parserBuilderCustomizer: ((Parser.Builder) -> Unit)?,
    private val streamingMarkdownParserFactory: ((MarkdownRenderConfig) -> StreamingMarkdownParser)?,
) {
    /** The lazily created parser used by regular Markdown rendering. */
    val markdownParser: MarkdownParser by lazy { createMarkdownParser() }

    fun createStreamingMarkdownParser(): StreamingMarkdownParser? = streamingMarkdownParserFactory?.invoke(this)

    /** Creates an independent parser from this configuration's extensions and source-span policy. */
    fun createMarkdownParser(minimumSourceSpans: IncludeSourceSpans = IncludeSourceSpans.NONE): MarkdownParser {
        val effectiveSourceSpans = includeSourceSpans.atLeast(minimumSourceSpans)
        val parserBuilder =
            Parser
                .builder()
                .includeSourceSpans(effectiveSourceSpans)
                .extensions(parserExtensions)
        parserBuilderCustomizer?.invoke(parserBuilder)
        val parser = parserBuilder.build()
        return MarkdownParser(parser::parse)
    }

    companion object {
        private val internalPlugins =
            listOf<IMarkdownRenderPlugin>(
                CorePlugin(),
            )
    }

    /**
     * Builder for constructing a [MarkdownRenderConfig] instance.
     *
     * Plugins, renderers, and theme can be customized before calling [build].
     */
    class Builder {
        private val plugins =
            mutableListOf(
                *internalPlugins.toTypedArray(),
            )

        private var markdownTheme: MarkdownTheme? = null

        private var markdownInlineTextRenderer: MarkdownInlineTextRenderer? = null

        private var markdownContentRenderer: MarkdownContentRenderer? = null

        private val inlineNodeStringBuilders =
            mutableMapOf<KClass<out Node>, IInlineNodeStringBuilder<*>>()

        private val blockRenderers = mutableMapOf<KClass<out Node>, IBlockRenderer<*>>()

        private val extensions = mutableListOf<Extension>()
        private var includeSourceSpans: IncludeSourceSpans = IncludeSourceSpans.BLOCKS
        private var streamingMarkdownParserFactory: ((MarkdownRenderConfig) -> StreamingMarkdownParser)? = null
        private var parserBuilderCustomizer: ((Parser.Builder) -> Unit)? = null

        fun markdownTheme(markdownTheme: MarkdownTheme): Builder {
            this.markdownTheme = markdownTheme
            return this
        }

        fun addPlugin(plugin: IMarkdownRenderPlugin): Builder {
            plugins.add(plugin)
            return this
        }

        fun <T : Node> addInlineNodeStringBuilder(
            nodeClass: KClass<T>,
            builder: IInlineNodeStringBuilder<T>,
        ): Builder {
            inlineNodeStringBuilders[nodeClass] = builder
            return this
        }

        fun <T : Node> addBlockRenderer(
            nodeClass: KClass<T>,
            renderer: IBlockRenderer<T>,
        ): Builder {
            blockRenderers[nodeClass] = renderer
            return this
        }

        fun markdownInlineTextRenderer(renderer: MarkdownInlineTextRenderer): Builder {
            this.markdownInlineTextRenderer = renderer
            return this
        }

        fun markdownContentRenderer(renderer: MarkdownContentRenderer): Builder {
            this.markdownContentRenderer = renderer
            return this
        }

        fun addExtension(extension: Extension): Builder {
            extensions.add(extension)
            return this
        }

        /** Configures source spans for parsers used by regular Markdown rendering. */
        fun includeSourceSpans(includeSourceSpans: IncludeSourceSpans): Builder {
            this.includeSourceSpans = includeSourceSpans
            return this
        }

        /**
         * Configures the per-component streaming parser factory.
         *
         * The default is `null`. Without a factory, Markdown components use the normal full parser
         * even when their `isStreaming` parameter is `true`.
         */
        fun streamingMarkdownParserFactory(factory: ((MarkdownRenderConfig) -> StreamingMarkdownParser)?): Builder {
            streamingMarkdownParserFactory = factory
            return this
        }

        /**
         * Configures each underlying commonmark [Parser.Builder].
         *
         * The customizer is applied after extensions, source spans, and all other parser settings,
         * allowing it to override values configured by this builder.
         */
        fun configureParserBuilder(customizer: (Parser.Builder) -> Unit): Builder {
            parserBuilderCustomizer = customizer
            return this
        }

        fun build(): MarkdownRenderConfig {
            val parserExtensions = extensions.toMutableList()
            plugins.forEach { plugin ->
                plugin.inlineNodeStringBuilders().forEach { (nodeClass, builder) ->
                    inlineNodeStringBuilders[nodeClass] = builder
                }
                plugin.blockRenderers().forEach { (nodeClass, renderer) ->
                    blockRenderers[nodeClass] = renderer
                }
                parserExtensions.addAll(plugin.parserExtensions())
            }

            return MarkdownRenderConfig(
                markdownTheme ?: MarkdownTheme.Default,
                RenderRegistry(
                    blockRenderers.toMap(),
                    inlineNodeStringBuilders.toMap(),
                    markdownContentRenderer,
                    markdownInlineTextRenderer,
                ),
                includeSourceSpans,
                parserExtensions.toList(),
                parserBuilderCustomizer,
                streamingMarkdownParserFactory,
            )
        }
    }
}

private fun IncludeSourceSpans.atLeast(minimum: IncludeSourceSpans): IncludeSourceSpans = if (ordinal >= minimum.ordinal) this else minimum
