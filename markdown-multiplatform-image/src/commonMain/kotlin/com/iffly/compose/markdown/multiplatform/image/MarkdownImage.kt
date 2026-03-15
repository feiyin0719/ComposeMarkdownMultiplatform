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
import org.intellij.markdown.ast.ASTNode

@Composable
fun MarkdownImage(
    url: String,
    contentDescription: String?,
    node: ASTNode,
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
