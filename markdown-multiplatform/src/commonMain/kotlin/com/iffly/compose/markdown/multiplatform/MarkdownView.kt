package com.iffly.compose.markdown.multiplatform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import com.iffly.compose.markdown.multiplatform.config.LocalActionHandlerProvider
import com.iffly.compose.markdown.multiplatform.config.LocalMarkdownThemeProvider
import com.iffly.compose.markdown.multiplatform.config.LocalNodeDataMap
import com.iffly.compose.markdown.multiplatform.config.LocalParserProvider
import com.iffly.compose.markdown.multiplatform.config.LocalRenderDependencies
import com.iffly.compose.markdown.multiplatform.config.LocalRenderRegistryProvider
import com.iffly.compose.markdown.multiplatform.config.LocalShowNotSupportedProvider
import com.iffly.compose.markdown.multiplatform.config.MarkdownRenderConfig
import com.iffly.compose.markdown.multiplatform.render.MarkdownContent
import com.iffly.compose.markdown.multiplatform.render.RenderRegistry
import kotlinx.coroutines.CoroutineDispatcher
import org.commonmark.node.Node

/**
 * A Composable that renders markdown text into native Compose UI elements.
 * Parses the provided markdown string and renders it using the configured render pipeline.
 *
 * @param text The raw markdown content to render.
 * @param modifier Modifier to be applied to the root content layout.
 * @param markdownRenderConfig Configuration controlling parsing, theming, and rendering behavior.
 * Remember custom instances at the call site.
 * @param actionHandler Optional handler for user interactions such as link clicks and image clicks.
 * @param renderDependencies Dependencies available to custom renderers and node string builders.
 * @param showNotSupported Whether to display placeholder text for unsupported markdown elements.
 * @param isStreaming Whether [text] is an append-only partial stream. Setting it to `false`
 * forces a final full parse. If no streaming parser factory is configured, normal full parsing is
 * used regardless of this value.
 */
@Composable
fun MarkdownView(
    text: String,
    modifier: Modifier = Modifier,
    markdownRenderConfig: MarkdownRenderConfig =
        remember { MarkdownRenderConfig.Builder().build() },
    actionHandler: ActionHandler? = null,
    renderDependencies: Map<String, Any> = emptyMap(),
    showNotSupported: Boolean = false,
    isStreaming: Boolean = false,
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

    MarkdownViewNode(
        node = rootNode,
        modifier = modifier,
        markdownRenderConfig = markdownRenderConfig,
        actionHandler = actionHandler,
        renderDependencies = renderDependencies,
        showNotSupported = showNotSupported,
    )
}

/** Asynchronously parses [text] on [parseDispatcher] before rendering it. */
@Composable
fun MarkdownView(
    text: String,
    parseDispatcher: CoroutineDispatcher,
    modifier: Modifier = Modifier,
    markdownRenderConfig: MarkdownRenderConfig =
        remember { MarkdownRenderConfig.Builder().build() },
    actionHandler: ActionHandler? = null,
    renderDependencies: Map<String, Any> = emptyMap(),
    showNotSupported: Boolean = false,
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
            MarkdownViewNode(
                node = state.node,
                modifier = modifier,
                markdownRenderConfig = markdownRenderConfig,
                actionHandler = actionHandler,
                renderDependencies = renderDependencies,
                showNotSupported = showNotSupported,
            )
        }
    }
}

@Composable
private fun MarkdownViewNode(
    node: Node,
    markdownRenderConfig: MarkdownRenderConfig,
    actionHandler: ActionHandler?,
    renderDependencies: Map<String, Any>,
    showNotSupported: Boolean,
    modifier: Modifier = Modifier,
) {
    ProvideMarkdownLocals(
        markdownRenderConfig = markdownRenderConfig,
        actionHandler = actionHandler,
        renderDependencies = renderDependencies,
        showNotSupported = showNotSupported,
    ) {
        MarkdownContent(
            node = node,
            modifier = modifier,
        )
    }
}

/**
 * Provides markdown-related [CompositionLocalProvider] values to the composition tree.
 * This sets up the theme, parser, render registry, and action handler
 * so that child composables can access them via composition locals.
 *
 * @param markdownRenderConfig The render configuration supplying theme, parser, and registry.
 * @param actionHandler Optional handler for user interaction events.
 * @param renderDependencies Dependencies available to custom renderers and node string builders.
 * @param renderRegistry Registry exposed to descendants. Defaults to the registry from [markdownRenderConfig].
 * @param showNotSupported Whether to show unsupported element placeholders.
 * @param content The composable content that will have access to the provided locals.
 */
@Composable
fun ProvideMarkdownLocals(
    markdownRenderConfig: MarkdownRenderConfig,
    actionHandler: ActionHandler? = null,
    renderDependencies: Map<String, Any> = emptyMap(),
    showNotSupported: Boolean = false,
    renderRegistry: RenderRegistry = markdownRenderConfig.renderRegistry,
    content: @Composable () -> Unit,
) {
    val nodeDataMap = remember { mutableStateMapOf<Node, Any>() }
    val actionHandlerState = rememberUpdatedState(actionHandler)
    CompositionLocalProvider(
        LocalMarkdownThemeProvider provides markdownRenderConfig.markdownTheme,
        LocalParserProvider provides markdownRenderConfig.markdownParser,
        LocalRenderRegistryProvider provides renderRegistry,
        LocalActionHandlerProvider provides actionHandlerState,
        LocalRenderDependencies provides renderDependencies,
        LocalShowNotSupportedProvider provides showNotSupported,
        LocalNodeDataMap provides nodeDataMap,
    ) {
        content()
    }
}
