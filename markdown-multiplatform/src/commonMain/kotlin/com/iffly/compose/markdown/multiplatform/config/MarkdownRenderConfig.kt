package com.iffly.compose.markdown.multiplatform.config

import com.iffly.compose.markdown.multiplatform.core.plugins.CorePlugin
import com.iffly.compose.markdown.multiplatform.render.IBlockRenderer
import com.iffly.compose.markdown.multiplatform.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.render.MarkdownContentRenderer
import com.iffly.compose.markdown.multiplatform.render.MarkdownTextRenderer
import com.iffly.compose.markdown.multiplatform.render.RenderRegistry
import com.iffly.compose.markdown.multiplatform.style.MarkdownTheme
import org.intellij.markdown.IElementType

class MarkdownRenderConfig {
    var markdownTheme: MarkdownTheme
        private set

    var renderRegistry: RenderRegistry
        private set

    private constructor(
        markdownTheme: MarkdownTheme,
        renderRegistry: RenderRegistry,
    ) {
        this.markdownTheme = markdownTheme
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
                plugin.inlineNodeStringBuilders().forEach { (elementType, builder) ->
                    // don't override user-provided builders
                    if (!inlineNodeStringBuilders.containsKey(elementType)) {
                        inlineNodeStringBuilders[elementType] = builder
                    }
                }
                plugin.blockRenderers().forEach { (elementType, renderer) ->
                    if (!blockRenderers.containsKey(elementType)) {
                        blockRenderers[elementType] = renderer
                    }
                }
            }

            return MarkdownRenderConfig(
                markdownTheme ?: MarkdownTheme(),
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
