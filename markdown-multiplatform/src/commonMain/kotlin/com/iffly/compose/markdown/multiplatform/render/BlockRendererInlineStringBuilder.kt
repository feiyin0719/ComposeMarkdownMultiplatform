package com.iffly.compose.markdown.multiplatform.render

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.iffly.compose.markdown.multiplatform.ActionHandler
import com.iffly.compose.markdown.multiplatform.style.MarkdownTheme
import com.iffly.compose.markdown.multiplatform.util.StringExt
import com.iffly.compose.markdown.multiplatform.util.nodeTextContent
import com.iffly.compose.markdown.multiplatform.widget.richtext.RichTextInlineContent
import org.commonmark.node.Document
import org.commonmark.node.Node

/**
 * An [IInlineNodeStringBuilder] that wraps an [IBlockRenderer] as inline content.
 *
 * When a block node (e.g. code block, blockquote, list) is encountered during
 * [markdownText] string building, this builder inserts an
 * [EmbeddedRichTextInlineContent][RichTextInlineContent.EmbeddedRichTextInlineContent]
 * with [adjustSizeByContent] set to `true`, and delegates the actual rendering to the
 * wrapped [blockRenderer].
 *
 * This enables the Text-based rendering path ([com.iffly.compose.markdown.multiplatform.MarkdownText])
 * to handle block nodes through the same [markdownText] pipeline without special-casing.
 *
 * @param T The type of node this builder handles.
 * @param blockRenderer The block renderer to delegate rendering to.
 *
 * @see IBlockRenderer
 * @see IInlineNodeStringBuilder
 */
class BlockRendererInlineStringBuilder<T : Node>(
    private val blockRenderer: IBlockRenderer<T>,
) : IInlineNodeStringBuilder<T> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: T,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    ) {
        val blockId = "block_${node.hashCode()}"
        inlineContentMap[blockId] =
            MarkdownInlineView.MarkdownRichTextInlineContent(
                RichTextInlineContent.EmbeddedRichTextInlineContent(
                    placeholder =
                        Placeholder(
                            width = 1.sp,
                            height = 1.sp,
                            placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
                        ),
                    adjustSizeByContent = true,
                ) {
                    blockRenderer.Invoke(node, Modifier.fillMaxWidth())
                },
            )
        appendInlineContent(blockId, REPLACEMENT_CHAR)
    }

    private companion object {
        const val REPLACEMENT_CHAR = "\uFFFD"
    }
}

/**
 * An [IInlineNodeStringBuilder] for [Document] nodes that iterates the document's
 * children and delegates each to the registered inline string builder.
 *
 * Text blocks (those with an [IInlineNodeStringBuilder], like Paragraph and Heading) are
 * merged directly into the [AnnotatedString]. Other blocks are handled by
 * [BlockRendererInlineStringBuilder] which inserts standalone inline content placeholders.
 *
 * @see BlockRendererInlineStringBuilder
 */
class DocumentInlineStringBuilder : IInlineNodeStringBuilder<Document> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: Document,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    ) {
        var child: Node? = node.firstChild
        while (child != null) {
            buildChildInlineNodeString(
                child = child,
                inlineContentMap = inlineContentMap,
                markdownTheme = markdownTheme,
                actionHandler = actionHandler,
                indentLevel = indentLevel,
                isShowNotSupported = isShowNotSupported,
                renderRegistry = renderRegistry,
                nodeStringBuilderContext = nodeStringBuilderContext,
            )
            if (child.next != null) {
                appendSpacer(
                    markdownTheme = markdownTheme,
                    nodeStringBuilderContext = nodeStringBuilderContext,
                )
            }
            child = child.next
        }
    }
}

private fun AnnotatedString.Builder.appendSpacer(
    markdownTheme: MarkdownTheme,
    nodeStringBuilderContext: NodeStringBuilderContext,
) {
    val spacerHeightSp = with(nodeStringBuilderContext.layoutContext.density) {
        markdownTheme.spacerTheme.spacerHeight.toSp().times(0.9)
    }
    withStyle(
        ParagraphStyle(
            lineHeight = spacerHeightSp,
            lineHeightStyle = LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Center,
                trim = LineHeightStyle.Trim.Both,
            ),
        ),
    ) {
        withStyle(
            SpanStyle(
                fontSize = spacerHeightSp,
                letterSpacing = 0.sp,
                baselineShift = BaselineShift(0f),
            ),
        ) {
            append(StringExt.FIGURE_SPACE)
        }
    }
}

private fun AnnotatedString.Builder.buildChildInlineNodeString(
    child: Node,
    inlineContentMap: MutableMap<String, MarkdownInlineView>,
    markdownTheme: MarkdownTheme,
    actionHandler: ActionHandler?,
    indentLevel: Int,
    isShowNotSupported: Boolean,
    renderRegistry: RenderRegistry,
    nodeStringBuilderContext: NodeStringBuilderContext,
) {
    val builder = renderRegistry.getInlineNodeStringBuilder(child)
    if (builder != null) {
        builder.buildMarkdownInlineNodeString(
            child,
            inlineContentMap,
            markdownTheme,
            indentLevel,
            actionHandler,
            renderRegistry,
            isShowNotSupported,
            this,
            nodeStringBuilderContext,
        )
    } else {
        if (isShowNotSupported) {
            append("[Unsupported: ${child::class.simpleName}]")
        } else {
            append(child.nodeTextContent())
        }
    }
}
