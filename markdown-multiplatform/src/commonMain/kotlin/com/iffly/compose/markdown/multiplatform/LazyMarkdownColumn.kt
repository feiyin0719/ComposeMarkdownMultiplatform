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
import com.iffly.compose.markdown.multiplatform.config.currentRenderRegistry
import com.iffly.compose.markdown.multiplatform.config.currentTheme
import com.iffly.compose.markdown.multiplatform.render.MarkdownContent
import com.iffly.compose.markdown.multiplatform.render.childNodes

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
    val markdownParser = markdownRenderConfig.markdownParser
    val rootNode =
        remember(text, markdownParser) {
            markdownParser.parse(text)
        }

    val children =
        remember(rootNode) {
            rootNode.childNodes()
        }

    ProvideMarkdownLocals(
        markdownRenderConfig = markdownRenderConfig,
        actionHandler = actionHandler,
        showNotSupported = showNotSupported,
    ) {
        val theme = currentTheme()
        val renderRegistry = currentRenderRegistry()
        LazyColumn(
            modifier = modifier,
            state = lazyListState,
        ) {
            itemsIndexed(children, key = { index, node -> index }) { index, node ->
                val skip = renderRegistry.shouldSkipRender(node)
                if (!skip) {
                    MarkdownContent(
                        node = node,
                        modifier = Modifier,
                    )
                    if (index != children.lastIndex &&
                        theme.spacerTheme.showSpacer &&
                        renderRegistry.getBlockRenderer(node) != null
                    ) {
                        Spacer(Modifier.height(theme.spacerTheme.spacerHeight))
                    }
                }
            }
        }
    }
}
