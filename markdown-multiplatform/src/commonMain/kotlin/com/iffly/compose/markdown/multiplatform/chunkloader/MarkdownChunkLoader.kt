package com.iffly.compose.markdown.multiplatform.chunkloader

import com.iffly.compose.markdown.multiplatform.render.MarkdownParser
import com.iffly.compose.markdown.multiplatform.render.childNodes
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.commonmark.node.Node

/**
 * A zero-based, random-access source of Markdown lines.
 *
 * Implementations may read from files, assets, databases, or network range APIs. The source must
 * support rereading previously returned ranges and remain unchanged for its lifetime. Returning
 * fewer lines than requested marks end-of-source; returning an empty list marks it immediately.
 */
fun interface MarkdownLineSource {
    /**
     * Reads at most [lineCount] lines beginning at zero-based [startLine].
     * Returning fewer lines than requested signals end-of-source.
     */
    suspend fun readLines(
        startLine: Int,
        lineCount: Int,
    ): List<String>
}

/**
 * In-memory [MarkdownLineSource] intended for samples and already-loaded Markdown strings.
 * @param text Immutable Markdown source captured by this line source.
 */
class StringMarkdownLineSource(
    text: String,
) : MarkdownLineSource {
    private val lines = text.lines()

    override suspend fun readLines(
        startLine: Int,
        lineCount: Int,
    ): List<String> {
        require(startLine >= 0) { "startLine must be non-negative" }
        require(lineCount > 0) { "lineCount must be positive" }
        if (startLine >= lines.size) return emptyList()
        return lines.subList(startLine, minOf(startLine + lineCount, lines.size))
    }
}

/**
 * Configuration for incremental line loading, viewport prefetching, and AST cache recycling.
 *
 * @param initialLineCount Lines requested for the first parse.
 * @param incrementalLineCount Lines requested when either loaded edge needs more content.
 * @param minNodesAhead Minimum parsed nodes retained after the last visible item.
 * @param minNodesBehind Minimum parsed nodes retained before the first visible item.
 * @param maxCachedNodes Target maximum number of parsed top-level nodes retained in memory.
 * @param maxCachedSourceLines Maximum source-line span retained by pending text or cached AST nodes.
 * @param minRecycleNodeCount Minimum batch removed during one recycling operation.
 * @param sourceDispatcher Dispatcher used to call [MarkdownLineSource.readLines].
 * @param parserDispatcher Dispatcher used for CommonMark parsing.
 */
data class MarkdownChunkLoaderConfig(
    val initialLineCount: Int = 1000,
    val incrementalLineCount: Int = 500,
    val minNodesAhead: Int = 100,
    val minNodesBehind: Int = 30,
    val maxCachedNodes: Int = 500,
    val maxCachedSourceLines: Int = 10_000,
    val minRecycleNodeCount: Int = 50,
    val sourceDispatcher: CoroutineDispatcher = Dispatchers.Default,
    val parserDispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    init {
        require(initialLineCount > 0) { "initialLineCount must be positive" }
        require(incrementalLineCount > 0) { "incrementalLineCount must be positive" }
        require(minNodesAhead > 0) { "minNodesAhead must be positive" }
        require(minNodesBehind >= 0) { "minNodesBehind must be non-negative" }
        require(maxCachedNodes > 0) { "maxCachedNodes must be positive" }
        require(maxCachedSourceLines > 0) { "maxCachedSourceLines must be positive" }
        require(minRecycleNodeCount > 0) { "minRecycleNodeCount must be positive" }
        require(minRecycleNodeCount <= maxCachedNodes) {
            "minRecycleNodeCount must not exceed maxCachedNodes"
        }
        require(minNodesAhead + minNodesBehind < maxCachedNodes) {
            "minNodesAhead + minNodesBehind must be smaller than maxCachedNodes"
        }
    }
}

internal data class MarkdownChunkNode(
    val node: Node,
    val startLine: Int,
    val endLine: Int,
    val key: String,
)

internal data class MarkdownChunkLoadResult(
    val nodes: List<MarkdownChunkNode>,
    val endOfSource: Boolean,
)

internal data class MarkdownNodeWindowSnapshot(
    val nodes: List<MarkdownChunkNode>,
    val canLoadBefore: Boolean,
    val canLoadAfter: Boolean,
    val needsRecycle: Boolean,
    val cachedSourceLineCount: Int,
) {
    companion object {
        val Empty = MarkdownNodeWindowSnapshot(emptyList(), false, true, false, 0)
    }
}

private data class EvictedNodeRange(
    val lines: IntRange,
    val nodeCount: Int,
    val keyHash: Int,
)

/**
 * Incrementally parses line ranges while retaining the trailing block until the next range proves
 * its boundary. This avoids emitting partial fenced blocks, lists, block quotes, and paragraphs.
 */
internal class MarkdownChunkLoader(
    private val source: MarkdownLineSource,
    private val parser: MarkdownParser,
    private val maxCachedSourceLines: Int = 10_000,
    private val sourceDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val parserDispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    private val mutex = Mutex()
    private var pendingStartLine = 0
    private val pendingLines = mutableListOf<String>()
    private var nextLine = 0
    private var endOfSource = false

    suspend fun load(lineCount: Int): MarkdownChunkLoadResult =
        mutex.withLock {
            if (endOfSource) return@withLock MarkdownChunkLoadResult(emptyList(), true)

            val loadedLines = readLines(nextLine, lineCount)
            require(loadedLines.size <= lineCount) {
                "MarkdownLineSource returned ${loadedLines.size} lines for a $lineCount-line request"
            }
            val reachedEnd = loadedLines.size < lineCount
            nextLine += loadedLines.size
            pendingLines.addAll(loadedLines)

            if (pendingLines.isEmpty()) {
                endOfSource = true
                return@withLock MarkdownChunkLoadResult(emptyList(), true)
            }

            val parsedNodes = parse(pendingLines)
            val trailingNode =
                parsedNodes.lastOrNull()?.takeIf { node ->
                    !reachedEnd && node.maxSourceLine()?.let { it >= pendingLines.lastIndex } == true
                }
            val emittedNodes = if (trailingNode == null) parsedNodes else parsedNodes.dropLast(1)
            val chunks = toChunkNodes(emittedNodes, pendingStartLine)

            if (reachedEnd) {
                pendingLines.clear()
                endOfSource = true
            } else {
                if (chunks.isNotEmpty()) {
                    val consumedLineCount = chunks.last().endLine - pendingStartLine + 1
                    pendingLines.subList(0, consumedLineCount).clear()
                    pendingStartLine += consumedLineCount
                }
                discardLeadingBlankLines()
                require(pendingLines.size <= maxCachedSourceLines) {
                    "Unconfirmed Markdown source exceeded maxCachedSourceLines=$maxCachedSourceLines"
                }
            }

            MarkdownChunkLoadResult(chunks, endOfSource)
        }

    suspend fun reload(range: IntRange): List<MarkdownChunkNode> =
        mutex.withLock {
            require(!range.isEmpty()) { "range must not be empty" }
            val expectedLineCount = range.last - range.first + 1
            val lines = readLines(range.first, expectedLineCount)
            require(lines.size == expectedLineCount) {
                "MarkdownLineSource changed while reloading lines ${range.first}..${range.last}"
            }
            toChunkNodes(parse(lines), range.first)
        }

    private suspend fun readLines(
        startLine: Int,
        lineCount: Int,
    ): List<String> =
        withContext(sourceDispatcher) {
            source.readLines(startLine, lineCount)
        }

    private suspend fun parse(lines: List<String>): List<Node> =
        withContext(parserDispatcher) {
            parser
                .parse(lines.joinToString("\n"))
                .childNodes()
                .onEach(Node::unlink)
        }

    private fun discardLeadingBlankLines() {
        val firstContentIndex = pendingLines.indexOfFirst(String::isNotBlank)
        val discardCount = if (firstContentIndex < 0) pendingLines.size else firstContentIndex
        if (discardCount > 0) {
            pendingLines.subList(0, discardCount).clear()
            pendingStartLine += discardCount
        }
    }

    private fun toChunkNodes(
        nodes: List<Node>,
        baseLine: Int,
    ): List<MarkdownChunkNode> {
        val keys = mutableSetOf<String>()
        return nodes.map { node ->
            val spans = node.getSourceSpans()
            require(spans.isNotEmpty()) {
                "${node::class.simpleName} has no source spans; LazyMarkdownView requires source-aware parser nodes"
            }
            val key = node.sourceSpanKey(spans, baseLine)
            val sourceLineCount = spans.maxOf { it.lineIndex } - spans.minOf { it.lineIndex } + 1
            require(sourceLineCount <= maxCachedSourceLines) {
                "${node::class.simpleName} exceeded maxCachedSourceLines=$maxCachedSourceLines"
            }
            require(keys.add(key)) {
                "LazyMarkdownView requires unique source spans for top-level nodes"
            }
            toChunkNode(
                node = node,
                spans = spans,
                baseLine = baseLine,
                key = key,
            )
        }
    }

    private fun toChunkNode(
        node: Node,
        spans: List<org.commonmark.node.SourceSpan>,
        baseLine: Int,
        key: String,
    ): MarkdownChunkNode =
        MarkdownChunkNode(
            node = node,
            startLine = baseLine + spans.minOf { it.lineIndex },
            endLine = baseLine + spans.maxOf { it.lineIndex },
            key = key,
        )

    private fun Node.sourceSpanKey(
        spans: List<org.commonmark.node.SourceSpan>,
        baseLine: Int,
    ): String =
        buildString {
            append(this@sourceSpanKey::class.simpleName)
            spans.forEach { span ->
                append(':')
                append(baseLine + span.lineIndex)
                append(':')
                append(span.columnIndex)
                append(':')
                append(span.length)
            }
        }

    private fun Node.maxSourceLine(): Int? = getSourceSpans().maxOfOrNull { it.lineIndex }
}

/** Maintains a bounded AST window and exact line ranges for nodes evicted on either side. */
internal class MarkdownNodeWindow(
    private val loader: MarkdownChunkLoader,
    private val config: MarkdownChunkLoaderConfig,
) {
    private val mutex = Mutex()
    private var nodes = emptyList<MarkdownChunkNode>()
    private val evictedBefore = ArrayDeque<EvictedNodeRange>()
    private val evictedAfter = ArrayDeque<EvictedNodeRange>()
    private var endOfSource = false

    suspend fun loadInitial(): MarkdownNodeWindowSnapshot =
        mutex.withLock {
            if (nodes.isEmpty() && !endOfSource) {
                appendFromSource(config.initialLineCount)
                while (
                    nodes.size < config.minNodesAhead &&
                    nodes.size < config.maxCachedNodes &&
                    cachedSourceLineCount() < config.maxCachedSourceLines &&
                    !endOfSource
                ) {
                    appendFromSource(config.incrementalLineCount)
                }
            }
            snapshot()
        }

    suspend fun loadBefore(lastVisibleKey: String?): MarkdownNodeWindowSnapshot =
        mutex.withLock {
            val range = evictedBefore.lastOrNull() ?: return@withLock snapshot()
            val loaded = reload(range)
            evictedBefore.removeLast()
            nodes = loaded + nodes
            recycleAfter(lastVisibleKey)
            snapshot()
        }

    suspend fun loadAfter(firstVisibleKey: String?): MarkdownNodeWindowSnapshot =
        mutex.withLock {
            if (evictedAfter.isNotEmpty()) {
                val range = evictedAfter.first()
                val loaded = reload(range)
                evictedAfter.removeFirst()
                nodes = nodes + loaded
            } else if (!endOfSource) {
                appendFromSource(config.incrementalLineCount)
            }
            recycleBefore(firstVisibleKey)
            snapshot()
        }

    suspend fun recycle(
        firstVisibleKey: String?,
        lastVisibleKey: String?,
    ): MarkdownNodeWindowSnapshot =
        mutex.withLock {
            val firstVisibleIndex = nodes.indexOfFirst { it.key == firstVisibleKey }
            val lastVisibleIndex = nodes.indexOfLast { it.key == lastVisibleKey }
            if (firstVisibleIndex < 0 || lastVisibleIndex < 0) return@withLock snapshot()

            val removableBefore =
                (firstVisibleIndex - config.minNodesBehind)
                    .coerceAtLeast(0)
            val removableAfter =
                (nodes.lastIndex - lastVisibleIndex - config.minNodesAhead)
                    .coerceAtLeast(0)
            if (removableBefore > removableAfter) {
                recycleBefore(firstVisibleKey)
            } else {
                recycleAfter(lastVisibleKey)
            }
            snapshot()
        }

    private suspend fun appendFromSource(lineCount: Int) {
        do {
            val result = loader.load(lineCount)
            nodes = nodes + result.nodes
            endOfSource = result.endOfSource
        } while (nodes.isEmpty() && !endOfSource)
    }

    private fun recycleBefore(firstVisibleKey: String?) {
        val firstVisibleIndex = nodes.indexOfFirst { it.key == firstVisibleKey }
        if (firstVisibleIndex < 0) return
        val removableCount =
            (firstVisibleIndex - config.minNodesBehind)
                .coerceAtLeast(0)
        val recycleCount = requiredRecycleFromStart().coerceAtMost(removableCount)
        if (!shouldRecycle(recycleCount)) return

        val removed = nodes.take(recycleCount)
        nodes = nodes.drop(recycleCount)
        evictedBefore.addLast(removed.toEvictedRange())
    }

    private fun recycleAfter(lastVisibleKey: String?) {
        val lastVisibleIndex = nodes.indexOfLast { it.key == lastVisibleKey }
        if (lastVisibleIndex < 0) return
        val removableCount =
            (nodes.lastIndex - lastVisibleIndex - config.minNodesAhead)
                .coerceAtLeast(0)
        val recycleCount = requiredRecycleFromEnd().coerceAtMost(removableCount)
        if (!shouldRecycle(recycleCount)) return

        val removed = nodes.takeLast(recycleCount)
        nodes = nodes.dropLast(recycleCount)
        evictedAfter.addFirst(removed.toEvictedRange())
    }

    private suspend fun reload(range: EvictedNodeRange): List<MarkdownChunkNode> {
        val loaded = loader.reload(range.lines)
        require(loaded.size == range.nodeCount && loaded.map { it.key }.hashCode() == range.keyHash) {
            "MarkdownLineSource produced a different node structure while reloading ${range.lines}"
        }
        return loaded
    }

    private fun List<MarkdownChunkNode>.toEvictedRange(): EvictedNodeRange =
        EvictedNodeRange(
            lines = first().startLine..last().endLine,
            nodeCount = size,
            keyHash = map { it.key }.hashCode(),
        )

    private fun requiredRecycleFromStart(): Int {
        var count = (nodes.size - config.maxCachedNodes).coerceAtLeast(0)
        while (count < nodes.size && sourceLineCount(nodes.drop(count)) > config.maxCachedSourceLines) {
            count++
        }
        return count
    }

    private fun requiredRecycleFromEnd(): Int {
        var count = (nodes.size - config.maxCachedNodes).coerceAtLeast(0)
        while (count < nodes.size && sourceLineCount(nodes.dropLast(count)) > config.maxCachedSourceLines) {
            count++
        }
        return count
    }

    private fun shouldRecycle(recycleCount: Int): Boolean =
        recycleCount >= config.minRecycleNodeCount ||
            (recycleCount > 0 && cachedSourceLineCount() > config.maxCachedSourceLines)

    private fun cachedSourceLineCount(): Int = sourceLineCount(nodes)

    private fun sourceLineCount(nodes: List<MarkdownChunkNode>): Int =
        if (nodes.isEmpty()) 0 else nodes.last().endLine - nodes.first().startLine + 1

    private fun snapshot(): MarkdownNodeWindowSnapshot =
        MarkdownNodeWindowSnapshot(
            nodes = nodes,
            canLoadBefore = evictedBefore.isNotEmpty(),
            canLoadAfter = evictedAfter.isNotEmpty() || !endOfSource,
            needsRecycle =
                nodes.size - config.maxCachedNodes >= config.minRecycleNodeCount ||
                    cachedSourceLineCount() > config.maxCachedSourceLines,
            cachedSourceLineCount = cachedSourceLineCount(),
        )
}
