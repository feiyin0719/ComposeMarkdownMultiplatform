package com.iffly.compose.markdown.multiplatform.task

import com.iffly.compose.markdown.multiplatform.config.IMarkdownRenderPlugin
import com.iffly.compose.markdown.multiplatform.core.renders.EmptyNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.render.IBlockRenderer
import com.iffly.compose.markdown.multiplatform.render.IInlineNodeStringBuilder
import org.commonmark.Extension
import org.commonmark.ext.task.list.items.TaskListItemMarker
import org.commonmark.ext.task.list.items.TaskListItemsExtension
import org.commonmark.node.ListItem
import org.commonmark.node.Node
import kotlin.reflect.KClass

/**
 * Markdown render plugin that adds task list item support.
 *
 * Recognizes `- [x]` (checked) and `- [ ]` (unchecked) syntax in list items
 * and renders them with checkbox markers. Uses the commonmark-kotlin
 * task-list-items extension for parsing.
 *
 * @see TaskListItemsExtension
 * @see IMarkdownRenderPlugin
 */
class TaskMarkdownPlugin : IMarkdownRenderPlugin {
    override fun parserExtensions(): List<Extension> = listOf(TaskListItemsExtension.create())

    override fun blockRenderers(): Map<KClass<out Node>, IBlockRenderer<*>> = mapOf(ListItem::class to TaskListItemRenderer())

    override fun inlineNodeStringBuilders(): Map<KClass<out Node>, IInlineNodeStringBuilder<*>> =
        mapOf(TaskListItemMarker::class to EmptyNodeStringBuilder())
}
