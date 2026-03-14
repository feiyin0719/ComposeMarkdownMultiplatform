package com.iffly.compose.markdown.multiplatform

import com.iffly.compose.markdown.multiplatform.config.MarkdownRenderConfig
import com.iffly.compose.markdown.multiplatform.style.MarkdownTheme

// Convenience functions for creating MarkdownRenderConfig

fun markdownRenderConfig(
    markdownTheme: MarkdownTheme = MarkdownTheme(),
    block: MarkdownRenderConfig.Builder.() -> Unit = {},
): MarkdownRenderConfig =
    MarkdownRenderConfig
        .Builder()
        .markdownTheme(markdownTheme)
        .apply(block)
        .build()
