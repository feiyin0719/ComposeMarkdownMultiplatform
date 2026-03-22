package com.iffly.compose.markdown.multiplatform.image

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.iffly.compose.markdown.multiplatform.config.currentActionHandler
import org.commonmark.node.Node

/**
 * Composable that asynchronously loads and displays a markdown image using Coil,
 * with crossfade animation, memory and disk caching, and customizable loading/error states.
 *
 * @param url The URL of the image to load.
 * @param contentDescription Accessibility description for the image.
 * @param node The commonmark node representing this image element.
 * @param errorView Renderer displayed when the image fails to load.
 * @param loadingView Renderer displayed while the image is loading.
 * @param modifier Modifier applied to the image.
 * @param alignment Alignment of the image within its bounds.
 * @param contentScale How the image is scaled to fit its bounds.
 */
@Composable
fun MarkdownImage(
    url: String,
    contentDescription: String?,
    node: Node,
    errorView: ImageWidgetRenderer,
    loadingView: ImageWidgetRenderer,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Inside,
) {
    val context = LocalPlatformContext.current
    val actionHandler = currentActionHandler()

    SubcomposeAsyncImage(
        ImageRequest
            .Builder(context)
            .data(url)
            .crossfade(true)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build(),
        contentDescription = contentDescription,
        contentScale = contentScale,
        alignment = alignment,
        modifier =
            modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clickable {
                    actionHandler?.handleImageClick(url, node)
                },
        loading = {
            loadingView(url, contentDescription, node, Modifier)
        },
        error = { errorResult ->
            print("Image load error: ${errorResult.result.throwable}")
            errorView(
                url,
                contentDescription,
                node,
                Modifier
                    .fillMaxWidth(0.5f)
                    .aspectRatio(4f / 3f),
            )
        },
    )
}
