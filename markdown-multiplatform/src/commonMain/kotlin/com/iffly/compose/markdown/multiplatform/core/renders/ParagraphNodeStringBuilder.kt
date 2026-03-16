package com.iffly.compose.markdown.multiplatform.core.renders

import androidx.compose.ui.text.ParagraphStyle
import com.iffly.compose.markdown.multiplatform.render.CompositeChildNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.style.MarkdownTheme
import com.iffly.compose.markdown.multiplatform.util.getNodeParagraphStyle
import org.intellij.markdown.ast.ASTNode

/**
 * Inline node string builder for paragraph elements.
 *
 * Extends [CompositeChildNodeStringBuilder] and applies the paragraph-specific
 * [ParagraphStyle] obtained from the current [MarkdownTheme].
 *
 * @see CompositeChildNodeStringBuilder
 */
class ParagraphNodeStringBuilder : CompositeChildNodeStringBuilder() {
    override fun getParagraphStyle(
        node: ASTNode,
        markdownTheme: MarkdownTheme,
    ): ParagraphStyle? = markdownTheme.getNodeParagraphStyle(node)
}
