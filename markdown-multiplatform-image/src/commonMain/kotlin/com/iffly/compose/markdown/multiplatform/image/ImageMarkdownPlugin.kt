package com.iffly.compose.markdown.multiplatform.image

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.iffly.compose.markdown.multiplatform.config.IMarkdownRenderPlugin
import com.iffly.compose.markdown.multiplatform.render.IBlockRenderer
import com.iffly.compose.markdown.multiplatform.render.IInlineNodeStringBuilder
import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes

/**
 * Theme configuration for markdown image rendering, controlling alignment, scaling, shape, and error appearance.
 *
 * @param alignment How the image is aligned within its container.
 * @param contentScale How the image is scaled to fit its bounds.
 * @param shape Shape applied to clip the image.
 * @param modifier Additional modifier applied to the image.
 * @param errorPlaceholderColor Background color for the error placeholder view.
 */
@Immutable
data class ImageTheme(
    val alignment: Alignment = Alignment.Center,
    val contentScale: ContentScale = ContentScale.Inside,
    val shape: Shape = RoundedCornerShape(8.dp),
    val modifier: Modifier = Modifier,
    val errorPlaceholderColor: Color = Color(0xFFE0E0E0),
)

/**
 * Markdown render plugin that adds inline image support for [MarkdownElementTypes.IMAGE] nodes.
 *
 * Registers an [ImageNodeStringBuilder] as the inline node string builder for image elements.
 *
 * @param imageTheme Theme configuration controlling the image appearance.
 * @param loadingView Renderer displayed while the image is loading.
 * @param errorView Renderer displayed when the image fails to load; defaults to [ErrorImageWidgetRenderer].
 * @see IMarkdownRenderPlugin
 */
class ImageMarkdownPlugin(
    private val imageTheme: ImageTheme = ImageTheme(),
    private val loadingView: ImageWidgetRenderer = LoadingImageWidgetRenderer(),
    errorView: ImageWidgetRenderer? = null,
) : IMarkdownRenderPlugin {
    private val errorView: ImageWidgetRenderer =
        errorView ?: ErrorImageWidgetRenderer(imageTheme)

    override fun blockRenderers(): Map<IElementType, IBlockRenderer> = emptyMap()

    override fun inlineNodeStringBuilders(): Map<IElementType, IInlineNodeStringBuilder> =
        mapOf(
            MarkdownElementTypes.IMAGE to
                ImageNodeStringBuilder(imageTheme, loadingView, this.errorView),
        )
}
