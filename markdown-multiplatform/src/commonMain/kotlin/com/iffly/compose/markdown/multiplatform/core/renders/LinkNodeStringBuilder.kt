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
import org.commonmark.node.Image
import org.commonmark.node.Link

/**
 * Inline node string builder for Link nodes.
 * Extracts the link destination and builds linked text content.
 */
class LinkNodeStringBuilder : IInlineNodeStringBuilder<Link> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: Link,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    ) {
        val url = node.destination

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

        if (node.firstChild != null) {
            withLink(linkAnnotation) {
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
        } else {
            withLink(linkAnnotation) {
                append(url)
            }
        }
    }
}

/**
 * Inline node string builder for Image nodes.
 * Renders the alt text as linked text (image rendering handled by plugin).
 */
class ImageNodeStringBuilder : IInlineNodeStringBuilder<Image> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: Image,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    ) {
        if (node.firstChild != null) {
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
        } else {
            append(node.destination)
        }
    }
}
