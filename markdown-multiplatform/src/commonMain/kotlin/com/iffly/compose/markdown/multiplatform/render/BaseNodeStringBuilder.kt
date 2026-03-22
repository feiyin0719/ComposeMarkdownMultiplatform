package com.iffly.compose.markdown.multiplatform.render

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import com.iffly.compose.markdown.multiplatform.ActionHandler
import com.iffly.compose.markdown.multiplatform.style.MarkdownTheme
import com.iffly.compose.markdown.multiplatform.util.nodeTextContent
import org.commonmark.node.Node

/**
 * Iterates over the children of a [parent] node and builds their inline annotated string
 * representations by delegating to the appropriate [IInlineNodeStringBuilder] for each child.
 * Falls back to appending raw content text or an unsupported placeholder if no builder is registered.
 */
fun AnnotatedString.Builder.buildChildNodeAnnotatedString(
    parent: Node,
    indentLevel: Int = 1,
    inlineContentMap: MutableMap<String, MarkdownInlineView>,
    markdownTheme: MarkdownTheme,
    renderRegistry: RenderRegistry,
    actionHandler: ActionHandler? = null,
    isShowNotSupported: Boolean,
    nodeStringBuilderContext: NodeStringBuilderContext,
    children: List<Node>? = null,
) {
    val childNodes = children ?: parent.childNodes()
    for (child in childNodes) {
        val customBuilder = renderRegistry.getInlineNodeStringBuilder(child)
        customBuilder?.buildMarkdownInlineNodeString(
            child,
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
                append("[Unsupported: ${child::class.simpleName}]")
            } else {
                append(child.nodeTextContent())
            }
        }
    }
}

/**
 * Returns the children of a Node as a List.
 */
fun Node.childNodes(): List<Node> {
    val result = mutableListOf<Node>()
    var child = firstChild
    while (child != null) {
        result.add(child)
        child = child.next
    }
    return result
}

/**
 * Base [IInlineNodeStringBuilder] implementation for composite nodes that contain child elements.
 * Applies optional [SpanStyle] and [ParagraphStyle] around the built child content.
 * Subclasses override [getSpanStyle] and/or [getParagraphStyle] to provide node-specific styling.
 */
open class CompositeChildNodeStringBuilder : IInlineNodeStringBuilder<Node> {
    open fun getSpanStyle(
        node: Node,
        markdownTheme: MarkdownTheme,
    ): SpanStyle? = null

    open fun getParagraphStyle(
        node: Node,
        markdownTheme: MarkdownTheme,
    ): ParagraphStyle? = null

    fun <R : Any> AnnotatedString.Builder.withSpanStyle(
        node: Node,
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
        node: Node,
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
        node: Node,
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
