package com.iffly.compose.markdown.multiplatform.render

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.iffly.compose.markdown.multiplatform.config.LocalSourceTextProvider
import com.iffly.compose.markdown.multiplatform.config.currentRenderRegistry
import com.iffly.compose.markdown.multiplatform.config.currentSourceText
import com.iffly.compose.markdown.multiplatform.config.currentTheme
import com.iffly.compose.markdown.multiplatform.config.isShowNotSupported
import com.iffly.compose.markdown.multiplatform.util.contentText
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode

fun interface MarkdownContentRenderer {
    @Composable
    operator fun invoke(
        node: ASTNode,
        sourceText: String,
        modifier: Modifier,
    )
}

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
    } else {
        // Try as text
        MarkdownText(node, modifier = modifier)
    }
}

@Composable
fun MarkdownChildren(
    parent: ASTNode,
    children: List<ASTNode> = parent.children,
    sourceText: String = currentSourceText(),
    modifier: Modifier = Modifier,
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
                if (index != children.lastIndex && showSpacer && child.type != MarkdownTokenTypes.EOL) {
                    Spacer(Modifier.height(spacerHeight))
                }
            }
        }
        onAfterAll?.invoke(parent)
    }
}
