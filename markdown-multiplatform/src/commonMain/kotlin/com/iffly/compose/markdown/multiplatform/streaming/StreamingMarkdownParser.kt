package com.iffly.compose.markdown.multiplatform.streaming

import com.iffly.compose.markdown.multiplatform.config.MarkdownRenderConfig
import com.iffly.compose.markdown.multiplatform.render.MarkdownParser
import org.commonmark.node.Document
import org.commonmark.node.Node
import org.commonmark.node.SourceSpan
import org.commonmark.parser.IncludeSourceSpans

/**
 * Owns the parsing lifecycle for Markdown content that may be streaming.
 *
 * Implementations receive only the latest complete [content] and [isStreaming] state, and have full
 * control over caching, incremental parsing, source positions, fallback behavior, and final parsing.
 * Every changed input must return a new root [Document] instance so Compose observes the parsed-node
 * change. Reuse unchanged completed child blocks by identity so keyed renderers can skip
 * recomposing the stable prefix.
 */
interface StreamingMarkdownParser {
    val markdownRenderConfig: MarkdownRenderConfig

    fun parse(
        content: String,
        isStreaming: Boolean,
    ): Node
}

/**
 * Default append-only CommonMark streaming parser.
 *
 * It creates an independent parser from [markdownRenderConfig] with at least block source spans.
 */
class DefaultStreamingMarkdownParser(
    override val markdownRenderConfig: MarkdownRenderConfig,
) : StreamingMarkdownParser {
    private val parser: MarkdownParser =
        markdownRenderConfig.createMarkdownParser(minimumSourceSpans = IncludeSourceSpans.BLOCKS)
    private var snapshot: MarkdownSnapshot? = null

    override fun parse(
        content: String,
        isStreaming: Boolean,
    ): Node {
        if (!isStreaming) return parseFull(content)

        val previous = snapshot ?: return parseFull(content)
        if (content == previous.text) return previous.node
        if (!content.startsWith(previous.text)) return parseFull(content)

        val previousDocument = previous.node as? Document ?: return parseFull(content)
        val lastBlock = previousDocument.lastChild ?: return parseFull(content)
        val firstSpan =
            lastBlock.getSourceSpans().minByOrNull(SourceSpan::inputIndex)
                ?: return parseFull(content)
        val startOffset = firstSpan.inputIndex - firstSpan.columnIndex
        if (startOffset !in 0..content.length || firstSpan.lineIndex < 0) {
            return parseFull(content)
        }

        val tailNode = parser.parse(content.substring(startOffset))
        val document =
            mergeTail(
                previousDocument = previousDocument,
                tailNode = tailNode,
                lineOffset = firstSpan.lineIndex,
                inputOffset = startOffset,
            )
        snapshot = MarkdownSnapshot(content, document)
        return document
    }

    private fun parseFull(content: String): Node {
        val node = parser.parse(content)
        snapshot = MarkdownSnapshot(content, node)
        return node
    }

    private fun mergeTail(
        previousDocument: Document,
        tailNode: Node,
        lineOffset: Int,
        inputOffset: Int,
    ): Document {
        val tailDocument =
            requireNotNull(tailNode as? Document) {
                "MarkdownParser must return a Document"
            }
        tailDocument.rebaseSourceSpans(lineOffset, inputOffset)

        val previousLastBlock = previousDocument.lastChild
        val document = Document()

        var child = previousDocument.firstChild
        while (child != null && child !== previousLastBlock) {
            val next = child.next
            document.appendChild(child)
            child = next
        }

        child = tailDocument.firstChild
        while (child != null) {
            val next = child.next
            document.appendChild(child)
            child = next
        }
        return document
    }
}

private data class MarkdownSnapshot(
    val text: String,
    val node: Node,
)

private fun Node.rebaseSourceSpans(
    lineOffset: Int,
    inputOffset: Int,
) {
    val spans = getSourceSpans()
    if (spans.isNotEmpty()) {
        setSourceSpans(
            spans.map { span ->
                SourceSpan.of(
                    lineIndex = lineOffset + span.lineIndex,
                    columnIndex = span.columnIndex,
                    inputIndex = inputOffset + span.inputIndex,
                    length = span.length,
                )
            },
        )
    }

    var child = firstChild
    while (child != null) {
        child.rebaseSourceSpans(lineOffset, inputOffset)
        child = child.next
    }
}
