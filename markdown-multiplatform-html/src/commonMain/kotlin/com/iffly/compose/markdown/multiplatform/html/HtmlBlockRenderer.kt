package com.iffly.compose.markdown.multiplatform.html

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.iffly.compose.markdown.multiplatform.config.currentParser
import com.iffly.compose.markdown.multiplatform.render.IBlockRenderer
import com.iffly.compose.markdown.multiplatform.render.MarkdownChildren
import com.iffly.compose.markdown.multiplatform.render.MarkdownInlineText
import org.commonmark.ext.htmlconverter.HtmlToMarkdownConverter
import org.commonmark.node.Document
import org.commonmark.node.HtmlBlock

/**
 * Block renderer for [HtmlBlock] nodes.
 *
 * Uses [HtmlToMarkdownConverter] to convert HTML content to a commonmark [Document] AST.
 * If the conversion succeeds (i.e. the result is not a single [HtmlBlock]), renders the
 * converted children as normal markdown. Otherwise falls back to [MarkdownInlineText] to render
 * the raw HTML as plain text.
 *
 * @see HtmlMarkdownPlugin
 * @see FallbackHtmlBlockNodeStringBuilder
 */
class HtmlBlockRenderer : IBlockRenderer<HtmlBlock> {
    @Composable
    override fun Invoke(
        node: HtmlBlock,
        modifier: Modifier,
    ) {
        val html = node.literal?.trim() ?: return
        if (html.isEmpty()) return

        val parser = currentParser()
        val document =
            remember(html, parser) {
                val converted = HtmlToMarkdownConverter.convert(html)
                parser.parse(converted)
            }
        if (document.isSingleHtmlBlock()) {
            MarkdownInlineText(parent = node, modifier = modifier)
        } else {
            MarkdownChildren(parent = document, modifier = modifier)
        }
    }

    private fun org.commonmark.node.Node.isSingleHtmlBlock(): Boolean {
        val firstChild = firstChild ?: return false
        return firstChild is HtmlBlock && firstChild.next == null
    }
}
