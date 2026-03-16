package com.iffly.compose.markdown.multiplatform.html

import com.iffly.compose.markdown.multiplatform.config.IMarkdownRenderPlugin
import com.iffly.compose.markdown.multiplatform.html.handlers.defaultHtmlInlineTagHandlers
import com.iffly.compose.markdown.multiplatform.render.IInlineNodeStringBuilder
import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownTokenTypes

/**
 * Markdown render plugin that adds inline HTML tag support.
 *
 * Combines the [defaultHtmlInlineTagHandlers] with any custom handlers provided,
 * where custom handlers override defaults for the same tag names. Registers an
 * [HtmlInlineNodeStringBuilder] for [MarkdownTokenTypes.HTML_TAG] elements.
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

    override fun inlineNodeStringBuilders(): Map<IElementType, IInlineNodeStringBuilder> =
        mapOf(MarkdownTokenTypes.HTML_TAG to HtmlInlineNodeStringBuilder(tagHandlerMap))
}
