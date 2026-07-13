package com.iffly.compose.markdown.multiplatform

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.iffly.compose.markdown.multiplatform.config.MarkdownRenderConfig
import com.iffly.compose.markdown.multiplatform.config.currentTheme
import com.iffly.compose.markdown.multiplatform.render.TextSizeConstraints
import com.iffly.compose.markdown.multiplatform.render.rememberMarkdownAnnotatedStringResult
import com.iffly.compose.markdown.multiplatform.widget.richtext.RichText
import kotlinx.coroutines.CoroutineDispatcher
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
 * Remember custom instances at the call site.
 * @param actionHandler Optional handler for user interactions such as link clicks.
 * @param renderDependencies Dependencies available to custom renderers and node string builders.
 * @param showNotSupported Whether to display placeholder text for unsupported markdown elements.
 * @param overflow How visual overflow should be handled.
 * @param softWrap Whether the text should wrap softly.
 * @param textAlign The alignment of the text.
 * @param maxLines The maximum number of lines to display.
 * @param minLines The minimum number of lines to display.
 * @param letterSpacing The spacing between letters.
 * @param textDecoration The text decoration to apply.
 * @param isStreaming Whether [text] is an append-only partial stream. Setting it to `false`
 * forces a final full parse. If no streaming parser factory is configured, normal full parsing is
 * used regardless of this value.
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
    renderDependencies: Map<String, Any> = emptyMap(),
    showNotSupported: Boolean = false,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    isStreaming: Boolean = false,
    onTextLayout: (TextLayoutResult) -> Unit = {},
) {
    val streamingParser =
        remember(markdownRenderConfig) {
            markdownRenderConfig.createStreamingMarkdownParser()
        }
    val rootNode =
        rememberMarkdownNode(
            text = text,
            isStreaming = isStreaming,
            streamingParser = streamingParser,
            fallbackParser = markdownRenderConfig.markdownParser,
        )

    MarkdownTextNode(
        node = rootNode,
        modifier = modifier,
        markdownRenderConfig = markdownRenderConfig,
        actionHandler = actionHandler,
        renderDependencies = renderDependencies,
        showNotSupported = showNotSupported,
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

/** Asynchronously parses [text] on [parseDispatcher] before rendering text-mode Markdown. */
@Composable
fun MarkdownText(
    text: String,
    parseDispatcher: CoroutineDispatcher,
    modifier: Modifier = Modifier,
    markdownRenderConfig: MarkdownRenderConfig =
        remember { MarkdownRenderConfig.Builder().build() },
    actionHandler: ActionHandler? = null,
    renderDependencies: Map<String, Any> = emptyMap(),
    showNotSupported: Boolean = false,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    isStreaming: Boolean = false,
    onLoading: (@Composable () -> Unit)? = null,
    onError: (@Composable (Throwable) -> Unit)? = null,
) {
    val streamingParser =
        remember(markdownRenderConfig) {
            markdownRenderConfig.createStreamingMarkdownParser()
        }
    val parseState by
        rememberAsyncMarkdownNode(
            text = text,
            isStreaming = isStreaming,
            streamingParser = streamingParser,
            fallbackParser = markdownRenderConfig.markdownParser,
            dispatcher = parseDispatcher,
        )
    when (val state = parseState) {
        MarkdownParseState.Loading -> {
            onLoading?.invoke()
        }

        is MarkdownParseState.Error -> {
            onError?.invoke(state.throwable)
        }

        is MarkdownParseState.Success -> {
            MarkdownTextNode(
                node = state.node,
                modifier = modifier,
                markdownRenderConfig = markdownRenderConfig,
                actionHandler = actionHandler,
                renderDependencies = renderDependencies,
                showNotSupported = showNotSupported,
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
}

@Composable
private fun MarkdownTextNode(
    node: Node,
    markdownRenderConfig: MarkdownRenderConfig,
    actionHandler: ActionHandler?,
    renderDependencies: Map<String, Any>,
    showNotSupported: Boolean,
    overflow: TextOverflow,
    softWrap: Boolean,
    textAlign: TextAlign?,
    maxLines: Int,
    minLines: Int,
    letterSpacing: TextUnit,
    textDecoration: TextDecoration?,
    modifier: Modifier = Modifier,
    onTextLayout: (TextLayoutResult) -> Unit = {},
) {
    val textModeRegistry =
        remember(markdownRenderConfig.renderRegistry) {
            markdownRenderConfig.renderRegistry.textModeRegistry()
        }

    ProvideMarkdownLocals(
        markdownRenderConfig = markdownRenderConfig,
        actionHandler = actionHandler,
        renderDependencies = renderDependencies,
        showNotSupported = showNotSupported,
        renderRegistry = textModeRegistry,
    ) {
        MarkdownTextContent(
            node = node,
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
        val textSizeConstraints =
            remember(maxWidth, maxHeight, minWidth, minHeight) {
                TextSizeConstraints(
                    maxWidth = maxWidth,
                    maxHeight = maxHeight,
                    minWidth = minWidth,
                    minHeight = minHeight,
                )
            }
        val result =
            rememberMarkdownAnnotatedStringResult(
                node = node,
                textSizeConstraints = textSizeConstraints,
                textAlign = textAlign ?: TextAlign.Start,
            )
        val richTextOnTextLayout =
            remember(onTextLayout) {
                { _: Int, result: TextLayoutResult -> onTextLayout(result) }
            }

        RichText(
            text = result.text,
            inlineContent = result.inlineContent,
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
            onTextLayout = richTextOnTextLayout,
        )
    }
}
