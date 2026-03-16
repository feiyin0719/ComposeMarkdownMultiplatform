package com.iffly.compose.markdown.multiplatform.render

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.iffly.compose.markdown.multiplatform.config.currentRenderRegistry
import com.iffly.compose.markdown.multiplatform.config.currentSourceText
import com.iffly.compose.markdown.multiplatform.config.currentTheme
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.CompositeASTNode

/** Functional interface for custom rendering of the top-level markdown content composable. */
fun interface MarkdownContentRenderer {
    @Composable
    operator fun invoke(
        node: ASTNode,
        sourceText: String,
        modifier: Modifier,
    )
}

/**
 * Renders a single AST node as Compose UI content.
 * Delegates to the custom [MarkdownContentRenderer] if one is registered, otherwise
 * falls back to [DefaultMarkdownContent].
 *
 * @param node The AST node to render.
 * @param modifier Modifier to apply to the rendered content.
 */
@Composable
fun MarkdownContent(
    node: ASTNode,
    modifier: Modifier = Modifier,
) {
    val renderRegistry = currentRenderRegistry()
    val sourceText = currentSourceText()
    renderRegistry.markdownContentRenderer?.invoke(
        node = node,
        sourceText = sourceText,
        modifier = modifier,
    ) ?: DefaultMarkdownContent(
        node = node,
        sourceText = sourceText,
        modifier = modifier,
    )
}

@Composable
private fun DefaultMarkdownContent(
    node: ASTNode,
    sourceText: String,
    modifier: Modifier = Modifier,
) {
    val renderRegistry = currentRenderRegistry()
    val renderer = renderRegistry.getBlockRenderer(node.type)
    if (renderer != null) {
        renderer.Invoke(node, sourceText, modifier)
    } else if (node is CompositeASTNode) {
        // Try as text only for element types
        MarkdownText(node, modifier = modifier)
    }
}

/**
 * Renders the child nodes of a parent AST node in a vertical [Column], with optional
 * spacers between block-level elements and lifecycle callbacks around each child.
 *
 * @param parent The parent AST node whose children will be rendered.
 * @param modifier Modifier to apply to the wrapping Column.
 * @param children The list of child nodes to render; defaults to [parent]'s children.
 * @param sourceText The raw markdown source text.
 * @param verticalArrangement The vertical arrangement strategy for the Column.
 * @param spacerHeight The height of spacers inserted between block-level children.
 * @param showSpacer Whether to show spacers between block-level children.
 * @param childModifierFactory Factory function producing a Modifier for each child node.
 * @param onBeforeChild Optional composable invoked before each child is rendered.
 * @param onAfterChild Optional composable invoked after each child is rendered.
 * @param onBeforeAll Optional composable invoked before all children are rendered.
 * @param onAfterAll Optional composable invoked after all children are rendered.
 */
@Composable
fun MarkdownChildren(
    parent: ASTNode,
    modifier: Modifier = Modifier,
    children: List<ASTNode> = parent.children,
    sourceText: String = currentSourceText(),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    spacerHeight: Dp = currentTheme().spacerTheme.spacerHeight,
    showSpacer: Boolean = currentTheme().spacerTheme.showSpacer,
    childModifierFactory: (child: ASTNode) -> Modifier = {
        Modifier
            .wrapContentHeight()
            .fillMaxWidth()
    },
    onBeforeChild: (@Composable (child: ASTNode, parent: ASTNode) -> Unit)? = null,
    onAfterChild: (@Composable (child: ASTNode, parent: ASTNode) -> Unit)? = null,
    onBeforeAll: (@Composable (parent: ASTNode) -> Unit)? = null,
    onAfterAll: (@Composable (parent: ASTNode) -> Unit)? = null,
) {
    val renderRegistry = currentRenderRegistry()
    Column(modifier = modifier, verticalArrangement = verticalArrangement) {
        onBeforeAll?.invoke(parent)
        children.forEachIndexed { index, child ->
            key(child) {
                onBeforeChild?.invoke(child, parent)
                MarkdownContent(
                    node = child,
                    modifier = childModifierFactory(child),
                )
                onAfterChild?.invoke(child, parent)
                if (index != children.lastIndex &&
                    showSpacer &&
                    renderRegistry.getBlockRenderer(child.type) != null
                ) {
                    Spacer(Modifier.height(spacerHeight))
                }
            }
        }
        onAfterAll?.invoke(parent)
    }
}
