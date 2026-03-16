package com.iffly.compose.markdown.multiplatform.core.renders

import androidx.compose.ui.text.SpanStyle
import com.iffly.compose.markdown.multiplatform.render.CompositeChildNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.style.MarkdownTheme
import org.intellij.markdown.ast.ASTNode

/**
 * Inline node string builder for GFM strikethrough elements (`~~text~~`).
 *
 * Applies the strikethrough [SpanStyle] from [MarkdownTheme] to the child content.
 *
 * @see CompositeChildNodeStringBuilder
 */
class StrikethroughNodeStringBuilder : CompositeChildNodeStringBuilder() {
    override fun getSpanStyle(
        node: ASTNode,
        markdownTheme: MarkdownTheme,
    ): SpanStyle? = markdownTheme.strikethrough
}

/**
 * Inline node string builder for strong emphasis (bold) elements (`**text**` or `__text__`).
 *
 * Applies the strong emphasis [SpanStyle] from [MarkdownTheme] to the child content.
 *
 * @see CompositeChildNodeStringBuilder
 */
class StrongEmphasisNodeStringBuilder : CompositeChildNodeStringBuilder() {
    override fun getSpanStyle(
        node: ASTNode,
        markdownTheme: MarkdownTheme,
    ): SpanStyle? = markdownTheme.strongEmphasis
}

/**
 * Inline node string builder for emphasis (italic) elements (`*text*` or `_text_`).
 *
 * Applies the emphasis [SpanStyle] from [MarkdownTheme] to the child content.
 *
 * @see CompositeChildNodeStringBuilder
 */
class EmphasisNodeStringBuilder : CompositeChildNodeStringBuilder() {
    override fun getSpanStyle(
        node: ASTNode,
        markdownTheme: MarkdownTheme,
    ): SpanStyle? = markdownTheme.emphasis
}
