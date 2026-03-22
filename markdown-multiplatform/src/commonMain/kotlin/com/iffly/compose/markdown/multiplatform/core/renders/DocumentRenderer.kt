package com.iffly.compose.markdown.multiplatform.core.renders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.iffly.compose.markdown.multiplatform.render.IBlockRenderer
import com.iffly.compose.markdown.multiplatform.render.MarkdownChildren
import org.commonmark.node.Document

class DocumentRenderer : IBlockRenderer<Document> {
    @Composable
    override fun Invoke(
        node: Document,
        modifier: Modifier,
    ) {
        MarkdownChildren(
            parent = node,
            modifier = modifier.wrapContentSize(),
            verticalArrangement = Arrangement.Top,
            childModifierFactory = { Modifier },
        )
    }
}
