package com.iffly.compose.markdown.multiplatform.render

import com.iffly.compose.markdown.multiplatform.widget.richtext.RichTextInlineContent

sealed interface MarkdownInlineView {
    data class MarkdownRichTextInlineContent(
        val inlineContent: RichTextInlineContent,
    ) : MarkdownInlineView
}
