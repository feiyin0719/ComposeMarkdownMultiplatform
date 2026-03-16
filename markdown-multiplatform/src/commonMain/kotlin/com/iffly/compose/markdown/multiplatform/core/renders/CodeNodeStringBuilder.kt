package com.iffly.compose.markdown.multiplatform.core.renders

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.withStyle
import com.iffly.compose.markdown.multiplatform.ActionHandler
import com.iffly.compose.markdown.multiplatform.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.render.MarkdownInlineView
import com.iffly.compose.markdown.multiplatform.render.NodeStringBuilderContext
import com.iffly.compose.markdown.multiplatform.render.RenderRegistry
import com.iffly.compose.markdown.multiplatform.style.MarkdownTheme
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getTextInNode

/**
 * Inline node string builder for inline code span elements (`` `code` ``).
 *
 * Extracts the code text by filtering out backtick tokens, then applies the
 * inline code [SpanStyle] from [MarkdownTheme] with surrounding spaces for visual padding.
 *
 * @see IInlineNodeStringBuilder
 */
class CodeNodeStringBuilder : IInlineNodeStringBuilder {
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
        val codeText =
            node.children
                .filter { it.type != MarkdownTokenTypes.BACKTICK }
                .joinToString("") { it.getTextInNode(sourceText).toString() }
        withStyle(markdownTheme.code.toSpanStyle()) {
            append(" $codeText ")
        }
    }
}
