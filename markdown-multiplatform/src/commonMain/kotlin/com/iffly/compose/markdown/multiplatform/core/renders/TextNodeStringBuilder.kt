package com.iffly.compose.markdown.multiplatform.core.renders

import androidx.compose.ui.text.AnnotatedString
import com.iffly.compose.markdown.multiplatform.ActionHandler
import com.iffly.compose.markdown.multiplatform.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.render.MarkdownInlineView
import com.iffly.compose.markdown.multiplatform.render.NodeStringBuilderContext
import com.iffly.compose.markdown.multiplatform.render.RenderRegistry
import com.iffly.compose.markdown.multiplatform.style.MarkdownTheme
import com.iffly.compose.markdown.multiplatform.util.EntityConverter
import org.commonmark.node.Text

/**
 * Inline node string builder for Text nodes.
 * Appends the node's literal text after processing escape sequences via [EntityConverter].
 */
class TextNodeStringBuilder : IInlineNodeStringBuilder<Text> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: Text,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    ) {
        append(
            EntityConverter.replaceEntities(
                node.literal,
                processEntities = false,
                processEscapes = true,
            ),
        )
    }
}
