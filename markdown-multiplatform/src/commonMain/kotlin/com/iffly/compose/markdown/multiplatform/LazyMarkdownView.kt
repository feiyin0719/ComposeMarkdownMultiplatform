@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.iffly.compose.markdown.multiplatform

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListPrefetchStrategy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.iffly.compose.markdown.multiplatform.chunkloader.MarkdownChunkLoader
import com.iffly.compose.markdown.multiplatform.chunkloader.MarkdownChunkLoaderConfig
import com.iffly.compose.markdown.multiplatform.chunkloader.MarkdownChunkNode
import com.iffly.compose.markdown.multiplatform.chunkloader.MarkdownLineSource
import com.iffly.compose.markdown.multiplatform.chunkloader.MarkdownNodeWindow
import com.iffly.compose.markdown.multiplatform.chunkloader.MarkdownNodeWindowSnapshot
import com.iffly.compose.markdown.multiplatform.chunkloader.StringMarkdownLineSource
import com.iffly.compose.markdown.multiplatform.config.LocalNodeDataMap
import com.iffly.compose.markdown.multiplatform.config.MarkdownRenderConfig
import com.iffly.compose.markdown.multiplatform.config.currentRenderRegistry
import com.iffly.compose.markdown.multiplatform.config.currentTheme
import com.iffly.compose.markdown.multiplatform.render.MarkdownContent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.yield
import org.commonmark.node.Node
import org.commonmark.parser.IncludeSourceSpans

/** Observable line-source and parsing activity. AST recycling remains [Idle]. */
enum class LazyMarkdownViewState {
    Idle,
    InitialLoading,
    LoadingBefore,
    LoadingAfter,
}

/** Lazily renders an already-loaded Markdown string using the same bounded AST window. */
@Suppress("ktlint:compose:parameter-naming")
@Composable
fun LazyMarkdownView(
    text: String,
    modifier: Modifier = Modifier,
    markdownRenderConfig: MarkdownRenderConfig =
        remember { MarkdownRenderConfig.Builder().build() },
    actionHandler: ActionHandler? = null,
    renderDependencies: Map<String, Any> = emptyMap(),
    showNotSupported: Boolean = false,
    chunkLoaderConfig: MarkdownChunkLoaderConfig = MarkdownChunkLoaderConfig(),
    nestedPrefetchItemCount: Int = 3,
    lazyListState: LazyListState =
        rememberLazyListState(
            prefetchStrategy = LazyListPrefetchStrategy(nestedPrefetchItemCount),
        ),
    onLoadingChanged: (Boolean) -> Unit = {},
    onStateChanged: (LazyMarkdownViewState) -> Unit = {},
    onError: (Throwable) -> Unit = {},
) {
    val source = remember(text) { StringMarkdownLineSource(text) }
    LazyMarkdownView(
        source = source,
        modifier = modifier,
        markdownRenderConfig = markdownRenderConfig,
        actionHandler = actionHandler,
        renderDependencies = renderDependencies,
        showNotSupported = showNotSupported,
        chunkLoaderConfig = chunkLoaderConfig,
        nestedPrefetchItemCount = nestedPrefetchItemCount,
        lazyListState = lazyListState,
        onLoadingChanged = onLoadingChanged,
        onStateChanged = onStateChanged,
        onError = onError,
    )
}

/**
 * Lazily reads, parses, and renders Markdown from a line-oriented source.
 *
 * Unlike [LazyMarkdownColumn], this component does not require the complete source in memory. It
 * requests lines incrementally as the viewport approaches either edge of the loaded AST window.
 * Nodes far behind the viewport are recycled, while their line ranges are retained for backward
 * loading. Stable item keys let [LazyColumn] preserve the visible scroll anchor during recycling.
 *
 * Independently parsed ranges do not share reference-definition state. Use inline links, or choose
 * [LazyMarkdownColumn] when reference-style links and other full-document parsing semantics are
 * required. Custom parsers must preserve source spans on every emitted top-level node.
 *
 * @param source Stable, rereadable Markdown line source. Remember it at the call site.
 * @param modifier Modifier applied to the underlying LazyColumn.
 * @param markdownRenderConfig Parser, theme, and renderer configuration. Parsing always includes
 * at least block source spans, even when the regular parser is configured with `NONE`.
 * @param actionHandler Optional interaction handler.
 * @param renderDependencies Dependencies available to custom renderers and string builders.
 * @param showNotSupported Whether unsupported nodes produce fallback text.
 * @param chunkLoaderConfig Line loading, prefetch, dispatcher, and recycling configuration.
 * @param nestedPrefetchItemCount Compose lazy-item precomposition distance, separate from node watermarks.
 * @param lazyListState State used to observe and control scrolling.
 * @param onLoadingChanged Reports only the initial wait while no content is available.
 * @param onStateChanged Reports initial and background line-source/parsing activity.
 * @param onError Called when reading, parsing, or reloading fails.
 */
@Suppress("ktlint:compose:parameter-naming")
@Composable
fun LazyMarkdownView(
    source: MarkdownLineSource,
    modifier: Modifier = Modifier,
    markdownRenderConfig: MarkdownRenderConfig =
        remember { MarkdownRenderConfig.Builder().build() },
    actionHandler: ActionHandler? = null,
    renderDependencies: Map<String, Any> = emptyMap(),
    showNotSupported: Boolean = false,
    chunkLoaderConfig: MarkdownChunkLoaderConfig = MarkdownChunkLoaderConfig(),
    nestedPrefetchItemCount: Int = 3,
    lazyListState: LazyListState =
        rememberLazyListState(
            prefetchStrategy = LazyListPrefetchStrategy(nestedPrefetchItemCount),
        ),
    onLoadingChanged: (Boolean) -> Unit = {},
    onStateChanged: (LazyMarkdownViewState) -> Unit = {},
    onError: (Throwable) -> Unit = {},
) {
    require(nestedPrefetchItemCount >= 0) { "nestedPrefetchItemCount must be non-negative" }
    val lazyMarkdownParser =
        remember(markdownRenderConfig) {
            markdownRenderConfig.createMarkdownParser(minimumSourceSpans = IncludeSourceSpans.BLOCKS)
        }
    val loader =
        remember(
            source,
            lazyMarkdownParser,
            chunkLoaderConfig,
        ) {
            MarkdownChunkLoader(
                source = source,
                parser = lazyMarkdownParser,
                maxCachedSourceLines = chunkLoaderConfig.maxCachedSourceLines,
                sourceDispatcher = chunkLoaderConfig.sourceDispatcher,
                parserDispatcher = chunkLoaderConfig.parserDispatcher,
            )
        }
    val window = remember(loader, chunkLoaderConfig) { MarkdownNodeWindow(loader, chunkLoaderConfig) }
    var snapshot by remember(window) { mutableStateOf(MarkdownNodeWindowSnapshot.Empty) }
    var operationInProgress by remember(window) { mutableStateOf(false) }
    val nodeDataMap = remember(window) { mutableStateMapOf<Node, Any>() }
    val currentOnLoadingChanged by rememberUpdatedState(onLoadingChanged)
    val currentOnStateChanged by rememberUpdatedState(onStateChanged)
    val currentOnError by rememberUpdatedState(onError)

    suspend fun performOperation(
        state: LazyMarkdownViewState?,
        operation: suspend () -> Unit,
    ) {
        if (operationInProgress) return
        operationInProgress = true
        if (state != null) currentOnStateChanged(state)
        if (state == LazyMarkdownViewState.InitialLoading) currentOnLoadingChanged(true)
        try {
            operation()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            currentOnError(throwable)
        } finally {
            operationInProgress = false
            if (state == LazyMarkdownViewState.InitialLoading) currentOnLoadingChanged(false)
            if (state != null) currentOnStateChanged(LazyMarkdownViewState.Idle)
        }
    }

    LaunchedEffect(window) {
        performOperation(LazyMarkdownViewState.InitialLoading) {
            snapshot = window.loadInitial()
        }
    }
    LaunchedEffect(snapshot.nodes) {
        val retainedNodes = snapshot.nodes.flatMapTo(mutableSetOf()) { chunk -> chunk.node.descendantsAndSelf() }
        nodeDataMap.keys.toList().forEach { node ->
            if (node !in retainedNodes) {
                nodeDataMap.remove(node)
            }
        }
    }
    LaunchedEffect(window, lazyListState, chunkLoaderConfig) {
        snapshotFlow {
            val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
            val first = visibleItems.firstOrNull()
            val last = visibleItems.lastOrNull()
            VisibleNodeWindow(
                firstIndex = first?.index ?: -1,
                lastIndex = last?.index ?: -1,
                firstKey = first?.key as? String,
                lastKey = last?.key as? String,
                nodeCount = snapshot.nodes.size,
                canLoadBefore = snapshot.canLoadBefore,
                canLoadAfter = snapshot.canLoadAfter,
                needsRecycle = snapshot.needsRecycle,
                cachedSourceLineCount = snapshot.cachedSourceLineCount,
                loadDirection =
                    when {
                        lazyListState.lastScrolledBackward -> LoadDirection.Before
                        lazyListState.lastScrolledForward -> LoadDirection.After
                        else -> LoadDirection.After
                    },
            )
        }.distinctUntilChanged()
            .collect { visible ->
                val shouldLoadBefore = visible.shouldLoadBefore(chunkLoaderConfig.minNodesBehind)
                val shouldLoadAfter = visible.shouldLoadAfter(chunkLoaderConfig.minNodesAhead)
                if (visible.shouldRecycle(chunkLoaderConfig)) {
                    performOperation(null) {
                        snapshot = window.recycle(visible.firstKey, visible.lastKey)
                    }
                } else if (shouldLoadBefore &&
                    (!shouldLoadAfter || visible.loadDirection == LoadDirection.Before)
                ) {
                    performOperation(LazyMarkdownViewState.LoadingBefore) {
                        var updated = snapshot
                        do {
                            updated = window.loadBefore(visible.lastKey)
                            yield()
                        } while (
                            updated.canLoadBefore &&
                            !updated.needsRecycle &&
                            updated.nodes.nodesBefore(visible.firstKey) < chunkLoaderConfig.minNodesBehind
                        )
                        snapshot = updated
                    }
                } else if (shouldLoadAfter) {
                    performOperation(LazyMarkdownViewState.LoadingAfter) {
                        var updated = snapshot
                        do {
                            updated = window.loadAfter(visible.firstKey)
                            yield()
                        } while (
                            updated.canLoadAfter &&
                            !updated.needsRecycle &&
                            updated.nodes.nodesAfter(visible.lastKey) < chunkLoaderConfig.minNodesAhead
                        )
                        snapshot = updated
                    }
                }
            }
    }
    DisposableEffect(window) {
        onDispose {
            currentOnLoadingChanged(false)
            currentOnStateChanged(LazyMarkdownViewState.Idle)
        }
    }

    ProvideMarkdownLocals(
        markdownRenderConfig = markdownRenderConfig,
        actionHandler = actionHandler,
        renderDependencies = renderDependencies,
        showNotSupported = showNotSupported,
    ) {
        CompositionLocalProvider(LocalNodeDataMap provides nodeDataMap) {
            val theme = currentTheme()
            val renderRegistry = currentRenderRegistry()
            LazyColumn(
                modifier = modifier,
                state = lazyListState,
            ) {
                itemsIndexed(
                    items = snapshot.nodes,
                    key = { _, chunk -> chunk.key },
                    contentType = { _, chunk -> chunk.node::class },
                ) { index, chunk ->
                    val node = chunk.node
                    if (!renderRegistry.shouldSkipRender(node)) {
                        MarkdownContent(node = node, modifier = Modifier)
                        if (index != snapshot.nodes.lastIndex &&
                            theme.spacerTheme.showSpacer &&
                            renderRegistry.getBlockRenderer(node) != null
                        ) {
                            Spacer(Modifier.height(theme.spacerTheme.spacerHeight))
                        }
                    }
                }
            }
        }
    }
}

private fun Node.descendantsAndSelf(): List<Node> =
    buildList {
        val pending = ArrayDeque<Node>()
        pending.addLast(this@descendantsAndSelf)
        while (pending.isNotEmpty()) {
            val node = pending.removeLast()
            add(node)
            var child = node.lastChild
            while (child != null) {
                pending.addLast(child)
                child = child.previous
            }
        }
    }

private enum class LoadDirection {
    Before,
    After,
}

private data class VisibleNodeWindow(
    val firstIndex: Int,
    val lastIndex: Int,
    val firstKey: String?,
    val lastKey: String?,
    val nodeCount: Int,
    val canLoadBefore: Boolean,
    val canLoadAfter: Boolean,
    val needsRecycle: Boolean,
    val cachedSourceLineCount: Int,
    val loadDirection: LoadDirection,
) {
    fun shouldLoadBefore(minNodesBehind: Int): Boolean = canLoadBefore && firstIndex in 0 until minNodesBehind

    fun shouldLoadAfter(minNodesAhead: Int): Boolean = canLoadAfter && lastIndex >= 0 && nodeCount - lastIndex - 1 < minNodesAhead

    fun shouldRecycle(config: MarkdownChunkLoaderConfig): Boolean {
        if (!needsRecycle || firstIndex < 0 || lastIndex < 0) return false
        val removableBefore = firstIndex - config.minNodesBehind
        val removableAfter = nodeCount - lastIndex - 1 - config.minNodesAhead
        val removableCount = maxOf(removableBefore, removableAfter)
        return removableCount >= config.minRecycleNodeCount ||
            (removableCount > 0 && cachedSourceLineCount > config.maxCachedSourceLines)
    }
}

private fun List<MarkdownChunkNode>.nodesBefore(key: String?): Int = indexOfFirst { node -> node.key == key }.coerceAtLeast(0)

private fun List<MarkdownChunkNode>.nodesAfter(key: String?): Int {
    val index = indexOfLast { node -> node.key == key }
    return if (index < 0) 0 else size - index - 1
}
