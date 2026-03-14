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
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode

class HeadingNodeStringBuilder : IInlineNodeStringBuilder {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: ASTNode,
        sourceText: String,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    ) {
        val atxContent =
            node.children.firstOrNull {
                it.type == MarkdownTokenTypes.ATX_CONTENT ||
                    it.type == MarkdownTokenTypes.SETEXT_CONTENT
            } ?: return

        val spanStyle = markdownTheme.getNodeSpanStyle(node)
        val paragraphStyle = markdownTheme.getNodeParagraphStyle(node)

        withStyle(paragraphStyle) {
            withStyle(spanStyle) {
                buildChildNodeAnnotatedString(
                    parent = atxContent,
                    sourceText = sourceText,
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
