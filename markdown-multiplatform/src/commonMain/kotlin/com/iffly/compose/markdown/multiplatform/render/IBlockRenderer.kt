package com.iffly.compose.markdown.multiplatform.render

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.intellij.markdown.ast.ASTNode

interface IBlockRenderer {
    @Composable
    fun Invoke(
        node: ASTNode,
        sourceText: String,
        modifier: Modifier,
    )
}
