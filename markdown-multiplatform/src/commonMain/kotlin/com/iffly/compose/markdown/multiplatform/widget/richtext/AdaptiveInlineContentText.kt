package com.iffly.compose.markdown.multiplatform.widget.richtext

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.SubcomposeMeasureScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMapIndexed
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap

/**
 * A composable that renders annotated text with support for adaptive inline content whose
 * placeholder size is determined by measuring the actual content.
 *
 * If all inline content has fixed sizes, the text is rendered directly via [AutoLineHeightText].
 * Otherwise, a [SubcomposeLayout] is used to measure adaptive content before final text layout.
 *
 * @param text The annotated string to display.
 * @param inlineContent A map of embedded inline content entries keyed by annotation ID.
 * @param onTextLayout Callback invoked when the text layout is computed.
 * @param style The default text style to apply.
 * @see AutoLineHeightText
 * @see RichTextInlineContent.EmbeddedRichTextInlineContent
 */
@Composable
fun AdaptiveInlineContentText(
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
    inlineContent: ImmutableMap<String, RichTextInlineContent.EmbeddedRichTextInlineContent> = persistentMapOf(),
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current,
) {
    val (fixedSizeInlineContent, adaptiveInlineContent) = groupInlineContent(inlineContent)

    if (adaptiveInlineContent.isEmpty()) {
        AutoLineHeightText(
            text = text,
            modifier = modifier,
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
            inlineContent = fixedSizeInlineContent.toImmutableMap(),
            onTextLayout = onTextLayout,
            style = style,
        )
    } else {
        TextWithAdaptiveInlineContent(
            text = text,
            modifier = modifier,
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
            adaptiveInlineContent = adaptiveInlineContent,
            fixedSizeInlineContent = fixedSizeInlineContent,
            onTextLayout = onTextLayout,
            style = style,
        )
    }
}

@Composable
private fun TextWithAdaptiveInlineContent(
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
    adaptiveInlineContent: Map<String, RichTextInlineContent.EmbeddedRichTextInlineContent> = persistentMapOf(),
    fixedSizeInlineContent: Map<String, InlineTextContent> = persistentMapOf(),
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current,
) {
    val density = LocalDensity.current
    SubcomposeLayout(modifier = modifier) { constraints ->
        val measuredAdaptiveInlineContent: Map<String, InlineTextContent> =
            measureAdaptiveInlineContentSize(adaptiveInlineContent, constraints, density)

        val combinedInlineContent = fixedSizeInlineContent + measuredAdaptiveInlineContent

        val textPlaceables =
            subcompose("text") {
                AutoLineHeightText(
                    text = text,
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
                    inlineContent = combinedInlineContent.toImmutableMap(),
                    onTextLayout = onTextLayout,
                    style = style,
                )
            }.map { it.measure(constraints) }

        val textPlaceable = textPlaceables.singleOrNull()

        val width = textPlaceable?.width ?: constraints.minWidth
        val height = textPlaceable?.height ?: constraints.minHeight

        layout(width, height) {
            textPlaceable?.place(0, 0)
        }
    }
}

private fun SubcomposeMeasureScope.measureAdaptiveInlineContentSize(
    adaptiveInlineContent: Map<String, RichTextInlineContent.EmbeddedRichTextInlineContent>,
    constraints: Constraints,
    density: Density,
): Map<String, InlineTextContent> {
    val adaptiveKeys = adaptiveInlineContent.keys.toList()
    val adaptiveInlineConstraints =
        constraints.copy(
            minWidth = 0,
            minHeight = 0,
        )
    val placeables =
        subcompose("adaptive_inline") {
            adaptiveKeys.fastForEach { key ->
                val value = adaptiveInlineContent.getValue(key)
                Box(modifier = Modifier.wrapContentSize()) {
                    value.content(key)
                }
            }
        }.map { it.measure(adaptiveInlineConstraints) }

    val measuredAdaptiveInlineContent: Map<String, InlineTextContent> =
        adaptiveKeys
            .fastMapIndexed { index, key ->
                val value = adaptiveInlineContent.getValue(key)
                val placeable = placeables.getOrNull(index)
                val width =
                    placeable?.width?.let { with(density) { it.toSp() } }
                        ?: value.placeholder.width
                val height =
                    placeable?.height?.let { with(density) { it.toSp() } }
                        ?: value.placeholder.height

                key to
                    InlineTextContent(
                        placeholder =
                            value.placeholder.copy(
                                width = width,
                                height = height,
                            ),
                        children = value.content,
                    )
            }.toMap()
    return measuredAdaptiveInlineContent
}

private fun groupInlineContent(
    inlineContent: Map<String, RichTextInlineContent.EmbeddedRichTextInlineContent>,
): Pair<Map<String, InlineTextContent>, Map<String, RichTextInlineContent.EmbeddedRichTextInlineContent>> {
    val fixedSizeInlineContent =
        inlineContent
            .mapNotNull { (key, value) ->
                if (!value.adjustSizeByContent) {
                    key to
                        InlineTextContent(
                            placeholder = value.placeholder,
                            children = value.content,
                        )
                } else {
                    null
                }
            }.toMap()

    val adaptiveInlineContent =
        inlineContent
            .mapNotNull { (key, value) ->
                if (value.adjustSizeByContent) {
                    key to value
                } else {
                    null
                }
            }.toMap()
    return Pair(fixedSizeInlineContent, adaptiveInlineContent)
}
