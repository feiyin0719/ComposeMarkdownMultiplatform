package com.iffly.compose.markdown.multiplatform.core.renders

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.withStyle
import com.iffly.compose.markdown.multiplatform.ActionHandler
import com.iffly.compose.markdown.multiplatform.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.render.MarkdownInlineView
import com.iffly.compose.markdown.multiplatform.render.NodeStringBuilderContext
import com.iffly.compose.markdown.multiplatform.render.RenderRegistry
import com.iffly.compose.markdown.multiplatform.style.MarkdownTheme
import org.commonmark.node.Code

/**
 * Inline node string builder for inline code span elements.
 * Extracts the code literal and applies the inline code style.
 */
class CodeNodeStringBuilder : IInlineNodeStringBuilder<Code> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: Code,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    ) {
        withStyle(markdownTheme.code.toSpanStyle()) {
            append(" ${node.literal} ")
        }
    }
}
