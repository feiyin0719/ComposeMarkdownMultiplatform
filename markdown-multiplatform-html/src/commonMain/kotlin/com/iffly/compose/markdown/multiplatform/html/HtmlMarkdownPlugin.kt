package com.iffly.compose.markdown.multiplatform.html

import com.iffly.compose.markdown.multiplatform.config.IMarkdownRenderPlugin
import com.iffly.compose.markdown.multiplatform.html.handlers.defaultHtmlInlineTagHandlers
import com.iffly.compose.markdown.multiplatform.render.IBlockRenderer
import com.iffly.compose.markdown.multiplatform.render.IInlineNodeStringBuilder
import org.commonmark.node.HtmlBlock
import org.commonmark.node.HtmlInline
import org.commonmark.node.Node
import kotlin.reflect.KClass

/**
 * Markdown render plugin that adds HTML block and inline HTML tag support.
 *
 * For block-level HTML, registers an [HtmlBlockRenderer] that converts HTML to Markdown
 * nodes using [org.commonmark.ext.htmlconverter.HtmlToMarkdownConverter]. When the HTML
 * cannot be converted to Markdown nodes, it falls back to rendering the raw HTML as plain
 * text via [FallbackHtmlBlockNodeStringBuilder].
 *
 * For inline HTML, combines the [defaultHtmlInlineTagHandlers] with any custom handlers
 * provided, where custom handlers override defaults for the same tag names. Registers an
 * [HtmlInlineNodeStringBuilder] for [HtmlInline] nodes.
 *
 * @param customTagHandlers Additional or overriding HTML inline tag handlers.
 * @see IMarkdownRenderPlugin
 * @see HtmlBlockRenderer
 * @see HtmlInlineTagHandler
 */
class HtmlMarkdownPlugin(
    customTagHandlers: List<HtmlInlineTagHandler> = emptyList(),
) : IMarkdownRenderPlugin {
    private val tagHandlerMap: Map<String, HtmlInlineTagHandler>

    init {
        val map = mutableMapOf<String, HtmlInlineTagHandler>()
        for (handler in defaultHtmlInlineTagHandlers()) {
            for (name in handler.tagNames) {
                map[name] = handler
            }
        }
        for (handler in customTagHandlers) {
            for (name in handler.tagNames) {
                map[name] = handler
            }
        }
        tagHandlerMap = map.toMap()
    }

    override fun blockRenderers(): Map<KClass<out Node>, IBlockRenderer<*>> = mapOf(HtmlBlock::class to HtmlBlockRenderer())

    override fun inlineNodeStringBuilders(): Map<KClass<out Node>, IInlineNodeStringBuilder<*>> =
        mapOf(
            HtmlInline::class to HtmlInlineNodeStringBuilder(tagHandlerMap),
            HtmlBlock::class to FallbackHtmlBlockNodeStringBuilder(),
        )
}
