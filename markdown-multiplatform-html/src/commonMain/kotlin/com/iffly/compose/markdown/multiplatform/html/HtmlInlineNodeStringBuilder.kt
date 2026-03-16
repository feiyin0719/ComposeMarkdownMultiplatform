package com.iffly.compose.markdown.multiplatform.html

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

/**
 * Inline node string builder for HTML tags within markdown content.
 *
 * Parses HTML tag tokens, handles `<br>` tags as line breaks, and delegates
 * open/close tag processing to registered [HtmlInlineTagHandler] instances.
 *
 * @param tagHandlers Map of lowercase tag names to their corresponding handlers.
 * @see IInlineNodeStringBuilder
 * @see HtmlInlineTagHandler
 */
class HtmlInlineNodeStringBuilder(
    private val tagHandlers: Map<String, HtmlInlineTagHandler>,
) : IInlineNodeStringBuilder {
    companion object {
        private val BR_REGEX = Regex("""<br\s*/?>""", RegexOption.IGNORE_CASE)
        private val TAG_NAME_REGEX = Regex("""</?(\w+)""")
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
            return
        }

        val tagName =
            TAG_NAME_REGEX.find(text)
                ?.groupValues?.get(1)?.lowercase() ?: return

        val handler = tagHandlers[tagName] ?: return

        val context =
            HtmlInlineTagContext(
                node = node,
                inlineContentMap = inlineContentMap,
                markdownTheme = markdownTheme,
                actionHandler = actionHandler,
                indentLevel = indentLevel,
                isShowNotSupported = isShowNotSupported,
                renderRegistry = renderRegistry,
                nodeStringBuilderContext = nodeStringBuilderContext,
            )

        if (text.startsWith("</")) {
            handler.onCloseTag(tagName, this, context)
        } else {
            handler.onOpenTag(tagName, text, this, context)
        }
    }
}
