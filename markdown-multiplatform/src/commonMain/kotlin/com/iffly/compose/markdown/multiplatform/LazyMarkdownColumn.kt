package com.iffly.compose.markdown.multiplatform

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.iffly.compose.markdown.multiplatform.config.MarkdownRenderConfig
import com.iffly.compose.markdown.multiplatform.config.currentTheme
import com.iffly.compose.markdown.multiplatform.render.MarkdownContent
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser

/**
 * A Composable that displays markdown content using LazyColumn.
 * Parses markdown upfront and uses LazyColumn for efficient rendering
 * of the parsed nodes.
 *
 * @param text The markdown content as a String.
 * @param modifier Modifier to be applied to the LazyColumn.
 * @param markdownRenderConfig Configuration for rendering the markdown.
 * @param actionHandler An optional ActionHandler to handle actions within the markdown content.
 * @param showNotSupported Whether to show text for unsupported elements.
 * @param lazyListState The state of the LazyColumn for scroll control.
 */
@Composable
fun LazyMarkdownColumn(
    text: String,
    modifier: Modifier = Modifier,
    markdownRenderConfig: MarkdownRenderConfig =
        remember { MarkdownRenderConfig.Builder().build() },
    actionHandler: ActionHandler? = null,
    showNotSupported: Boolean = false,
    lazyListState: LazyListState = rememberLazyListState(),
) {
    val flavour = remember { GFMFlavourDescriptor() }
    val rootNode =
        remember(text) {
            MarkdownParser(flavour).buildMarkdownTreeFromString(text)
        }

    val children =
        remember(rootNode) {
            rootNode.children
        }

    ProvideMarkdownLocals(
        markdownRenderConfig = markdownRenderConfig,
        sourceText = text,
        actionHandler = actionHandler,
        showNotSupported = showNotSupported,
    ) {
        val theme = currentTheme()
        LazyColumn(
            modifier = modifier,
            state = lazyListState,
        ) {
            itemsIndexed(children, key = { index, node -> index }) { index, node ->
                MarkdownContent(
                    node = node,
                    modifier = Modifier,
                )
                if (index != children.lastIndex && theme.spacerTheme.showSpacer) {
                    Spacer(Modifier.height(theme.spacerTheme.spacerHeight))
                }
            }
        }
    }
}
