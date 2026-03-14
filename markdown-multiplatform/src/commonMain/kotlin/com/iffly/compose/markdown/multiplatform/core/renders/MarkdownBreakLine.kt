package com.iffly.compose.markdown.multiplatform.core.renders

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.iffly.compose.markdown.multiplatform.config.currentTheme
import com.iffly.compose.markdown.multiplatform.render.IBlockRenderer
import org.intellij.markdown.ast.ASTNode

class BreakLineRenderer : IBlockRenderer {
    @Composable
    override fun Invoke(
        node: ASTNode,
        sourceText: String,
        modifier: Modifier,
    ) {
        val theme = currentTheme()
        HorizontalDivider(
            color = theme.breakLineColor,
            thickness = theme.breakLineHeight,
            modifier =
                modifier
                    .height(theme.breakLineHeight)
                    .fillMaxWidth(),
        )
    }
}
