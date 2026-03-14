package com.iffly.compose.markdown.multiplatform.render

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import com.iffly.compose.markdown.multiplatform.ActionHandler
import com.iffly.compose.markdown.multiplatform.style.MarkdownTheme
import com.iffly.compose.markdown.multiplatform.util.contentText
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode

fun AnnotatedString.Builder.buildChildNodeAnnotatedString(
    parent: ASTNode,
    sourceText: String,
    indentLevel: Int = 1,
    inlineContentMap: MutableMap<String, MarkdownInlineView>,
    markdownTheme: MarkdownTheme,
    renderRegistry: RenderRegistry,
    actionHandler: ActionHandler? = null,
    isShowNotSupported: Boolean,
    nodeStringBuilderContext: NodeStringBuilderContext,
    children: List<ASTNode> = parent.children,
) {
    for (child in children) {
        val customBuilder =
            renderRegistry.getInlineNodeStringBuilder(child.type)
        customBuilder?.buildMarkdownInlineNodeString(
            child,
            sourceText,
            inlineContentMap,
            markdownTheme,
            indentLevel,
            actionHandler,
            renderRegistry,
            isShowNotSupported,
            this,
            nodeStringBuilderContext,
        ) ?: run {
            if (isShowNotSupported) {
                append("[Unsupported: ${child.type}]")
            } else {
                append(child.contentText(sourceText))
            }
        }
    }
}

open class CompositeChildNodeStringBuilder : IInlineNodeStringBuilder {
    open fun getSpanStyle(
        node: ASTNode,
        markdownTheme: MarkdownTheme,
    ): SpanStyle? = null

    open fun getParagraphStyle(
        node: ASTNode,
        markdownTheme: MarkdownTheme,
    ): ParagraphStyle? = null

    fun <R : Any> AnnotatedString.Builder.withSpanStyle(
        node: ASTNode,
        markdownTheme: MarkdownTheme,
        content: AnnotatedString.Builder.() -> R,
    ) {
        val style = getSpanStyle(node, markdownTheme)
        if (style != null) {
            withStyle(style) {
                content()
            }
        } else {
            content()
        }
    }

    fun <R : Any> AnnotatedString.Builder.withParagraphStyle(
        node: ASTNode,
        markdownTheme: MarkdownTheme,
        content: AnnotatedString.Builder.() -> R,
    ) {
        val style = getParagraphStyle(node, markdownTheme)
        if (style != null) {
            withStyle(style) {
                content()
            }
        } else {
            content()
        }
    }

    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: ASTNode,
        sourceText: String,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    ) {
        withParagraphStyle(node = node, markdownTheme = markdownTheme) {
            withSpanStyle(node = node, markdownTheme = markdownTheme) {
                buildChildNodeAnnotatedString(
                    parent = node,
                    sourceText = sourceText,
                    indentLevel = indentLevel,
                    inlineContentMap = inlineContentMap,
                    markdownTheme = markdownTheme,
                    renderRegistry = renderRegistry,
                    actionHandler = actionHandler,
                    isShowNotSupported = isShowNotSupported,
                    nodeStringBuilderContext = nodeStringBuilderContext,
                )
            }
        }
    }
}
