package com.iffly.compose.markdown.multiplatform.widget.richtext

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.AnnotatedString.Range
import androidx.compose.ui.text.Placeholder

sealed interface RichTextInlineContent {
    @Immutable
    data class EmbeddedRichTextInlineContent(
        val placeholder: Placeholder,
        val adjustSizeByContent: Boolean = false,
        val content: @Composable (String) -> Unit,
    ) : RichTextInlineContent

    @Immutable
    data class StandaloneInlineContent(
        val modifier: Modifier = Modifier,
        val content: @Composable (modifier: Modifier) -> Unit,
    ) : RichTextInlineContent
}

private const val STANDALONE_INLINE_CONTENT_TAG =
    "com.iffly.compose.markdown.multiplatform.widget.richtext.StandaloneInlineTextContent"
private const val REPLACEMENT_CHAR = "\uFFFD"

fun AnnotatedString.Builder.appendStandaloneInlineTextContent(
    id: String,
    alternateText: String = REPLACEMENT_CHAR,
) {
    require(alternateText.isNotEmpty())
    pushStringAnnotation(STANDALONE_INLINE_CONTENT_TAG, id)
    append(alternateText)
    pop()
}

fun AnnotatedString.getStandaloneInlineTextContentAnnotations(
    start: Int = 0,
    end: Int = this.length,
): List<Range<String>> =
    getStringAnnotations(
        tag = STANDALONE_INLINE_CONTENT_TAG,
        start = start,
        end = end,
    )
