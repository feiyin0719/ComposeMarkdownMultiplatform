package com.iffly.compose.markdown.multiplatform.core.renders

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.iffly.compose.markdown.multiplatform.render.IBlockRenderer
import org.intellij.markdown.ast.ASTNode

class EmptyRenderer : IBlockRenderer {
    @Composable
    override fun Invoke(
        node: ASTNode,
        sourceText: String,
        modifier: Modifier,
    ) {
        // Empty render for EOL - intentionally renders nothing
    }
}
