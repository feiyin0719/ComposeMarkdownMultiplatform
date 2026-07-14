package com.iffly.compose.markdown.multiplatform.widget.richtext

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.SubcomposeMeasureScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
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
import com.iffly.compose.markdown.multiplatform.util.toPlaceholderSp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

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
    val (fixedSizeInlineContent, adaptiveInlineContent) =
        remember(inlineContent) { groupInlineContent(inlineContent) }

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
            inlineContent = fixedSizeInlineContent,
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
    adaptiveInlineContent: ImmutableList<Pair<String, RichTextInlineContent.EmbeddedRichTextInlineContent>> = persistentListOf(),
    fixedSizeInlineContent: ImmutableMap<String, InlineTextContent> = persistentMapOf(),
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current,
) {
    val density = LocalDensity.current
    val adaptiveInlineContentEntries = rememberAdaptiveInlineContentEntries(adaptiveInlineContent)
    SubcomposeLayout(modifier = modifier) { constraints ->
        val measuredAdaptiveInlineContent: ImmutableMap<String, InlineTextContent> =
            measureAdaptiveInlineContentSize(adaptiveInlineContentEntries, constraints, density)

        val combinedInlineContent =
            persistentMapOf<String, InlineTextContent>()
                .builder()
                .apply {
                    putAll(fixedSizeInlineContent)
                    putAll(measuredAdaptiveInlineContent)
                }.build()

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
                    inlineContent = combinedInlineContent,
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

@Composable
private fun rememberAdaptiveInlineContentEntries(
    adaptiveInlineContent: ImmutableList<Pair<String, RichTextInlineContent.EmbeddedRichTextInlineContent>>,
): ImmutableList<AdaptiveInlineContentEntry> {
    val entries = persistentListOf<AdaptiveInlineContentEntry>().builder()
    adaptiveInlineContent.fastForEach { (id, content) ->
        val entry =
            key(id) {
                val currentContent = rememberUpdatedState(content)
                remember { AdaptiveInlineContentEntry(id, currentContent) }
            }
        entries.add(entry)
    }
    return entries.build()
}

private fun SubcomposeMeasureScope.measureAdaptiveInlineContentSize(
    adaptiveInlineContent: ImmutableList<AdaptiveInlineContentEntry>,
    constraints: Constraints,
    density: Density,
): ImmutableMap<String, InlineTextContent> {
    val adaptiveInlineConstraints =
        constraints.copy(
            minWidth = 0,
            minHeight = 0,
        )
    val placeables =
        subcompose("adaptive_inline") {
            adaptiveInlineContent.fastForEach { entry ->
                key(entry.id) {
                    Box(modifier = Modifier.wrapContentSize()) {
                        entry.content.value.content(entry.id)
                    }
                }
            }
        }.map { it.measure(adaptiveInlineConstraints) }

    val measuredAdaptiveInlineContent = persistentMapOf<String, InlineTextContent>().builder()
    adaptiveInlineContent.forEachIndexed { index, entry ->
        val value = entry.content.value
        val placeable = placeables.getOrNull(index)
        val width = placeable?.width?.let(density::toPlaceholderSp) ?: value.placeholder.width
        val height = placeable?.height?.let(density::toPlaceholderSp) ?: value.placeholder.height
        val placeholder = value.placeholder.copy(width = width, height = height)
        measuredAdaptiveInlineContent[entry.id] =
            entry.inlineContent(
                placeholder = placeholder,
                child = value.content,
            )
    }
    return measuredAdaptiveInlineContent.build()
}

private class AdaptiveInlineContentEntry(
    val id: String,
    val content: State<RichTextInlineContent.EmbeddedRichTextInlineContent>,
) {
    private var cachedPlaceholder: Placeholder? = null
    private var cachedChild: (@Composable (String) -> Unit)? = null
    private var cachedInlineContent: InlineTextContent? = null

    fun inlineContent(
        placeholder: Placeholder,
        child: @Composable (String) -> Unit,
    ): InlineTextContent {
        val cached = cachedInlineContent
        if (cached != null && cachedPlaceholder == placeholder && cachedChild === child) return cached

        return InlineTextContent(
            placeholder = placeholder,
            children = { alternateText ->
                key(id) {
                    child(alternateText)
                }
            },
        ).also {
            cachedPlaceholder = placeholder
            cachedChild = child
            cachedInlineContent = it
        }
    }
}

private fun groupInlineContent(
    inlineContent: ImmutableMap<String, RichTextInlineContent.EmbeddedRichTextInlineContent>,
): GroupedInlineContent {
    val fixed = persistentMapOf<String, InlineTextContent>().builder()
    val adaptive = persistentListOf<Pair<String, RichTextInlineContent.EmbeddedRichTextInlineContent>>().builder()
    inlineContent.forEach { (id, value) ->
        if (value.adjustSizeByContent) {
            adaptive.add(id to value)
        } else {
            fixed[id] =
                InlineTextContent(
                    placeholder = value.placeholder,
                    children = { alternateText ->
                        key(id) {
                            value.content(alternateText)
                        }
                    },
                )
        }
    }
    return GroupedInlineContent(fixed = fixed.build(), adaptive = adaptive.build())
}

private data class GroupedInlineContent(
    val fixed: ImmutableMap<String, InlineTextContent>,
    val adaptive: ImmutableList<Pair<String, RichTextInlineContent.EmbeddedRichTextInlineContent>>,
)
