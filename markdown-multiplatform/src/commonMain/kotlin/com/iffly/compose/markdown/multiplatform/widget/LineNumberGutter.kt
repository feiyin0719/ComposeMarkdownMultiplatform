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

/**
 * A composable that renders a vertical gutter of line numbers corresponding to the text content.
 *
 * It maps visual (potentially wrapped) line offsets back to original source line numbers,
 * displaying the original line number for the first visual line of each source line and
 * a continuation placeholder for wrapped continuation lines. The gutter text is wrapped
 * in [DisableSelection] so it cannot be selected by the user.
 *
 * @param originalLineStartOffset Character offsets where each original source line begins.
 * @param visualLineStartOffset Character offsets where each visual (rendered) line begins.
 * @param lineNumberStyle The text style applied to line numbers.
 * @param continuationPlaceholder Text shown for wrapped continuation lines (defaults to a space).
 * @param paddingValues Padding applied around the gutter.
 * @param calculateGutterLineNumber Strategy for mapping visual lines to original line numbers.
 * @see CalculateGutterLineNumber
 */
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

/**
 * A functional interface that maps visual (rendered) line offsets to original source line indices.
 *
 * Implementations receive the character offsets where original and visual lines begin
 * and return a list mapping each visual line to its original line index (or [EMPTY_LINE_INDEX]
 * for wrapped continuation lines).
 *
 * @see LineNumberGutter
 */
fun interface CalculateGutterLineNumber {
    companion object {
        /** Sentinel value indicating that a visual line is a continuation of the previous original line. */
        const val EMPTY_LINE_INDEX = Int.MIN_VALUE

        /** Default implementation that sequentially maps visual lines to original source lines. */
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

    /**
     * Computes the mapping from visual lines to original source line indices.
     *
     * @param originalLineStartOffset Character offsets where each original source line begins.
     * @param visualLineStartOffset Character offsets where each visual (rendered) line begins.
     * @return A list where each element is the original line index for the corresponding visual line,
     *         or [EMPTY_LINE_INDEX] for continuation lines.
     */
    operator fun invoke(
        originalLineStartOffset: List<Int>,
        visualLineStartOffset: List<Int>,
    ): List<Int>
}
