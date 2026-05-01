package com.iffly.compose.markdown.multiplatform.core.renders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.iffly.compose.markdown.multiplatform.config.currentActionHandler
import com.iffly.compose.markdown.multiplatform.config.currentTheme
import com.iffly.compose.markdown.multiplatform.render.IBlockRenderer
import com.iffly.compose.markdown.multiplatform.util.StringExt
import com.iffly.compose.markdown.multiplatform.widget.DisableSelectionWrapper
import com.iffly.compose.markdown.multiplatform.widget.LineNumberText
import com.iffly.compose.markdown.multiplatform.widget.SelectionFormatText
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.IndentedCodeBlock
import org.commonmark.node.Node

/**
 * Transforms raw code text into a styled [AnnotatedString] for display in a code block.
 *
 * Implement this interface to apply any kind of per-character or per-token styling —
 * syntax highlighting, diff coloring, error underlining, etc.
 *
 * The [annotate] method is called once per rendered code block. Returning a plain
 * [AnnotatedString] with no spans is equivalent to unstyled rendering.
 *
 * @see BasicSyntaxHighlighter for a ready-made implementation.
 */
fun interface CodeAnnotator {
    /**
     * Produces a styled [AnnotatedString] for the given code block.
     *
     * @param code The raw code text extracted from the block (may include a trailing newline).
     * @param language The language info string from the fenced code fence (e.g. `"kotlin"`,
     *   `"python"`). Empty string for indented code blocks or when no language was specified.
     * @param node The originating AST node, available for advanced use cases such as reading
     *   custom attributes or node metadata.
     * @return A styled [AnnotatedString]. Returning [AnnotatedString(code)][AnnotatedString]
     *   is valid and produces plain text.
     */
    fun annotate(
        code: String,
        language: String,
        node: Node,
    ): AnnotatedString
}

/**
 * Renders fenced and indented code blocks with an optional [CodeAnnotator] for styling.
 *
 * @param codeAnnotator Optional [CodeAnnotator] that transforms the raw code text into a
 *   styled [AnnotatedString] (e.g. syntax highlighting). Defaults to [BasicSyntaxHighlighter].
 *   Pass `null` to disable syntax highlighting (plain text).
 * @see BasicSyntaxHighlighter
 */
class CodeBlockRenderer(
    private val codeAnnotator: CodeAnnotator? = BasicSyntaxHighlighter(),
) : IBlockRenderer<Node> {
    @Composable
    override fun Invoke(
        node: Node,
        modifier: Modifier,
    ) {
        val codeBlockTheme = currentTheme().codeBlockTheme
        val language =
            when (node) {
                is FencedCodeBlock -> node.info?.trim() ?: ""
                else -> "Text"
            }
        val codeText =
            when (node) {
                is FencedCodeBlock -> node.literal?.trimEnd('\n') ?: ""
                is IndentedCodeBlock -> node.literal?.trimEnd('\n') ?: ""
                else -> return
            }

        Column(
            modifier =
                modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(codeBlockTheme.backgroundColor, shape = codeBlockTheme.shape)
                    .border(
                        width = codeBlockTheme.borderWidth,
                        color = codeBlockTheme.borderColor,
                        shape = codeBlockTheme.shape,
                    ).then(codeBlockTheme.blockModifier),
        ) {
            if (codeBlockTheme.showHeader) {
                CodeHeader(node = node, language = language)
            }
            CodeContent(codeText = codeText, language = language, node = node, codeAnnotator = codeAnnotator)
        }
    }
}

@Composable
private fun CodeHeader(
    node: Node,
    language: String,
) {
    val actionHandler = currentActionHandler()
    val codeBlockTheme = currentTheme().codeBlockTheme
    DisableSelectionWrapper(disabled = true) {
        Column {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .then(codeBlockTheme.headerModifier),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = language,
                    style = codeBlockTheme.codeTitleTextStyle,
                    modifier = Modifier.wrapContentSize(),
                )
                if (codeBlockTheme.showCopyButton) {
                    Text(
                        "Copy",
                        style = codeBlockTheme.codeCopyTextStyle,
                        modifier =
                            Modifier.wrapContentSize().clickable {
                                actionHandler?.handleCopyClick(node)
                            },
                    )
                }
            }
            HorizontalDivider(
                color = codeBlockTheme.borderColor,
                thickness = codeBlockTheme.borderWidth,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 9.dp),
            )
            SelectionFormatText(StringExt.LINE_SEPARATOR)
        }
    }
}

@Composable
private fun CodeContent(
    codeText: String,
    language: String,
    node: Node,
    codeAnnotator: CodeAnnotator?,
) {
    val contentTheme = currentTheme().codeBlockTheme.contentTheme
    val annotatedCode =
        (codeAnnotator ?: BasicSyntaxHighlighter(colors = currentTheme().codeBlockTheme.codeColors))
            .annotate(codeText, language, node)
    val scrollModifier =
        if (contentTheme.height != null) {
            val scrollState = rememberScrollState()
            Modifier
                .heightIn(max = contentTheme.height)
                .verticalScroll(scrollState)
        } else {
            Modifier
        }
    DisableSelectionWrapper(disabled = contentTheme.disableSelection) {
        LineNumberText(
            text = annotatedCode,
            textStyle = contentTheme.codeTextStyle,
            lineNumberStyle = contentTheme.lineNumberTextStyle,
            contentPadding = contentTheme.contentPadding,
            lineNumberPadding = contentTheme.lineNumberPadding,
            showLineNumber = contentTheme.showLineNumber,
            softWrap = contentTheme.softWrap,
            maxLines = contentTheme.maxLines,
            minLines = contentTheme.minLines,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .then(contentTheme.modifier)
                    .then(scrollModifier),
        )
        SelectionFormatText(StringExt.LINE_SEPARATOR)
    }
}
