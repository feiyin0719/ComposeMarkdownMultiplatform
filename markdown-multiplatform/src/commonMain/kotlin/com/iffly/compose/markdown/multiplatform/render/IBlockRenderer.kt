package com.iffly.compose.markdown.multiplatform.render

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.intellij.markdown.ast.ASTNode

/**
 * Interface for rendering block-level markdown elements as Compose UI.
 * Implementations handle specific block types such as headings, paragraphs, code blocks, lists, etc.
 *
 * @see RenderRegistry
 */
interface IBlockRenderer {
    /**
     * Renders the given AST node as a Compose UI block element.
     *
     * @param node The AST node representing the block element to render.
     * @param sourceText The raw markdown source text.
     * @param modifier Modifier to apply to the rendered block.
     */
    @Composable
    fun Invoke(
        node: ASTNode,
        sourceText: String,
        modifier: Modifier,
    )
}
