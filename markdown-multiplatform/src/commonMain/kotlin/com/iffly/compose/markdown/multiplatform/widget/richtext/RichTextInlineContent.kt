package com.iffly.compose.markdown.multiplatform.widget.richtext

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.AnnotatedString.Range
import androidx.compose.ui.text.Placeholder

/**
 * Sealed interface representing inline content that can be embedded within rich text.
 *
 * @see EmbeddedRichTextInlineContent
 * @see StandaloneInlineContent
 */
sealed interface RichTextInlineContent {
    /**
     * Inline content that is embedded directly within the text flow using a placeholder.
     *
     * @param placeholder The placeholder defining the reserved space in the text layout.
     * @param adjustSizeByContent When true, the placeholder size is measured from the actual content.
     * @param content The composable content to render, receiving the annotation ID as a parameter.
     */
    @Immutable
    data class EmbeddedRichTextInlineContent(
        val placeholder: Placeholder,
        val adjustSizeByContent: Boolean = false,
        val content: @Composable (String) -> Unit,
    ) : RichTextInlineContent

    /**
     * Inline content that occupies its own block, rendered outside the text flow.
     *
     * @param modifier The modifier applied to the content.
     * @param content The composable content to render as a standalone block.
     */
    @Immutable
    data class StandaloneInlineContent(
        val modifier: Modifier = Modifier,
        val content: @Composable (modifier: Modifier) -> Unit,
    ) : RichTextInlineContent
}

private const val STANDALONE_INLINE_CONTENT_TAG =
    "com.iffly.compose.markdown.multiplatform.widget.richtext.StandaloneInlineTextContent"
private const val REPLACEMENT_CHAR = "\uFFFD"

/**
 * Appends a standalone inline content annotation to this [AnnotatedString.Builder].
 *
 * The annotation is tagged so that [RichText] can later split the text around it
 * and render standalone content blocks between text segments.
 *
 * @param id The unique identifier for the standalone inline content entry.
 * @param alternateText Replacement text inserted into the string (defaults to the Unicode replacement character).
 */
fun AnnotatedString.Builder.appendStandaloneInlineTextContent(
    id: String,
    alternateText: String = REPLACEMENT_CHAR,
) {
    require(alternateText.isNotEmpty())
    pushStringAnnotation(STANDALONE_INLINE_CONTENT_TAG, id)
    append(alternateText)
    pop()
}

/**
 * Retrieves all standalone inline content annotations within the specified range of this [AnnotatedString].
 *
 * @param start The start offset (inclusive) to search from.
 * @param end The end offset (exclusive) to search to.
 * @return A list of annotation ranges whose items are the standalone content IDs.
 */
fun AnnotatedString.getStandaloneInlineTextContentAnnotations(
    start: Int = 0,
    end: Int = this.length,
): List<Range<String>> =
    getStringAnnotations(
        tag = STANDALONE_INLINE_CONTENT_TAG,
        start = start,
        end = end,
    )
