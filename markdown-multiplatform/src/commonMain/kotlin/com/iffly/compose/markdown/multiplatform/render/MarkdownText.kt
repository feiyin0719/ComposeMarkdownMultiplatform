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
import com.iffly.compose.markdown.multiplatform.config.currentSourceText
import com.iffly.compose.markdown.multiplatform.config.currentTheme
import com.iffly.compose.markdown.multiplatform.config.isShowNotSupported
import com.iffly.compose.markdown.multiplatform.style.MarkdownTheme
import com.iffly.compose.markdown.multiplatform.util.contentText
import com.iffly.compose.markdown.multiplatform.util.isInQuoteBlock
import com.iffly.compose.markdown.multiplatform.widget.richtext.RichText
import kotlinx.collections.immutable.toImmutableMap
import org.intellij.markdown.ast.ASTNode

/** Functional interface for custom rendering of inline markdown text composables. */
fun interface MarkdownTextRenderer {
    @Composable
    operator fun invoke(
        parent: ASTNode,
        sourceText: String,
        modifier: Modifier,
        textAlign: TextAlign,
        textStyle: TextStyle?,
    )
}

/**
 * Renders an AST node as styled inline text using the registered [MarkdownTextRenderer],
 * or falls back to the default implementation.
 *
 * @param parent The AST node whose inline content is rendered as text.
 * @param modifier Modifier to apply to the text layout.
 * @param textAlign Horizontal text alignment.
 * @param textStyle Optional text style override; merged with the theme's default text style.
 */
@Composable
fun MarkdownText(
    parent: ASTNode,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    textStyle: TextStyle? = null,
) {
    val renderRegistry = currentRenderRegistry()
    val sourceText = currentSourceText()
    renderRegistry.markdownTextRenderer?.invoke(
        parent = parent,
        sourceText = sourceText,
        modifier = modifier,
        textAlign = textAlign,
        textStyle = textStyle,
    ) ?: DefaultMarkdownText(
        parent = parent,
        sourceText = sourceText,
        modifier = modifier,
        textAlign = textAlign,
        textStyle = textStyle,
    )
}

@Composable
private fun DefaultMarkdownText(
    parent: ASTNode,
    sourceText: String,
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
                sourceText,
                theme,
                renderRegistry,
                isShowNotSupported,
                actionHandler,
                nodeStringBuilderContext,
            ) {
                markdownText(
                    parent,
                    sourceText,
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

/**
 * Builds an [AnnotatedString] and associated inline content map from an AST node.
 * This is the core function that traverses the inline node tree and produces styled text
 * with embedded inline content (e.g., images, code spans).
 *
 * @param node The AST node to convert to an annotated string.
 * @param sourceText The raw markdown source text.
 * @param markdownTheme The theme providing text styles.
 * @param renderRegistry The registry of inline node string builders.
 * @param actionHandler Optional handler for user interaction events.
 * @param indentLevel The current indentation level for nested elements.
 * @param isShowNotSupported Whether to display placeholder text for unsupported elements.
 * @param nodeStringBuilderContext Context providing layout, style, and system information.
 * @return A pair of the built [AnnotatedString] and a map of inline content keyed by placeholder ID.
 */
fun markdownText(
    node: ASTNode,
    sourceText: String,
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
                renderRegistry.getInlineNodeStringBuilder(node.type)
            if (buildNodeAnnotatedString != null) {
                buildNodeAnnotatedString.buildMarkdownInlineNodeString(
                    node,
                    sourceText,
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
                    append("[Unsupported: ${node.type}]")
                } else {
                    append(node.contentText(sourceText))
                }
            }
        }

    return Pair(annotatedString, inlineContentMap)
}
