package com.iffly.compose.markdown.multiplatform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.iffly.compose.markdown.multiplatform.config.LocalActionHandlerProvider
import com.iffly.compose.markdown.multiplatform.config.LocalMarkdownThemeProvider
import com.iffly.compose.markdown.multiplatform.config.LocalParserProvider
import com.iffly.compose.markdown.multiplatform.config.LocalRenderRegistryProvider
import com.iffly.compose.markdown.multiplatform.config.LocalShowNotSupportedProvider
import com.iffly.compose.markdown.multiplatform.config.MarkdownRenderConfig
import com.iffly.compose.markdown.multiplatform.render.MarkdownContent

/**
 * A Composable that renders markdown text into native Compose UI elements.
 * Parses the provided markdown string and renders it using the configured render pipeline.
 *
 * @param text The raw markdown content to render.
 * @param modifier Modifier to be applied to the root content layout.
 * @param markdownRenderConfig Configuration controlling parsing, theming, and rendering behavior.
 * @param actionHandler Optional handler for user interactions such as link clicks and image clicks.
 * @param showNotSupported Whether to display placeholder text for unsupported markdown elements.
 */
@Composable
fun MarkdownView(
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
        MarkdownContent(
            node = rootNode,
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
 * @param showNotSupported Whether to show unsupported element placeholders.
 * @param content The composable content that will have access to the provided locals.
 */
@Composable
fun ProvideMarkdownLocals(
    markdownRenderConfig: MarkdownRenderConfig,
    actionHandler: ActionHandler? = null,
    showNotSupported: Boolean = false,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalMarkdownThemeProvider provides markdownRenderConfig.markdownTheme,
        LocalParserProvider provides markdownRenderConfig.markdownParser,
        LocalRenderRegistryProvider provides markdownRenderConfig.renderRegistry,
        LocalActionHandlerProvider provides actionHandler,
        LocalShowNotSupportedProvider provides showNotSupported,
    ) {
        content()
    }
}
