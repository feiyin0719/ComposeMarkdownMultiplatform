package com.iffly.compose.markdown.multiplatform.core.renders

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.withLink
import com.iffly.compose.markdown.multiplatform.ActionHandler
import com.iffly.compose.markdown.multiplatform.MarkdownLinkInteractionListener
import com.iffly.compose.markdown.multiplatform.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.render.MarkdownInlineView
import com.iffly.compose.markdown.multiplatform.render.NodeStringBuilderContext
import com.iffly.compose.markdown.multiplatform.render.RenderRegistry
import com.iffly.compose.markdown.multiplatform.render.buildChildNodeAnnotatedString
import com.iffly.compose.markdown.multiplatform.style.MarkdownTheme
import com.iffly.compose.markdown.multiplatform.util.findChildOfType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getTextInNode

/**
 * Inline node string builder for inline link elements (`[text](url)`).
 *
 * Extracts the link destination URL and link text, applies a [LinkAnnotation] with
 * the configured link styles from [MarkdownTheme], and optionally attaches an
 * [ActionHandler]-based interaction listener for click handling.
 *
 * @see IInlineNodeStringBuilder
 */
class LinkNodeStringBuilder : IInlineNodeStringBuilder {
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
        val destinationNode = node.findChildOfType(MarkdownElementTypes.LINK_DESTINATION)
        val textNode = node.findChildOfType(MarkdownElementTypes.LINK_TEXT)
        val url = destinationNode?.getTextInNode(sourceText)?.toString() ?: ""

        val linkInteractionListener =
            actionHandler?.let {
                MarkdownLinkInteractionListener(actionHandler = it, node = node)
            }
        val linkAnnotation =
            LinkAnnotation.Url(
                url = url,
                styles = markdownTheme.link,
                linkInteractionListener = linkInteractionListener,
            )

        if (textNode != null) {
            withLink(linkAnnotation) {
                buildChildNodeAnnotatedString(
                    parent = textNode,
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
        } else {
            withLink(linkAnnotation) {
                append(url)
            }
        }
    }
}

/**
 * Inline node string builder for short reference link elements (`[text]`).
 *
 * Renders the link text content without resolving the reference, since reference
 * resolution is not yet supported.
 *
 * @see IInlineNodeStringBuilder
 */
class ShortReferenceLinkNodeStringBuilder : IInlineNodeStringBuilder {
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
        val textNode = node.findChildOfType(MarkdownElementTypes.LINK_TEXT)
        val linkText = textNode?.getTextInNode(sourceText)?.toString()?.removeSurrounding("[", "]") ?: ""
        // For short reference links, just render the text (reference resolution not supported yet)
        buildChildNodeAnnotatedString(
            parent = textNode ?: node,
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

/**
 * Inline node string builder for link definition elements (`[label]: url`).
 *
 * Link definitions do not produce any visible output; this builder intentionally
 * appends nothing.
 *
 * @see IInlineNodeStringBuilder
 */
class LinkDefinitionNodeStringBuilder : IInlineNodeStringBuilder {
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
        // Link definitions do not render any visible text
    }
}
