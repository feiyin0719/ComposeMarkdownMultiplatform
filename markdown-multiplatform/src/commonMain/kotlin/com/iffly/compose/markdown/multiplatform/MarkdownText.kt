package com.iffly.compose.markdown.multiplatform

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.iffly.compose.markdown.multiplatform.config.MarkdownRenderConfig
import com.iffly.compose.markdown.multiplatform.config.currentActionHandler
import com.iffly.compose.markdown.multiplatform.config.currentRenderRegistry
import com.iffly.compose.markdown.multiplatform.config.currentTheme
import com.iffly.compose.markdown.multiplatform.config.isShowNotSupported
import com.iffly.compose.markdown.multiplatform.render.MarkdownInlineView
import com.iffly.compose.markdown.multiplatform.render.TextSizeConstraints
import com.iffly.compose.markdown.multiplatform.render.markdownText
import com.iffly.compose.markdown.multiplatform.render.rememberNodeStringBuilderContext
import com.iffly.compose.markdown.multiplatform.widget.richtext.RichText
import kotlinx.collections.immutable.toImmutableMap
import org.commonmark.node.Node

/**
 * Text-based Markdown rendering component.
 *
 * Unlike [MarkdownView] which renders each block as a separate composable in a Column,
 * this component renders the entire Markdown document through a single [RichText] composable.
 * Text-containing blocks (Paragraph, Heading) are merged into a single [AnnotatedString],
 * enabling cross-paragraph text selection. Non-text blocks (code blocks, block quotes, lists, etc.)
 * are rendered as [EmbeddedRichTextInlineContent][RichTextInlineContent.EmbeddedRichTextInlineContent]
 * using existing [IBlockRenderer][com.iffly.compose.markdown.multiplatform.render.IBlockRenderer]
 * implementations.
 *
 * This is a **complementary** approach to [MarkdownView], best suited for documents where
 * continuous text selection across paragraphs is desired.
 *
 * @param text The raw markdown content to render.
 * @param modifier Modifier to be applied to the Markdown text.
 * @param markdownRenderConfig Configuration controlling parsing, theming, and rendering behavior.
 * @param actionHandler Optional handler for user interactions such as link clicks.
 * @param showNotSupported Whether to display placeholder text for unsupported markdown elements.
 * @param overflow How visual overflow should be handled.
 * @param softWrap Whether the text should wrap softly.
 * @param textAlign The alignment of the text.
 * @param maxLines The maximum number of lines to display.
 * @param minLines The minimum number of lines to display.
 * @param letterSpacing The spacing between letters.
 * @param textDecoration The text decoration to apply.
 * @param onTextLayout Callback invoked when the text layout is computed.
 *
 * @see MarkdownView
 */
@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    markdownRenderConfig: MarkdownRenderConfig =
        remember { MarkdownRenderConfig.Builder().build() },
    actionHandler: ActionHandler? = null,
    showNotSupported: Boolean = false,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    onTextLayout: (TextLayoutResult) -> Unit = {},
) {
    val markdownParser = markdownRenderConfig.markdownParser
    val rootNode =
        remember(text, markdownParser) {
            markdownParser.parse(text)
        }

    ProvideMarkdownLocals(
        markdownRenderConfig = markdownRenderConfig,
        actionHandler = actionHandler,
        showNotSupported = showNotSupported,
    ) {
        MarkdownTextContent(
            node = rootNode,
            modifier = modifier,
            overflow = overflow,
            softWrap = softWrap,
            textAlign = textAlign,
            maxLines = maxLines,
            minLines = minLines,
            letterSpacing = letterSpacing,
            textDecoration = textDecoration,
            onTextLayout = onTextLayout,
        )
    }
}

/**
 * Internal composable that builds a document-level
 * [AnnotatedString][androidx.compose.ui.text.AnnotatedString]
 * and renders it via [RichText].
 *
 * All node types are handled uniformly through [markdownText]: text blocks
 * (Paragraph, Heading) are merged directly into the AnnotatedString via their
 * registered [IInlineNodeStringBuilder][com.iffly.compose.markdown.multiplatform.render.IInlineNodeStringBuilder],
 * while other blocks are wrapped as embedded inline content by
 * [BlockRendererInlineStringBuilder][com.iffly.compose.markdown.multiplatform.render.BlockRendererInlineStringBuilder]
 * (lazily registered via [RenderRegistry.textModeRegistry][com.iffly.compose.markdown.multiplatform.render.RenderRegistry.textModeRegistry]).
 */
@Composable
private fun MarkdownTextContent(
    node: Node,
    modifier: Modifier = Modifier,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    onTextLayout: (TextLayoutResult) -> Unit = {},
) {
    BoxWithConstraints(modifier = modifier) {
        val theme = currentTheme()
        val baseRegistry = currentRenderRegistry()
        val renderRegistry = remember(baseRegistry) { baseRegistry.textModeRegistry() }
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
                textAlign = textAlign ?: TextAlign.Start,
            )

        val (text, inlineContentMap) =
            remember(
                node,
                theme,
                renderRegistry,
                isShowNotSupported,
                actionHandler,
                nodeStringBuilderContext,
            ) {
                markdownText(
                    node = node,
                    markdownTheme = theme,
                    renderRegistry = renderRegistry,
                    actionHandler = actionHandler,
                    indentLevel = 1,
                    isShowNotSupported = isShowNotSupported,
                    nodeStringBuilderContext = nodeStringBuilderContext,
                )
            }

        val richTextInlineContent =
            remember(inlineContentMap) {
                inlineContentMap
                    .mapNotNull { (key, value) ->
                        if (value is MarkdownInlineView.MarkdownRichTextInlineContent) {
                            key to value.inlineContent
                        } else {
                            null
                        }
                    }.toMap()
            }

        RichText(
            text = text,
            inlineContent = richTextInlineContent.toImmutableMap(),
            modifier =
                Modifier
                    .wrapContentHeight()
                    .widthIn(minWidth, maxWidth),
            style = theme.textStyle,
            overflow = overflow,
            softWrap = softWrap,
            textAlign = textAlign,
            maxLines = maxLines,
            minLines = minLines,
            letterSpacing = letterSpacing,
            textDecoration = textDecoration,
            onTextLayout = { _, result -> onTextLayout(result) },
        )
    }
}
