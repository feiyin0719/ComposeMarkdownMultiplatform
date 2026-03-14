package com.iffly.compose.markdown.multiplatform.core.renders

import androidx.compose.ui.text.SpanStyle
import com.iffly.compose.markdown.multiplatform.render.CompositeChildNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.style.MarkdownTheme
import org.intellij.markdown.ast.ASTNode

class StrikethroughNodeStringBuilder : CompositeChildNodeStringBuilder() {
    override fun getSpanStyle(
        node: ASTNode,
        markdownTheme: MarkdownTheme,
    ): SpanStyle? = markdownTheme.strikethrough
}

class StrongEmphasisNodeStringBuilder : CompositeChildNodeStringBuilder() {
    override fun getSpanStyle(
        node: ASTNode,
        markdownTheme: MarkdownTheme,
    ): SpanStyle? = markdownTheme.strongEmphasis
}

class EmphasisNodeStringBuilder : CompositeChildNodeStringBuilder() {
    override fun getSpanStyle(
        node: ASTNode,
        markdownTheme: MarkdownTheme,
    ): SpanStyle? = markdownTheme.emphasis
}
