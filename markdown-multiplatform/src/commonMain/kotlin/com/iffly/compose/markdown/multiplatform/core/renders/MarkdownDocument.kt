package com.iffly.compose.markdown.multiplatform.core.renders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.iffly.compose.markdown.multiplatform.render.IBlockRenderer
import com.iffly.compose.markdown.multiplatform.render.MarkdownChildren
import org.intellij.markdown.ast.ASTNode

class DocumentRenderer : IBlockRenderer {
    @Composable
    override fun Invoke(
        node: ASTNode,
        sourceText: String,
        modifier: Modifier,
    ) {
        MarkdownChildren(
            parent = node,
            sourceText = sourceText,
            modifier = modifier.wrapContentSize(),
            verticalArrangement = Arrangement.Top,
            childModifierFactory = { Modifier },
        )
    }
}
