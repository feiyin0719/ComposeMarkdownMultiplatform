package com.iffly.compose.markdown.multiplatform.core.renders

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.withStyle
import com.iffly.compose.markdown.multiplatform.ActionHandler
import com.iffly.compose.markdown.multiplatform.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.render.MarkdownInlineView
import com.iffly.compose.markdown.multiplatform.render.NodeStringBuilderContext
import com.iffly.compose.markdown.multiplatform.render.RenderRegistry
import com.iffly.compose.markdown.multiplatform.render.buildChildNodeAnnotatedString
import com.iffly.compose.markdown.multiplatform.style.MarkdownTheme
import com.iffly.compose.markdown.multiplatform.util.getNodeParagraphStyle
import com.iffly.compose.markdown.multiplatform.util.getNodeSpanStyle
import org.commonmark.node.Heading

/**
 * Inline node string builder for heading elements (h1-h6).
 * Applies heading-level-specific styles before recursively building child inline content.
 */
class HeadingNodeStringBuilder : IInlineNodeStringBuilder<Heading> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: Heading,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    ) {
        val spanStyle = markdownTheme.getNodeSpanStyle(node)
        val paragraphStyle = markdownTheme.getNodeParagraphStyle(node)

        withStyle(paragraphStyle) {
            withStyle(spanStyle) {
                buildChildNodeAnnotatedString(
                    parent = node,
                    indentLevel = indentLevel,
                    inlineContentMap = inlineContentMap,
                    markdownTheme = markdownTheme,
                    renderRegistry = renderRegistry,
                    actionHandler = actionHandler,
                    isShowNotSupported = isShowNotSupported,
                    nodeStringBuilderContext = nodeStringBuilderContext,
                )
            }
        }
    }
}
