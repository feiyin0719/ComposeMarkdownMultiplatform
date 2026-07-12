package com.iffly.compose.markdown.multiplatform.widget

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
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
    contentPadding: PaddingValues = remember { PaddingValues(4.dp) },
    lineNumberPadding: PaddingValues =
        remember {
            PaddingValues(
                start = 4.dp,
                top = 4.dp,
                bottom = 4.dp,
                end = 16.dp,
            )
        },
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    showLineNumber: Boolean = true,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
) {
    if (showLineNumber) {
        NumberedText(
            text = text,
            modifier = modifier,
            lineNumberStyle = lineNumberStyle,
            textStyle = textStyle,
            contentPadding = contentPadding,
            lineNumberPadding = lineNumberPadding,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            minLines = minLines,
            onTextLayout = onTextLayout,
        )
    } else {
        PlainText(
            text = text,
            modifier = modifier,
            textStyle = textStyle,
            contentPadding = contentPadding,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            minLines = minLines,
            onTextLayout = onTextLayout,
        )
    }
}

@Composable
private fun NumberedText(
    text: AnnotatedString,
    lineNumberStyle: TextStyle,
    textStyle: TextStyle,
    contentPadding: PaddingValues,
    lineNumberPadding: PaddingValues,
    overflow: TextOverflow,
    softWrap: Boolean,
    maxLines: Int,
    minLines: Int,
    modifier: Modifier = Modifier,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
) {
    var visualLineStartOffset by remember(text.text) {
        mutableStateOf<ImmutableList<Int>>(persistentListOf())
    }
    val originalLineStartOffset =
        remember(text.text) {
            buildList {
                add(0)
                text.text.forEachIndexed { index, character ->
                    if (character == '\n') add(index + 1)
                }
            }.toImmutableList()
        }
    val resolvedLineNumberStyle =
        remember(lineNumberStyle, textStyle.lineHeight) {
            lineNumberStyle.copy(lineHeight = textStyle.lineHeight)
        }

    Row(modifier = modifier) {
        LineNumberGutter(
            originalLineStartOffset = originalLineStartOffset,
            visualLineStartOffset = visualLineStartOffset,
            modifier = Modifier.wrapContentSize(),
            lineNumberStyle = resolvedLineNumberStyle,
            paddingValues = lineNumberPadding,
        )
        CodeText(
            text = text,
            textStyle = textStyle,
            contentPadding = contentPadding,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            minLines = minLines,
            onTextLayout = { result ->
                val newVisualLineStartOffset =
                    List(result.lineCount) { lineIndex -> result.getLineStart(lineIndex) }
                        .toImmutableList()
                if (visualLineStartOffset != newVisualLineStartOffset) {
                    visualLineStartOffset = newVisualLineStartOffset
                }
                onTextLayout?.invoke(result)
            },
        )
    }
}

@Composable
private fun PlainText(
    text: AnnotatedString,
    textStyle: TextStyle,
    contentPadding: PaddingValues,
    overflow: TextOverflow,
    softWrap: Boolean,
    maxLines: Int,
    minLines: Int,
    modifier: Modifier = Modifier,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
) {
    Row(modifier = modifier) {
        CodeText(
            text = text,
            textStyle = textStyle,
            contentPadding = contentPadding,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            minLines = minLines,
            onTextLayout = onTextLayout ?: emptyOnTextLayout,
        )
    }
}

@Composable
private fun RowScope.CodeText(
    text: AnnotatedString,
    textStyle: TextStyle,
    contentPadding: PaddingValues,
    overflow: TextOverflow,
    softWrap: Boolean,
    maxLines: Int,
    minLines: Int,
    onTextLayout: (TextLayoutResult) -> Unit = emptyOnTextLayout,
) {
    val scrollModifier =
        if (!softWrap) {
            val scrollState = rememberScrollState()
            Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
        } else {
            Modifier
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
        onTextLayout = onTextLayout,
    )
}

private val emptyOnTextLayout: (TextLayoutResult) -> Unit = {}
