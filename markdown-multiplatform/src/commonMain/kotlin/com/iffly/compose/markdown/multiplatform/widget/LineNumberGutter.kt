package com.iffly.compose.markdown.multiplatform.widget

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList

@Composable
fun LineNumberGutter(
    originalLineStartOffset: ImmutableList<Int>,
    visualLineStartOffset: ImmutableList<Int>,
    modifier: Modifier = Modifier,
    lineNumberStyle: TextStyle =
        MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
        ),
    continuationPlaceholder: String = " ",
    paddingValues: PaddingValues = PaddingValues(horizontal = 4.dp),
    calculateGutterLineNumber: CalculateGutterLineNumber = CalculateGutterLineNumber.DefaultCalculateGutterLineNumber,
) {
    val visualToOriginalLine =
        remember(originalLineStartOffset, visualLineStartOffset) {
            calculateGutterLineNumber(
                originalLineStartOffset = originalLineStartOffset,
                visualLineStartOffset = visualLineStartOffset,
            )
        }

    val gutterText =
        remember(visualToOriginalLine, continuationPlaceholder) {
            if (visualToOriginalLine.isEmpty()) {
                ""
            } else {
                buildString {
                    visualToOriginalLine.forEachIndexed { index, orig ->
                        if (orig != CalculateGutterLineNumber.EMPTY_LINE_INDEX) {
                            append(orig + 1)
                        } else {
                            append(continuationPlaceholder)
                        }
                        if (index < visualToOriginalLine.lastIndex) append('\n')
                    }
                }
            }
        }
    DisableSelection {
        Text(
            text = gutterText,
            modifier =
                modifier
                    .padding(paddingValues = paddingValues),
            style = lineNumberStyle,
        )
    }
}

fun interface CalculateGutterLineNumber {
    companion object {
        const val EMPTY_LINE_INDEX = Int.MIN_VALUE

        val DefaultCalculateGutterLineNumber =
            CalculateGutterLineNumber { originalLineStartOffset, visualLineStartOffset ->
                val visualToOriginalLine = mutableListOf<Int>()
                var originalLineIndex = 0
                var lastOriginalLineEnd = -1

                visualLineStartOffset.forEach { currentVisualLineStartOffset ->
                    if (currentVisualLineStartOffset > lastOriginalLineEnd) {
                        visualToOriginalLine.add(originalLineIndex)
                        originalLineIndex++
                        lastOriginalLineEnd =
                            originalLineStartOffset.getOrElse(originalLineIndex) { Int.MAX_VALUE } - 1
                    } else {
                        visualToOriginalLine.add(EMPTY_LINE_INDEX)
                    }
                }

                visualToOriginalLine
            }
    }

    operator fun invoke(
        originalLineStartOffset: List<Int>,
        visualLineStartOffset: List<Int>,
    ): List<Int>
}
