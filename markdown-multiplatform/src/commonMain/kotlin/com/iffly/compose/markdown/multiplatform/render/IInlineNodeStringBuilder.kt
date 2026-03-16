package com.iffly.compose.markdown.multiplatform.render

import androidx.compose.ui.text.AnnotatedString
import com.iffly.compose.markdown.multiplatform.ActionHandler
import com.iffly.compose.markdown.multiplatform.style.MarkdownTheme
import org.intellij.markdown.ast.ASTNode

/**
 * Interface for building inline annotated string content from an AST node.
 * Implementations handle specific inline element types such as bold, italic, links, code spans, etc.
 *
 * @see RenderRegistry
 */
interface IInlineNodeStringBuilder {
    /**
     * Builds an inline annotated string representation for the given AST node.
     *
     * @param node The AST node to process.
     * @param sourceText The raw markdown source text.
     * @param inlineContentMap Mutable map collecting inline content views keyed by placeholder ID.
     * @param markdownTheme The theme providing styling information.
     * @param actionHandler Optional handler for user interactions.
     * @param indentLevel The current indentation level.
     * @param isShowNotSupported Whether to display unsupported element placeholders.
     * @param renderRegistry The registry for looking up other inline builders.
     * @param nodeStringBuilderContext Context providing layout, style, and system information.
     */
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

/**
 * Convenience extension function that invokes [IInlineNodeStringBuilder.buildInlineNodeString]
 * with the given [AnnotatedString.Builder], avoiding the need for explicit `with(builder)` scoping at call sites.
 */
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
