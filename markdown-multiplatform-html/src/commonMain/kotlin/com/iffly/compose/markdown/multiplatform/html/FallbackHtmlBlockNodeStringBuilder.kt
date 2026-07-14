package com.iffly.compose.markdown.multiplatform.html

import androidx.compose.ui.text.AnnotatedString
import com.iffly.compose.markdown.multiplatform.ActionHandlerState
import com.iffly.compose.markdown.multiplatform.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.render.MarkdownInlineViewMap
import com.iffly.compose.markdown.multiplatform.render.NodeStringBuilderContext
import com.iffly.compose.markdown.multiplatform.render.RenderRegistry
import com.iffly.compose.markdown.multiplatform.style.MarkdownTheme
import org.commonmark.node.HtmlBlock

/**
 * Fallback string builder for [HtmlBlock] nodes that cannot be converted to Markdown.
 *
 * When [HtmlBlockRenderer] converts an HTML block but the result is re-parsed into
 * another single [HtmlBlock] (i.e. the converter could not map the HTML to any Markdown
 * syntax), [HtmlBlockRenderer] falls back to [MarkdownInlineText] which uses this builder
 * to render the raw HTML as plain text, preventing infinite recursion.
 *
 * @see HtmlBlockRenderer
 * @see HtmlMarkdownPlugin
 */
class FallbackHtmlBlockNodeStringBuilder : IInlineNodeStringBuilder<HtmlBlock> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: HtmlBlock,
        inlineContentMap: MarkdownInlineViewMap,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandlerState?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    ) {
        append(node.literal?.trim() ?: "")
    }
}
