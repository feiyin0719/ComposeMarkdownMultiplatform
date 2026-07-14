package com.iffly.compose.markdown.multiplatform.core.renders

import androidx.compose.ui.text.AnnotatedString
import com.iffly.compose.markdown.multiplatform.ActionHandlerState
import com.iffly.compose.markdown.multiplatform.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.render.MarkdownInlineViewMap
import com.iffly.compose.markdown.multiplatform.render.NodeStringBuilderContext
import com.iffly.compose.markdown.multiplatform.render.RenderRegistry
import com.iffly.compose.markdown.multiplatform.style.MarkdownTheme
import com.iffly.compose.markdown.multiplatform.util.StringExt
import org.commonmark.node.HardLineBreak

class HardLineBreakNodeStringBuilder : IInlineNodeStringBuilder<HardLineBreak> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: HardLineBreak,
        inlineContentMap: MarkdownInlineViewMap,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandlerState?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    ) {
        append(StringExt.LINE_SEPARATOR)
    }
}
