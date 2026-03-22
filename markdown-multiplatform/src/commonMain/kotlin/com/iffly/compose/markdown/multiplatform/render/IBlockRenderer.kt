package com.iffly.compose.markdown.multiplatform.render

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.commonmark.node.Node

interface IBlockRenderer<T : Node> {
    @Composable
    fun Invoke(
        node: T,
        modifier: Modifier,
    )
}
