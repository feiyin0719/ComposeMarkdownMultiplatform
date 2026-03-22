package com.iffly.compose.markdown.multiplatform.render

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import com.iffly.compose.markdown.multiplatform.ActionHandler
import com.iffly.compose.markdown.multiplatform.config.currentActionHandler
import com.iffly.compose.markdown.multiplatform.config.currentRenderRegistry
import com.iffly.compose.markdown.multiplatform.config.currentTheme
import com.iffly.compose.markdown.multiplatform.config.isShowNotSupported
import com.iffly.compose.markdown.multiplatform.style.MarkdownTheme
import com.iffly.compose.markdown.multiplatform.util.isInQuoteBlock
import com.iffly.compose.markdown.multiplatform.util.nodeTextContent
import com.iffly.compose.markdown.multiplatform.widget.richtext.RichText
import kotlinx.collections.immutable.toImmutableMap
import org.commonmark.node.Node

/** Functional interface for custom rendering of inline markdown text composables. */
fun interface MarkdownTextRenderer {
    @Composable
    operator fun invoke(
        parent: Node,
        modifier: Modifier,
        textAlign: TextAlign,
        textStyle: TextStyle?,
    )
}

@Composable
fun MarkdownText(
    parent: Node,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    textStyle: TextStyle? = null,
) {
    val renderRegistry = currentRenderRegistry()
    renderRegistry.markdownTextRenderer?.invoke(
        parent = parent,
        modifier = modifier,
        textAlign = textAlign,
        textStyle = textStyle,
    ) ?: DefaultMarkdownText(
        parent = parent,
        modifier = modifier,
        textAlign = textAlign,
        textStyle = textStyle,
    )
}

@Composable
private fun DefaultMarkdownText(
    parent: Node,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    textStyle: TextStyle? = null,
) {
    BoxWithConstraints(modifier = modifier) {
        val theme = currentTheme()
        val renderRegistry = currentRenderRegistry()
        val actionHandler = currentActionHandler()
        val isShowNotSupported = isShowNotSupported()
        val nodeStringBuilderContext =
            rememberNodeStringBuilderContext(
                textSizeConstraints =
                    TextSizeConstraints(
                        maxWidth = maxWidth,
                        maxHeight = maxHeight,
                        minWidth = minWidth,
                        minHeight = minHeight,
                    ),
                textAlign = textAlign,
                textStyle = textStyle,
            )
        val (text, inlineContent) =
            remember(
                parent,
                theme,
                renderRegistry,
                isShowNotSupported,
                actionHandler,
                nodeStringBuilderContext,
            ) {
                markdownText(
                    parent,
                    theme,
                    renderRegistry,
                    actionHandler,
                    1,
                    isShowNotSupported,
                    nodeStringBuilderContext,
                )
            }
        val inlineContentMap =
            remember(inlineContent) {
                inlineContent
                    .mapNotNull { (key, value) ->
                        if (value is MarkdownInlineView.MarkdownRichTextInlineContent) {
                            key to value.inlineContent
                        } else {
                            null
                        }
                    }.toMap()
            }
        val isInQuote = parent.isInQuoteBlock()
        val mergedTextStyle =
            (textStyle ?: theme.textStyle).merge(
                theme.blockQuoteTheme.textStyle.takeIf { isInQuote },
            )
        RichText(
            text = text,
            inlineContent = inlineContentMap.toImmutableMap(),
            modifier =
                Modifier
                    .wrapContentHeight()
                    .widthIn(minWidth, maxWidth),
            textAlign = textAlign,
            style = mergedTextStyle,
        )
    }
}

fun markdownText(
    node: Node,
    markdownTheme: MarkdownTheme,
    renderRegistry: RenderRegistry,
    actionHandler: ActionHandler? = null,
    indentLevel: Int = 0,
    isShowNotSupported: Boolean,
    nodeStringBuilderContext: NodeStringBuilderContext,
): Pair<AnnotatedString, Map<String, MarkdownInlineView>> {
    val inlineContentMap = mutableMapOf<String, MarkdownInlineView>()

    val annotatedString =
        buildAnnotatedString {
            val buildNodeAnnotatedString =
                renderRegistry.getInlineNodeStringBuilder(node)
            if (buildNodeAnnotatedString != null) {
                buildNodeAnnotatedString.buildMarkdownInlineNodeString(
                    node,
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
                    append("[Unsupported: ${node::class.simpleName}]")
                } else {
                    append(node.nodeTextContent())
                }
            }
        }

    return Pair(annotatedString, inlineContentMap)
}
