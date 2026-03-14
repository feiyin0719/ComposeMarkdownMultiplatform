package com.iffly.compose.markdown.multiplatform.core.renders

import androidx.compose.ui.text.ParagraphStyle
import com.iffly.compose.markdown.multiplatform.render.CompositeChildNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.style.MarkdownTheme
import com.iffly.compose.markdown.multiplatform.util.getNodeParagraphStyle
import org.intellij.markdown.ast.ASTNode

class ParagraphNodeStringBuilder : CompositeChildNodeStringBuilder() {
    override fun getParagraphStyle(
        node: ASTNode,
        markdownTheme: MarkdownTheme,
    ): ParagraphStyle? = markdownTheme.getNodeParagraphStyle(node)
}
