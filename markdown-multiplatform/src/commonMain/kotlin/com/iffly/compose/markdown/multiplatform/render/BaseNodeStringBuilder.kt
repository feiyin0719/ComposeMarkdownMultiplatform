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

/**
 * Iterates over the children of a [parent] AST node and builds their inline annotated string
 * representations by delegating to the appropriate [IInlineNodeStringBuilder] for each child.
 * Falls back to appending raw content text or an unsupported placeholder if no builder is registered.
 *
 * @param parent The parent AST node whose children will be processed.
 * @param sourceText The raw markdown source text.
 * @param indentLevel The current indentation level.
 * @param inlineContentMap Mutable map collecting inline content views.
 * @param markdownTheme The theme providing styling information.
 * @param renderRegistry The registry for looking up inline builders.
 * @param actionHandler Optional handler for user interactions.
 * @param isShowNotSupported Whether to display unsupported element placeholders.
 * @param nodeStringBuilderContext Context providing layout, style, and system information.
 * @param children The list of child nodes to process; defaults to [parent]'s children.
 */
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

/**
 * Base [IInlineNodeStringBuilder] implementation for composite AST nodes that contain child elements.
 * Applies optional [SpanStyle] and [ParagraphStyle] around the built child content.
 * Subclasses override [getSpanStyle] and/or [getParagraphStyle] to provide node-specific styling.
 */
open class CompositeChildNodeStringBuilder : IInlineNodeStringBuilder {
    /**
     * Returns an optional [SpanStyle] to apply to the node's inline content.
     *
     * @param node The AST node being rendered.
     * @param markdownTheme The current markdown theme.
     * @return A [SpanStyle] to wrap the content, or null if no span styling is needed.
     */
    open fun getSpanStyle(
        node: ASTNode,
        markdownTheme: MarkdownTheme,
    ): SpanStyle? = null

    /**
     * Returns an optional [ParagraphStyle] to apply to the node's inline content.
     *
     * @param node The AST node being rendered.
     * @param markdownTheme The current markdown theme.
     * @return A [ParagraphStyle] to wrap the content, or null if no paragraph styling is needed.
     */
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
