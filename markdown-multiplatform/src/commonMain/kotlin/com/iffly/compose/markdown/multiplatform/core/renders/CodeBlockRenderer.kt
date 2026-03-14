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
import androidx.compose.ui.unit.dp
import com.iffly.compose.markdown.multiplatform.config.currentActionHandler
import com.iffly.compose.markdown.multiplatform.config.currentTheme
import com.iffly.compose.markdown.multiplatform.render.IBlockRenderer
import com.iffly.compose.markdown.multiplatform.util.StringExt
import com.iffly.compose.markdown.multiplatform.widget.DisableSelectionWrapper
import com.iffly.compose.markdown.multiplatform.widget.LineNumberText
import com.iffly.compose.markdown.multiplatform.widget.SelectionFormatText
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getTextInNode

class CodeBlockRenderer : IBlockRenderer {
    @Composable
    override fun Invoke(
        node: ASTNode,
        sourceText: String,
        modifier: Modifier,
    ) {
        val codeBlockTheme = currentTheme().codeBlockTheme
        val language =
            when (node.type) {
                MarkdownElementTypes.CODE_FENCE -> node.getCodeFenceLanguage(sourceText)
                else -> "Text"
            }
        val codeText =
            when (node.type) {
                MarkdownElementTypes.CODE_FENCE -> node.getCodeFenceContent(sourceText)
                MarkdownElementTypes.CODE_BLOCK -> node.getIndentedCodeContent(sourceText)
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
            CodeContent(codeText = codeText)
        }
    }
}

@Composable
private fun CodeHeader(
    node: ASTNode,
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
private fun CodeContent(codeText: String) {
    val contentTheme = currentTheme().codeBlockTheme.contentTheme
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
            text = codeText,
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

private fun ASTNode.getCodeFenceLanguage(sourceText: String): String {
    val langNode = children.firstOrNull { it.type == MarkdownTokenTypes.FENCE_LANG }
    return langNode?.getTextInNode(sourceText)?.toString()?.trim() ?: ""
}

private fun ASTNode.getCodeFenceContent(sourceText: String): String {
    val contentLines =
        children
            .filter { it.type == MarkdownTokenTypes.CODE_FENCE_CONTENT || it.type == MarkdownTokenTypes.EOL }
            .dropWhile { it.type == MarkdownTokenTypes.EOL }
            .dropLastWhile { it.type == MarkdownTokenTypes.EOL }
    if (contentLines.isEmpty()) return ""
    return contentLines.joinToString("") { it.getTextInNode(sourceText).toString() }
}

private fun ASTNode.getIndentedCodeContent(sourceText: String): String {
    val codeLines = children.filter { it.type == MarkdownTokenTypes.CODE_LINE }
    return codeLines.joinToString("") { it.getTextInNode(sourceText).toString() }
}
