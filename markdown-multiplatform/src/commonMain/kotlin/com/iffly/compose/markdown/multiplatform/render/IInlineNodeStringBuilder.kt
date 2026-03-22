package com.iffly.compose.markdown.multiplatform.render

import androidx.compose.ui.text.AnnotatedString
import com.iffly.compose.markdown.multiplatform.ActionHandler
import com.iffly.compose.markdown.multiplatform.style.MarkdownTheme
import org.commonmark.node.Node

interface IInlineNodeStringBuilder<T : Node> {
    fun AnnotatedString.Builder.buildInlineNodeString(
        node: T,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    )
}

fun IInlineNodeStringBuilder<*>.buildMarkdownInlineNodeString(
    node: Node,
    inlineContentMap: MutableMap<String, MarkdownInlineView>,
    markdownTheme: MarkdownTheme,
    indentLevel: Int,
    actionHandler: ActionHandler? = null,
    renderRegistry: RenderRegistry,
    isShowNotSupported: Boolean,
    builder: AnnotatedString.Builder,
    nodeStringBuilderContext: NodeStringBuilderContext,
) {
    @Suppress("UNCHECKED_CAST")
    val typedBuilder = this as IInlineNodeStringBuilder<Node>
    with(typedBuilder) {
        builder.buildInlineNodeString(
            node,
            inlineContentMap,
            markdownTheme,
            actionHandler,
            indentLevel,
            isShowNotSupported,
            renderRegistry,
            nodeStringBuilderContext,
        )
    }
}
