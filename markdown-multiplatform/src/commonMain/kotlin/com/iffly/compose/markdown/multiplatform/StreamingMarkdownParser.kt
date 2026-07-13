package com.iffly.compose.markdown.multiplatform

import com.iffly.compose.markdown.multiplatform.render.MarkdownParser
import org.commonmark.node.Document
import org.commonmark.node.Node
import org.commonmark.node.SourceSpan

/**
 * Parses the replaceable tail of an append-only Markdown stream.
 *
 * [source] contains the complete current Markdown input. [startOffset] is the start of the source
 * line containing the previous document's last top-level block. Implementations parse from that
 * offset through the end and return a document whose source spans are relative to that substring;
 * the library rebases them to the complete source coordinate space before rendering.
 */
fun interface StreamingMarkdownParser {
    fun parse(
        parser: MarkdownParser,
        source: String,
        startOffset: Int,
    ): Node
}

/** Default CommonMark streaming parser. */
object DefaultStreamingMarkdownParser : StreamingMarkdownParser {
    override fun parse(
        parser: MarkdownParser,
        source: String,
        startOffset: Int,
    ): Node = parser.parse(source.substring(startOffset))
}

private data class MarkdownSnapshot(
    val text: String,
    val node: Node,
)

internal sealed interface MarkdownParseRequest {
    val text: String

    data class Reuse(
        override val text: String,
        val node: Node,
    ) : MarkdownParseRequest

    data class Full(
        override val text: String,
    ) : MarkdownParseRequest

    data class Tail(
        override val text: String,
        val startOffset: Int,
        val startLine: Int,
        val previousDocument: Document,
    ) : MarkdownParseRequest
}

internal class StreamingMarkdownParseSession {
    private var snapshot: MarkdownSnapshot? = null

    fun createRequest(
        text: String,
        isStreaming: Boolean,
    ): MarkdownParseRequest {
        if (!isStreaming) return MarkdownParseRequest.Full(text)

        val previous = snapshot ?: return MarkdownParseRequest.Full(text)
        if (text == previous.text) return MarkdownParseRequest.Reuse(text, previous.node)
        if (!text.startsWith(previous.text)) return MarkdownParseRequest.Full(text)

        val previousDocument = previous.node as? Document ?: return MarkdownParseRequest.Full(text)
        val lastBlock = previousDocument.lastChild ?: return MarkdownParseRequest.Full(text)
        val firstSpan =
            lastBlock.getSourceSpans().minByOrNull(SourceSpan::inputIndex)
                ?: return MarkdownParseRequest.Full(text)
        val startOffset = firstSpan.inputIndex - firstSpan.columnIndex
        if (startOffset !in 0..text.length || firstSpan.lineIndex < 0) {
            return MarkdownParseRequest.Full(text)
        }

        return MarkdownParseRequest.Tail(
            text = text,
            startOffset = startOffset,
            startLine = firstSpan.lineIndex,
            previousDocument = previousDocument,
        )
    }

    fun complete(
        request: MarkdownParseRequest,
        parsedNode: Node?,
    ): Node {
        val node =
            when (request) {
                is MarkdownParseRequest.Reuse -> {
                    request.node
                }

                is MarkdownParseRequest.Full -> {
                    requireNotNull(parsedNode)
                }

                is MarkdownParseRequest.Tail -> {
                    mergeTail(request, requireNotNull(parsedNode))
                }
            }
        snapshot = MarkdownSnapshot(request.text, node)
        return node
    }
}

internal fun MarkdownParseRequest.parse(
    parser: MarkdownParser,
    streamingParser: StreamingMarkdownParser,
): Node? =
    when (this) {
        is MarkdownParseRequest.Reuse -> null
        is MarkdownParseRequest.Full -> parser.parse(text)
        is MarkdownParseRequest.Tail -> streamingParser.parse(parser, text, startOffset)
    }

private fun mergeTail(
    request: MarkdownParseRequest.Tail,
    tailNode: Node,
): Document {
    val tailDocument =
        requireNotNull(tailNode as? Document) {
            "StreamingMarkdownParser must return a Document"
        }
    tailDocument.rebaseSourceSpans(request.startLine, request.startOffset)

    val previousDocument = request.previousDocument
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
