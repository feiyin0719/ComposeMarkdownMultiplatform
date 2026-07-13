package com.iffly.compose.markdown.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iffly.compose.markdown.multiplatform.LazyMarkdownView
import com.iffly.compose.markdown.multiplatform.LazyMarkdownViewState
import com.iffly.compose.markdown.multiplatform.chunkloader.MarkdownChunkLoaderConfig

@Composable
fun LazyMarkdownViewExample(
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val markdown =
        remember {
            buildString {
                repeat(160) { section ->
                    appendLine("# Section $section")
                    appendLine()
                    appendLine("This section is loaded from a Markdown string.")
                    appendLine()
                    appendLine("- Item A")
                    appendLine("- Item B")
                    appendLine()
                }
            }
        }
    val loaderConfig =
        remember {
            MarkdownChunkLoaderConfig(
                initialLineCount = 80,
                incrementalLineCount = 60,
                minNodesAhead = 20,
                minNodesBehind = 8,
                maxCachedNodes = 60,
                maxCachedSourceLines = 500,
                minRecycleNodeCount = 10,
            )
        }
    var isInitialLoading by remember { mutableStateOf(false) }
    var loadState by remember { mutableStateOf(LazyMarkdownViewState.Idle) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .padding(paddingValues),
    ) {
        LazyMarkdownView(
            text = markdown,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
            chunkLoaderConfig = loaderConfig,
            onLoadingChanged = { isInitialLoading = it },
            onStateChanged = { loadState = it },
            onError = { errorMessage = it.message ?: it::class.simpleName },
        )
        if (isInitialLoading) {
            LinearProgressIndicator(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
            )
        }
        errorMessage?.let { message ->
            Text(
                text = message,
                modifier = Modifier.align(Alignment.Center),
            )
        }
        if (!isInitialLoading && loadState != LazyMarkdownViewState.Idle) {
            Text(
                text = loadState.name,
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
            )
        }
    }
}
