package com.iffly.compose.markdown.multiplatform.render

import androidx.compose.ui.text.AnnotatedString
import com.iffly.compose.markdown.multiplatform.ActionHandler
import com.iffly.compose.markdown.multiplatform.style.MarkdownTheme
import org.intellij.markdown.ast.ASTNode

interface IInlineNodeStringBuilder {
    fun AnnotatedString.Builder.buildInlineNodeString(
        node: ASTNode,
        sourceText: String,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    )
}

fun IInlineNodeStringBuilder.buildMarkdownInlineNodeString(
    node: ASTNode,
    sourceText: String,
    inlineContentMap: MutableMap<String, MarkdownInlineView>,
    markdownTheme: MarkdownTheme,
    indentLevel: Int,
    actionHandler: ActionHandler? = null,
    renderRegistry: RenderRegistry,
    isShowNotSupported: Boolean,
    builder: AnnotatedString.Builder,
    nodeStringBuilderContext: NodeStringBuilderContext,
) {
    with(builder) {
        buildInlineNodeString(
            node,
            sourceText,
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
