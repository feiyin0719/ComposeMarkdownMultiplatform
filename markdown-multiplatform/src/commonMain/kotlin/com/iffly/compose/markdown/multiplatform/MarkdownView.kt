package com.iffly.compose.markdown.multiplatform

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.iffly.compose.markdown.multiplatform.config.LocalActionHandlerProvider
import com.iffly.compose.markdown.multiplatform.config.LocalMarkdownThemeProvider
import com.iffly.compose.markdown.multiplatform.config.LocalParserProvider
import com.iffly.compose.markdown.multiplatform.config.LocalRenderRegistryProvider
import com.iffly.compose.markdown.multiplatform.config.LocalShowNotSupportedProvider
import com.iffly.compose.markdown.multiplatform.config.LocalSourceTextProvider
import com.iffly.compose.markdown.multiplatform.config.MarkdownRenderConfig
import com.iffly.compose.markdown.multiplatform.render.MarkdownContent
import org.intellij.markdown.ast.ASTNode

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
        sourceText = text,
        actionHandler = actionHandler,
        showNotSupported = showNotSupported,
    ) {
        MarkdownContent(
            node = rootNode,
            modifier = modifier,
        )
    }
}

@Composable
fun ProvideMarkdownLocals(
    markdownRenderConfig: MarkdownRenderConfig,
    sourceText: String,
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
        LocalSourceTextProvider provides sourceText,
    ) {
        content()
    }
}
