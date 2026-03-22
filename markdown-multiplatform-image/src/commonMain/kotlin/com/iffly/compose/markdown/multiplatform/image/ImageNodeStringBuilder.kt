package com.iffly.compose.markdown.multiplatform.image

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.iffly.compose.markdown.multiplatform.ActionHandler
import com.iffly.compose.markdown.multiplatform.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.render.MarkdownInlineView
import com.iffly.compose.markdown.multiplatform.render.NodeStringBuilderContext
import com.iffly.compose.markdown.multiplatform.render.RenderRegistry
import com.iffly.compose.markdown.multiplatform.style.MarkdownTheme
import com.iffly.compose.markdown.multiplatform.widget.LoadingView
import com.iffly.compose.markdown.multiplatform.widget.richtext.RichTextInlineContent
import com.iffly.compose.markdown.multiplatform.widget.richtext.appendStandaloneInlineTextContent
import org.commonmark.node.Image
import org.commonmark.node.Node

/**
 * Returns the text content of this node by concatenating the [literal][org.commonmark.node.Text.literal]
 * of all direct [Text][org.commonmark.node.Text] children.
 */
fun Node.textContent(): String {
    val sb = StringBuilder()
    var child = firstChild
    while (child != null) {
        if (child is org.commonmark.node.Text) {
            sb.append(child.literal)
        }
        child = child.next
    }
    return sb.toString()
}

/**
 * Functional interface for rendering an image widget as a Composable, used for loading
 * and error states.
 *
 * @see LoadingImageWidgetRenderer
 * @see ErrorImageWidgetRenderer
 */
fun interface ImageWidgetRenderer {
    @Suppress("ComposableNaming")
    @Composable
    operator fun invoke(
        url: String,
        contentDescription: String?,
        node: Node,
        modifier: Modifier,
    )
}

/**
 * Default loading state renderer that displays a generic loading indicator.
 */
class LoadingImageWidgetRenderer : ImageWidgetRenderer {
    @Suppress("ComposableNaming")
    @Composable
    override fun invoke(
        url: String,
        contentDescription: String?,
        node: Node,
        modifier: Modifier,
    ) {
        LoadingView(modifier)
    }
}

/**
 * Default error state renderer that displays an error placeholder with alt text.
 *
 * @param imageTheme Theme providing the error placeholder color and shape.
 */
class ErrorImageWidgetRenderer(
    private val imageTheme: ImageTheme,
) : ImageWidgetRenderer {
    @Suppress("ComposableNaming")
    @Composable
    override fun invoke(
        url: String,
        contentDescription: String?,
        node: Node,
        modifier: Modifier,
    ) {
        MarkdownImageErrorView(
            modifier =
                modifier
                    .background(color = imageTheme.errorPlaceholderColor)
                    .clip(imageTheme.shape)
                    .then(imageTheme.modifier),
            altText = contentDescription,
        )
    }
}

/**
 * Composable that displays an error view when an image fails to load,
 * showing the alt text or a default "Image load failed" message.
 *
 * @param modifier Modifier applied to the error view container.
 * @param altText Alternative text to display; falls back to "Image load failed" when null.
 */
@Composable
fun MarkdownImageErrorView(
    modifier: Modifier = Modifier,
    altText: String? = null,
) {
    Box(
        modifier = modifier.padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = altText ?: "Image load failed",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

/**
 * Inline node string builder for markdown [Image] elements that extracts the image URL
 * and alt text from the commonmark node, then appends a standalone inline content placeholder
 * that renders the image via [MarkdownImage].
 *
 * @param imageTheme Theme controlling the image appearance.
 * @param loadingView Renderer displayed during image loading.
 * @param errorView Renderer displayed when the image fails to load.
 * @see IInlineNodeStringBuilder
 */
class ImageNodeStringBuilder(
    private val imageTheme: ImageTheme = ImageTheme(),
    private val loadingView: ImageWidgetRenderer = LoadingImageWidgetRenderer(),
    errorView: ImageWidgetRenderer? = null,
) : IInlineNodeStringBuilder<Image> {
    private val errorView: ImageWidgetRenderer =
        errorView ?: ErrorImageWidgetRenderer(imageTheme)

    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: Image,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    ) {
        val url = node.destination.trim()
        val altText = node.textContent().takeIf { it.isNotBlank() }

        if (url.isNotBlank()) {
            val imageId = "image_$url"
            inlineContentMap[imageId] =
                MarkdownInlineView.MarkdownRichTextInlineContent(
                    RichTextInlineContent.StandaloneInlineContent(
                        modifier = Modifier,
                    ) { modifier ->
                        MarkdownImage(
                            url = url,
                            contentDescription = altText,
                            node = node,
                            modifier =
                                modifier
                                    .clip(imageTheme.shape)
                                    .then(imageTheme.modifier),
                            contentScale = imageTheme.contentScale,
                            alignment = imageTheme.alignment,
                            loadingView = loadingView,
                            errorView = errorView,
                        )
                    },
                )
            appendStandaloneInlineTextContent(imageId, "[${altText ?: "Image"}]")
        } else {
            altText?.let {
                append("[$it]")
            }
        }
    }
}
