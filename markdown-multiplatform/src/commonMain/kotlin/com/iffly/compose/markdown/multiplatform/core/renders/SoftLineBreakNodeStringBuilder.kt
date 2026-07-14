package com.iffly.compose.markdown.multiplatform.core.renders

import androidx.compose.ui.text.AnnotatedString
import com.iffly.compose.markdown.multiplatform.ActionHandlerState
import com.iffly.compose.markdown.multiplatform.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.render.MarkdownInlineViewMap
import com.iffly.compose.markdown.multiplatform.render.NodeStringBuilderContext
import com.iffly.compose.markdown.multiplatform.render.RenderRegistry
import com.iffly.compose.markdown.multiplatform.style.MarkdownTheme
import org.commonmark.node.SoftLineBreak

/**
 * Inline node string builder for SoftLineBreak nodes.
 * Appends a single space to join lines.
 */
class SoftLineBreakNodeStringBuilder : IInlineNodeStringBuilder<SoftLineBreak> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: SoftLineBreak,
        inlineContentMap: MarkdownInlineViewMap,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandlerState?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    ) {
        append(' ')
    }
}
