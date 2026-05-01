package com.iffly.compose.markdown.multiplatform.widget

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.toImmutableList

/**
 * A composable that displays text with an optional line number gutter alongside it.
 *
 * The gutter maps visual (wrapped) lines back to original source lines, and the text
 * content supports horizontal scrolling when soft wrapping is disabled.
 *
 * @param text The source text to display.
 * @param lineNumberStyle The text style applied to the line number gutter.
 * @param textStyle The text style applied to the main text content.
 * @param contentPadding Padding applied around the main text content.
 * @param lineNumberPadding Padding applied around the line number gutter.
 * @param showLineNumber Whether to show the line number gutter.
 * @param onTextLayout Optional callback invoked when the text layout is computed.
 * @see LineNumberGutter
 */
@Composable
fun LineNumberText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    lineNumberStyle: TextStyle =
        MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = Color.Gray,
        ),
    textStyle: TextStyle =
        MaterialTheme.typography.bodySmall.copy(
            fontSize = 12.sp,
            lineHeight = 16.sp,
        ),
    contentPadding: PaddingValues = PaddingValues(4.dp),
    lineNumberPadding: PaddingValues =
        PaddingValues(
            start = 4.dp,
            top = 4.dp,
            bottom = 4.dp,
            end = 16.dp,
        ),
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    showLineNumber: Boolean = true,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
) {
    var textLayoutResult: TextLayoutResult? by remember { mutableStateOf(null) }
    val originalLineStartOffset =
        remember(text) {
            text.text
                .withIndex()
                .filter { it.value == '\n' }
                .map { it.index + 1 }
                .toMutableList()
                .apply { add(0, 0) }
                .toList()
        }
    val visualLineStartOffset =
        remember(textLayoutResult) {
            textLayoutResult?.let { result ->
                val lineCount = result.lineCount
                List(lineCount) { lineIndex ->
                    result.getLineStart(lineIndex)
                }
            } ?: emptyList()
        }

    val scrollModifier =
        if (!softWrap) {
            val scrollState = rememberScrollState()
            Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
        } else {
            Modifier
        }

    Row(modifier = modifier) {
        if (showLineNumber) {
            LineNumberGutter(
                originalLineStartOffset = originalLineStartOffset.toImmutableList(),
                visualLineStartOffset = visualLineStartOffset.toImmutableList(),
                modifier = Modifier.wrapContentSize(),
                lineNumberStyle =
                    lineNumberStyle.copy(
                        lineHeight = textStyle.lineHeight,
                    ),
                paddingValues = lineNumberPadding,
            )
        }

        Text(
            text = text,
            modifier =
                Modifier
                    .weight(1f)
                    .padding(contentPadding)
                    .then(scrollModifier),
            style = textStyle,
            softWrap = softWrap,
            overflow = overflow,
            maxLines = maxLines,
            minLines = minLines,
            onTextLayout = {
                textLayoutResult = it
                onTextLayout?.invoke(it)
            },
        )
    }
}
