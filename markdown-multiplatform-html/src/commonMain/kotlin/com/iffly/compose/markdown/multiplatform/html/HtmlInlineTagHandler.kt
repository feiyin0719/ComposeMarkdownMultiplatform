package com.iffly.compose.markdown.multiplatform.html

import androidx.compose.ui.text.AnnotatedString
import com.iffly.compose.markdown.multiplatform.ActionHandler
import com.iffly.compose.markdown.multiplatform.render.MarkdownInlineView
import com.iffly.compose.markdown.multiplatform.render.NodeStringBuilderContext
import com.iffly.compose.markdown.multiplatform.render.RenderRegistry
import com.iffly.compose.markdown.multiplatform.style.MarkdownTheme
import org.intellij.markdown.ast.ASTNode

data class HtmlInlineTagContext(
    val node: ASTNode,
    val inlineContentMap: MutableMap<String, MarkdownInlineView>,
    val markdownTheme: MarkdownTheme,
    val actionHandler: ActionHandler?,
    val indentLevel: Int,
    val isShowNotSupported: Boolean,
    val renderRegistry: RenderRegistry,
    val nodeStringBuilderContext: NodeStringBuilderContext,
)

interface HtmlInlineTagHandler {
    val tagNames: Set<String>

    fun onOpenTag(
        tagName: String,
        rawTag: String,
        builder: AnnotatedString.Builder,
        context: HtmlInlineTagContext,
    )

    fun onCloseTag(
        tagName: String,
        builder: AnnotatedString.Builder,
        context: HtmlInlineTagContext,
    ) {
        builder.pop()
    }
}
