package com.iffly.compose.markdown.multiplatform.render

import com.iffly.compose.markdown.multiplatform.widget.richtext.RichTextInlineContent

/**
 * Sealed interface representing inline views that can be embedded within annotated markdown text.
 * Used as values in the inline content map produced during markdown text building.
 */
sealed interface MarkdownInlineView {
    /**
     * An inline view backed by a [RichTextInlineContent], enabling composable content
     * to be embedded within rich text.
     *
     * @property inlineContent The rich text inline content to render.
     */
    data class MarkdownRichTextInlineContent(
        val inlineContent: RichTextInlineContent,
    ) : MarkdownInlineView
}
