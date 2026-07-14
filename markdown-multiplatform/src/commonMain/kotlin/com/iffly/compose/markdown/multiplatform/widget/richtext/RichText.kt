package com.iffly.compose.markdown.multiplatform.widget.richtext

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.util.fastForEach
import com.iffly.compose.markdown.multiplatform.util.StringExt
import com.iffly.compose.markdown.multiplatform.widget.SelectionFormatText
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf

/**
 * A composable that renders rich text with support for both inline and standalone inline content.
 *
 * The text is split into segments around standalone inline content annotations, and each segment
 * is rendered in a vertical [Column]. Embedded inline content (e.g., inline images or icons)
 * is delegated to [AdaptiveInlineContentText].
 *
 * @param text The annotated string to display, possibly containing inline content annotations.
 * @param inlineContent A map of inline content keyed by their annotation IDs.
 * @param onTextLayout Callback invoked when the text layout is computed.
 * The first parameter is the segment index (0-based) within this RichText,
 * the second is the [TextLayoutResult].
 * @param style The default text style to apply.
 * @see RichTextInlineContent
 * @see AdaptiveInlineContentText
 */
@Composable
fun RichText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    inlineContent: ImmutableMap<String, RichTextInlineContent> = persistentMapOf(),
    onTextLayout: ((Int, TextLayoutResult) -> Unit)? = null,
    style: TextStyle = LocalTextStyle.current,
) {
    val (standaloneInlineContent, inlineTextContent) =
        remember(inlineContent) { groupRichTextInlineContent(inlineContent) }
    val textSegments =
        rememberRichTextSegment(
            text = text,
            standaloneInlineContentKeys = standaloneInlineContent.keys,
        )

    Column(modifier = modifier) {
        var textSegmentIndex = 0
        textSegments.fastForEach {
            when (it) {
                is RichTextSegment.Text -> {
                    val currentIndex = textSegmentIndex
                    textSegmentIndex++
                    val segmentOnTextLayout = rememberSegmentOnTextLayout(currentIndex, onTextLayout)
                    AdaptiveInlineContentText(
                        text = it.text,
                        color = color,
                        fontSize = fontSize,
                        fontStyle = fontStyle,
                        fontWeight = fontWeight,
                        fontFamily = fontFamily,
                        letterSpacing = letterSpacing,
                        textDecoration = textDecoration,
                        textAlign = textAlign,
                        lineHeight = lineHeight,
                        overflow = overflow,
                        softWrap = softWrap,
                        maxLines = maxLines,
                        minLines = minLines,
                        inlineContent = inlineTextContent,
                        onTextLayout = segmentOnTextLayout,
                        style = style,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                    )
                }

                is RichTextSegment.InlineContentSegment -> {
                    standaloneInlineContent[it.key]?.let { content ->
                        content.content(content.modifier)
                    }
                }
            }
            SelectionFormatText(StringExt.LINE_SEPARATOR)
        }
    }
}

@Composable
private fun rememberRichTextSegment(
    text: AnnotatedString,
    standaloneInlineContentKeys: Set<String>,
): List<RichTextSegment> =
    remember(text, standaloneInlineContentKeys) {
        buildRichTextSegments(text, standaloneInlineContentKeys)
    }

@Composable
private fun rememberSegmentOnTextLayout(
    segmentIndex: Int,
    onTextLayout: ((Int, TextLayoutResult) -> Unit)?,
): (TextLayoutResult) -> Unit {
    val currentSegmentIndex = rememberUpdatedState(segmentIndex)
    val currentOnTextLayout = rememberUpdatedState(onTextLayout)
    return remember {
        { result -> currentOnTextLayout.value?.invoke(currentSegmentIndex.value, result) }
    }
}

private fun groupRichTextInlineContent(inlineContent: ImmutableMap<String, RichTextInlineContent>): GroupedRichTextInlineContent {
    val standalone = persistentMapOf<String, RichTextInlineContent.StandaloneInlineContent>().builder()
    val embedded = persistentMapOf<String, RichTextInlineContent.EmbeddedRichTextInlineContent>().builder()
    inlineContent.forEach { (key, value) ->
        when (value) {
            is RichTextInlineContent.StandaloneInlineContent -> standalone[key] = value
            is RichTextInlineContent.EmbeddedRichTextInlineContent -> embedded[key] = value
        }
    }
    return GroupedRichTextInlineContent(
        standalone = standalone.build(),
        embedded = embedded.build(),
    )
}

private fun buildRichTextSegments(
    text: AnnotatedString,
    standaloneInlineContentKeys: Set<String>,
): List<RichTextSegment> {
    if (text.isEmpty()) return emptyList()
    if (standaloneInlineContentKeys.isEmpty()) return listOf(RichTextSegment.Text(text))

    val standaloneInlineTextContentAnnotations = text.getStandaloneInlineTextContentAnnotations()
    if (standaloneInlineTextContentAnnotations.isEmpty()) return listOf(RichTextSegment.Text(text))

    val segments = mutableListOf<RichTextSegment>()
    var lastIndex = 0
    var hasStandaloneInlineContent = false
    standaloneInlineTextContentAnnotations.fastForEach { annotation ->
        if (annotation.item !in standaloneInlineContentKeys) return@fastForEach

        hasStandaloneInlineContent = true
        if (annotation.start > lastIndex) {
            segments.add(RichTextSegment.Text(text.subSequence(lastIndex, annotation.start)))
        }
        segments.add(RichTextSegment.InlineContentSegment(annotation.item))
        lastIndex = annotation.end
    }

    if (!hasStandaloneInlineContent) return listOf(RichTextSegment.Text(text))
    if (lastIndex < text.length) {
        segments.add(RichTextSegment.Text(text.subSequence(lastIndex, text.length)))
    }
    return segments
}

private sealed interface RichTextSegment {
    data class Text(
        val text: AnnotatedString,
    ) : RichTextSegment

    data class InlineContentSegment(
        val key: String,
    ) : RichTextSegment
}

private data class GroupedRichTextInlineContent(
    val standalone: ImmutableMap<String, RichTextInlineContent.StandaloneInlineContent>,
    val embedded: ImmutableMap<String, RichTextInlineContent.EmbeddedRichTextInlineContent>,
)
