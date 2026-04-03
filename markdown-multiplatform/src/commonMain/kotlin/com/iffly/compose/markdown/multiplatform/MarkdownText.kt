package com.iffly.compose.markdown.multiplatform

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import com.iffly.compose.markdown.multiplatform.config.MarkdownRenderConfig
import com.iffly.compose.markdown.multiplatform.config.currentActionHandler
import com.iffly.compose.markdown.multiplatform.config.currentRenderRegistry
import com.iffly.compose.markdown.multiplatform.config.currentTheme
import com.iffly.compose.markdown.multiplatform.config.isShowNotSupported
import com.iffly.compose.markdown.multiplatform.render.MarkdownContent
import com.iffly.compose.markdown.multiplatform.render.MarkdownInlineView
import com.iffly.compose.markdown.multiplatform.render.NodeStringBuilderContext
import com.iffly.compose.markdown.multiplatform.render.RenderRegistry
import com.iffly.compose.markdown.multiplatform.render.TextSizeConstraints
import com.iffly.compose.markdown.multiplatform.render.markdownText
import com.iffly.compose.markdown.multiplatform.render.rememberNodeStringBuilderContext
import com.iffly.compose.markdown.multiplatform.style.MarkdownTheme
import com.iffly.compose.markdown.multiplatform.widget.richtext.RichText
import com.iffly.compose.markdown.multiplatform.widget.richtext.RichTextInlineContent
import com.iffly.compose.markdown.multiplatform.widget.richtext.appendStandaloneInlineTextContent
import kotlinx.collections.immutable.toImmutableMap
import org.commonmark.node.Heading
import org.commonmark.node.Node
import org.commonmark.node.Paragraph

/**
 * Text-based Markdown rendering component.
 *
 * Unlike [MarkdownView] which renders each block as a separate composable in a Column,
 * this component renders the entire Markdown document through a single [RichText] composable.
 * Text-containing blocks (Paragraph, Heading) are merged into a single [AnnotatedString],
 * enabling cross-paragraph text selection. Non-text blocks (code blocks, block quotes, lists, etc.)
 * are rendered as [StandaloneInlineContent][RichTextInlineContent.StandaloneInlineContent]
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
        MarkdownTextContent(rootNode, modifier)
    }
}

/**
 * Internal composable that builds a document-level [AnnotatedString] and renders it
 * via [RichText]. Text blocks (Paragraph, Heading) are merged directly into the
 * AnnotatedString; other blocks are wrapped as [StandaloneInlineContent]
 * [RichTextInlineContent.StandaloneInlineContent] delegating to their registered
 * [IBlockRenderer][com.iffly.compose.markdown.multiplatform.render.IBlockRenderer].
 */
@Composable
private fun MarkdownTextContent(
    node: Node,
    modifier: Modifier = Modifier,
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
                textAlign = TextAlign.Start,
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
                buildDocumentAnnotatedString(
                    document = node,
                    theme = theme,
                    renderRegistry = renderRegistry,
                    actionHandler = actionHandler,
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
        )
    }
}

/**
 * Walks the document AST and builds a single [AnnotatedString] with inline content.
 *
 * - [Paragraph] and [Heading] nodes: their inline content is built via [markdownText] and
 *   appended directly into the AnnotatedString (enabling cross-paragraph text selection).
 * - Other block nodes: inserted as [StandaloneInlineContent]
 *   [RichTextInlineContent.StandaloneInlineContent] placeholders.
 *   Their rendering delegates to the registered
 *   [IBlockRenderer][com.iffly.compose.markdown.multiplatform.render.IBlockRenderer].
 */
private fun buildDocumentAnnotatedString(
    document: Node,
    theme: MarkdownTheme,
    renderRegistry: RenderRegistry,
    actionHandler: ActionHandler?,
    isShowNotSupported: Boolean,
    nodeStringBuilderContext: NodeStringBuilderContext,
): Pair<AnnotatedString, Map<String, MarkdownInlineView>> {
    val inlineContentMap = mutableMapOf<String, MarkdownInlineView>()

    val annotatedString =
        buildAnnotatedString {
            var child = document.firstChild
            var isFirst = true

            while (child != null) {
                if (!isFirst) {
                    append('\n')
                }

                when (child) {
                    is Paragraph, is Heading -> {
                        val (childText, childInlines) =
                            markdownText(
                                node = child,
                                markdownTheme = theme,
                                renderRegistry = renderRegistry,
                                actionHandler = actionHandler,
                                indentLevel = 1,
                                isShowNotSupported = isShowNotSupported,
                                nodeStringBuilderContext = nodeStringBuilderContext,
                            )
                        append(childText)
                        inlineContentMap.putAll(childInlines)
                    }

                    else -> {
                        val blockId = "block_${child.hashCode()}"
                        val blockNode = child
                        appendStandaloneInlineTextContent(blockId)
                        inlineContentMap[blockId] =
                            MarkdownInlineView.MarkdownRichTextInlineContent(
                                RichTextInlineContent.StandaloneInlineContent(
                                    modifier = Modifier.fillMaxWidth(),
                                ) { mod ->
                                    MarkdownContent(
                                        node = blockNode,
                                        modifier = mod,
                                    )
                                },
                            )
                    }
                }

                isFirst = false
                child = child.next
            }
        }

    return Pair(annotatedString, inlineContentMap)
}
