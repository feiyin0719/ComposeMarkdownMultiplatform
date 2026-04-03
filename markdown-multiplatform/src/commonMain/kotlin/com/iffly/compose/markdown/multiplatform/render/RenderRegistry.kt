package com.iffly.compose.markdown.multiplatform.render

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.commonmark.node.Node
import kotlin.reflect.KClass

data class RenderRegistry(
    private val blockRenderers: Map<KClass<out Node>, IBlockRenderer<*>>,
    private val inlineNodeStringBuilders: Map<KClass<out Node>, IInlineNodeStringBuilder<*>>,
    val markdownContentRenderer: MarkdownContentRenderer? = null,
    val markdownInlineTextRenderer: MarkdownInlineTextRenderer? = null,
) {
    fun getBlockRenderer(nodeClass: KClass<out Node>): IBlockRenderer<*>? = blockRenderers[nodeClass]

    fun getBlockRenderer(node: Node): IBlockRenderer<*>? = blockRenderers[node::class]

    fun getInlineNodeStringBuilder(nodeClass: KClass<out Node>): IInlineNodeStringBuilder<*>? = inlineNodeStringBuilders[nodeClass]

    fun getInlineNodeStringBuilder(node: Node): IInlineNodeStringBuilder<*>? = inlineNodeStringBuilders[node::class]

    @Composable
    fun invokeBlockRenderer(
        node: Node,
        modifier: Modifier = Modifier,
    ): Boolean {
        val renderer = getBlockRenderer(node) ?: return false
        @Suppress("UNCHECKED_CAST")
        (renderer as IBlockRenderer<Node>).Invoke(node, modifier)
        return true
    }
}
