package com.iffly.compose.markdown.multiplatform.core.renders

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.iffly.compose.markdown.multiplatform.render.IBlockRenderer
import com.iffly.compose.markdown.multiplatform.render.MarkdownText
import org.intellij.markdown.ast.ASTNode

/**
 * Block renderer for text-based markdown elements such as paragraphs and headings.
 *
 * Delegates rendering to [MarkdownText], which builds an [AnnotatedString] from
 * the node's inline children and displays it as styled text.
 *
 * @see IBlockRenderer
 * @see MarkdownText
 */
class TextBlockRenderer : IBlockRenderer {
    @Composable
    override fun Invoke(
        node: ASTNode,
        sourceText: String,
        modifier: Modifier,
    ) {
        MarkdownText(parent = node, modifier = modifier)
    }
}
