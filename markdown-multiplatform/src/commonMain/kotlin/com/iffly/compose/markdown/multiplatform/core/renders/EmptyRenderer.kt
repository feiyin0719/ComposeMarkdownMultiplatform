package com.iffly.compose.markdown.multiplatform.core.renders

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.iffly.compose.markdown.multiplatform.render.IBlockRenderer
import org.commonmark.node.Node

class EmptyRenderer : IBlockRenderer<Node> {
    @Composable
    override fun Invoke(
        node: Node,
        modifier: Modifier,
    ) {
    }
}
