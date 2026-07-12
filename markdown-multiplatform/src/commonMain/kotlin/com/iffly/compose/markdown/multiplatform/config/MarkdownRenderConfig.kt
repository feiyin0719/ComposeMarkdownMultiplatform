package com.iffly.compose.markdown.multiplatform.config

import androidx.compose.runtime.Immutable
import com.iffly.compose.markdown.multiplatform.core.plugins.CorePlugin
import com.iffly.compose.markdown.multiplatform.render.IBlockRenderer
import com.iffly.compose.markdown.multiplatform.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.render.MarkdownContentRenderer
import com.iffly.compose.markdown.multiplatform.render.MarkdownInlineTextRenderer
import com.iffly.compose.markdown.multiplatform.render.MarkdownParser
import com.iffly.compose.markdown.multiplatform.render.RenderRegistry
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
    /** The parser responsible for converting raw Markdown text into a Node tree. */
    val markdownParser: MarkdownParser,
    /** The registry that maps node types to their corresponding renderers. */
    val renderRegistry: RenderRegistry,
) {
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

        fun build(): MarkdownRenderConfig {
            plugins.forEach { plugin ->
                plugin.inlineNodeStringBuilders().forEach { (nodeClass, builder) ->
                    inlineNodeStringBuilders[nodeClass] = builder
                }
                plugin.blockRenderers().forEach { (nodeClass, renderer) ->
                    blockRenderers[nodeClass] = renderer
                }
                extensions.addAll(plugin.parserExtensions())
            }

            val parser =
                Parser
                    .builder()
                    .includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES)
                    .extensions(extensions)
                    .build()

            return MarkdownRenderConfig(
                markdownTheme ?: MarkdownTheme.Default,
                MarkdownParser(parser::parse),
                RenderRegistry(
                    blockRenderers.toMap(),
                    inlineNodeStringBuilders.toMap(),
                    markdownContentRenderer,
                    markdownInlineTextRenderer,
                ),
            )
        }
    }
}
