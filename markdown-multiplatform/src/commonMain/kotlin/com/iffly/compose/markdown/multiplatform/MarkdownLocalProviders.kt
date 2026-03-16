package com.iffly.compose.markdown.multiplatform

import com.iffly.compose.markdown.multiplatform.config.MarkdownRenderConfig
import com.iffly.compose.markdown.multiplatform.style.MarkdownTheme

/**
 * Convenience factory function for creating a [MarkdownRenderConfig] with a DSL-style builder.
 *
 * @param markdownTheme The theme to use for rendering markdown elements.
 * @param block A configuration block applied to the [MarkdownRenderConfig.Builder].
 * @return A fully constructed [MarkdownRenderConfig] instance.
 */
fun markdownRenderConfig(
    markdownTheme: MarkdownTheme = MarkdownTheme(),
    block: MarkdownRenderConfig.Builder.() -> Unit = {},
): MarkdownRenderConfig =
    MarkdownRenderConfig
        .Builder()
        .markdownTheme(markdownTheme)
        .apply(block)
        .build()
