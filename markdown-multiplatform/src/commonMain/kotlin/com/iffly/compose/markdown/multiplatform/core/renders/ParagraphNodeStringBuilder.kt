package com.iffly.compose.markdown.multiplatform.core.renders

import androidx.compose.ui.text.ParagraphStyle
import com.iffly.compose.markdown.multiplatform.render.CompositeChildNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.style.MarkdownTheme
import com.iffly.compose.markdown.multiplatform.util.getNodeParagraphStyle
import org.commonmark.node.Node

class ParagraphNodeStringBuilder : CompositeChildNodeStringBuilder() {
    override fun getParagraphStyle(
        node: Node,
        markdownTheme: MarkdownTheme,
    ): ParagraphStyle? = markdownTheme.getNodeParagraphStyle(node)
}
