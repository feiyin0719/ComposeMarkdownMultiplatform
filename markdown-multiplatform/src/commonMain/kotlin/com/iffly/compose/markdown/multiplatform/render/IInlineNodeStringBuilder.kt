package com.iffly.compose.markdown.multiplatform.render

import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.text.AnnotatedString
import com.iffly.compose.markdown.multiplatform.ActionHandler
import com.iffly.compose.markdown.multiplatform.style.MarkdownTheme
import com.iffly.compose.markdown.multiplatform.widget.richtext.RichTextInlineContent
import com.iffly.compose.markdown.multiplatform.widget.richtext.appendStandaloneInlineTextContent
import org.commonmark.node.Node

interface IInlineNodeStringBuilder<T : Node> {
    fun AnnotatedString.Builder.buildInlineNodeString(
        node: T,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    )
}

/**
 * Registers [inlineContent] and appends its matching annotation in one operation.
 *
 * When [overwrite] is `false`, an existing [id] is preserved and a deterministic occurrence
 * suffix is added to the new entry. A node's class name is therefore sufficient as the base ID;
 * callers do not need to include node content or source positions. When [overwrite] is `true`,
 * the existing entry is replaced;
 * all annotations that reference [id], including ones appended earlier, then resolve to the new
 * content. Existing and replacement content must use the same embedded or standalone annotation
 * type. Use overwrite only when those occurrences are semantically interchangeable.
 *
 * Prefer this helper over writing to [inlineContentMap] and calling Compose's native
 * `appendInlineContent` directly, because the native API does not manage map key collisions.
 *
 * @param id Base ID for the inline content, normally the node class name.
 * @param inlineContent Embedded or standalone rich text content to register.
 * @param inlineContentMap Map supplied to [IInlineNodeStringBuilder.buildInlineNodeString].
 * @param alternateText Text used for accessibility and when the inline content cannot be resolved.
 * @param overwrite Whether to replace an existing entry with [id]. Defaults to `false`.
 * @return The actual ID registered and appended to this annotated string.
 */
fun AnnotatedString.Builder.appendMarkdownInlineContent(
    id: String,
    inlineContent: RichTextInlineContent,
    inlineContentMap: MutableMap<String, MarkdownInlineView>,
    alternateText: String = INLINE_CONTENT_REPLACEMENT_CHAR,
    overwrite: Boolean = false,
): String {
    require(alternateText.isNotEmpty())
    inlineContentMap.requireCompatibleOverwrite(id, inlineContent, overwrite)
    val actualId = inlineContentMap.resolveInlineContentId(id, overwrite)
    inlineContentMap[actualId] =
        MarkdownInlineView.MarkdownRichTextInlineContent(inlineContent)
    when (inlineContent) {
        is RichTextInlineContent.EmbeddedRichTextInlineContent -> {
            appendInlineContent(actualId, alternateText)
        }

        is RichTextInlineContent.StandaloneInlineContent -> {
            appendStandaloneInlineTextContent(actualId, alternateText)
        }
    }
    return actualId
}

private fun Map<String, MarkdownInlineView>.requireCompatibleOverwrite(
    id: String,
    inlineContent: RichTextInlineContent,
    overwrite: Boolean,
) {
    if (!overwrite) return
    val existing =
        (this[id] as? MarkdownInlineView.MarkdownRichTextInlineContent)?.inlineContent
            ?: return
    require(existing.hasSameAnnotationTypeAs(inlineContent)) {
        "Cannot overwrite inline content '$id' with a different annotation type"
    }
}

private fun RichTextInlineContent.hasSameAnnotationTypeAs(other: RichTextInlineContent): Boolean =
    when (this) {
        is RichTextInlineContent.EmbeddedRichTextInlineContent -> {
            other is RichTextInlineContent.EmbeddedRichTextInlineContent
        }

        is RichTextInlineContent.StandaloneInlineContent -> {
            other is RichTextInlineContent.StandaloneInlineContent
        }
    }

private fun Map<String, MarkdownInlineView>.resolveInlineContentId(
    id: String,
    overwrite: Boolean,
): String {
    if (overwrite || id !in this) return id

    var occurrence = 1
    var candidate = "${id}_$occurrence"
    while (candidate in this) {
        occurrence++
        candidate = "${id}_$occurrence"
    }
    return candidate
}

private const val INLINE_CONTENT_REPLACEMENT_CHAR = "\uFFFD"

fun IInlineNodeStringBuilder<*>.buildMarkdownInlineNodeString(
    node: Node,
    inlineContentMap: MutableMap<String, MarkdownInlineView>,
    markdownTheme: MarkdownTheme,
    indentLevel: Int,
    actionHandler: ActionHandler? = null,
    renderRegistry: RenderRegistry,
    isShowNotSupported: Boolean,
    builder: AnnotatedString.Builder,
    nodeStringBuilderContext: NodeStringBuilderContext,
) {
    @Suppress("UNCHECKED_CAST")
    val typedBuilder = this as IInlineNodeStringBuilder<Node>
    with(typedBuilder) {
        builder.buildInlineNodeString(
            node,
            inlineContentMap,
            markdownTheme,
            actionHandler,
            indentLevel,
            isShowNotSupported,
            renderRegistry,
            nodeStringBuilderContext,
        )
    }
}
