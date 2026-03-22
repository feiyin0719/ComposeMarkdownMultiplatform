package com.iffly.compose.markdown.multiplatform.html

import com.iffly.compose.markdown.multiplatform.config.IMarkdownRenderPlugin
import com.iffly.compose.markdown.multiplatform.html.handlers.defaultHtmlInlineTagHandlers
import com.iffly.compose.markdown.multiplatform.render.IInlineNodeStringBuilder
import org.commonmark.node.HtmlInline
import org.commonmark.node.Node
import kotlin.reflect.KClass

/**
 * Markdown render plugin that adds inline HTML tag support.
 *
 * Combines the [defaultHtmlInlineTagHandlers] with any custom handlers provided,
 * where custom handlers override defaults for the same tag names. Registers an
 * [HtmlInlineNodeStringBuilder] for [HtmlInline] nodes.
 *
 * @param customTagHandlers Additional or overriding HTML inline tag handlers.
 * @see IMarkdownRenderPlugin
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

    override fun inlineNodeStringBuilders(): Map<KClass<out Node>, IInlineNodeStringBuilder<*>> =
        mapOf(HtmlInline::class to HtmlInlineNodeStringBuilder(tagHandlerMap))
}
