package com.iffly.compose.markdown.multiplatform.core.renders

import androidx.compose.ui.text.AnnotatedString
import com.iffly.compose.markdown.multiplatform.ActionHandler
import com.iffly.compose.markdown.multiplatform.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.render.MarkdownInlineView
import com.iffly.compose.markdown.multiplatform.render.NodeStringBuilderContext
import com.iffly.compose.markdown.multiplatform.render.RenderRegistry
import com.iffly.compose.markdown.multiplatform.style.MarkdownTheme
import com.iffly.compose.markdown.multiplatform.util.StringExt
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getTextInNode

class HtmlInlineNodeStringBuilder : IInlineNodeStringBuilder {
    companion object {
        private val BR_REGEX = Regex("""<br\s*/?>""", RegexOption.IGNORE_CASE)
    }

    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: ASTNode,
        sourceText: String,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    ) {
        val text = node.getTextInNode(sourceText).toString().trim()
        if (BR_REGEX.matches(text)) {
            append(StringExt.LINE_SEPARATOR)
        }
    }
}
