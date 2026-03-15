package com.iffly.compose.markdown.sample

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable

data class MarkdownExample(
    val title: String,
    val description: String,
    val content: @Composable (PaddingValues) -> Unit,
)

val markdownExamples =
    listOf(
        MarkdownExample(
            title = "Basic Syntax Example",
            description = "Demonstrates basic usage of standard Markdown syntax",
        ) { paddingValues ->
            BasicSyntaxExample(paddingValues)
        },
        MarkdownExample(
            title = "Custom Style Example",
            description = "Shows how to customize typography styles and themes",
        ) { paddingValues ->
            CustomStyleExample(paddingValues)
        },
        MarkdownExample(
            title = "Link Interaction Example",
            description = "Demonstrates custom link click handling",
        ) { paddingValues ->
            LinkInteractionExample(paddingValues)
        },
        MarkdownExample(
            title = "Dark Theme Example",
            description = "Demonstrates Markdown rendering effects in dark mode",
        ) { paddingValues ->
            DarkThemeExample(paddingValues)
        },
        MarkdownExample(
            title = "Dynamic Content Example",
            description = "Shows handling of dynamic content updates in Markdown",
        ) { paddingValues ->
            DynamicContentExample(paddingValues)
        },
        MarkdownExample(
            title = "Table Example",
            description = "Demonstrates GFM table rendering with the table plugin",
        ) { paddingValues ->
            TableExample(paddingValues)
        },
        MarkdownExample(
            title = "Image Example",
            description = "Demonstrates image rendering with the image plugin",
        ) { paddingValues ->
            ImageExample(paddingValues)
        },
        MarkdownExample(
            title = "HTML Example",
            description = "Demonstrates HTML rendering with the HTML plugin",
        ) { paddingValues ->
            HtmlExample(paddingValues)
        },
        MarkdownExample(
            title = "LazyMarkdownColumn Example",
            description = "Renders markdown using LazyColumn for efficient display",
        ) { paddingValues ->
            LazyMarkdownColumnExample(paddingValues)
        },
    )
