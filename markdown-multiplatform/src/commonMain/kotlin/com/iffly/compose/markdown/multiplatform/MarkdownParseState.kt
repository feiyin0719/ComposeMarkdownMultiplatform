package com.iffly.compose.markdown.multiplatform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import com.iffly.compose.markdown.multiplatform.render.MarkdownParser
import com.iffly.compose.markdown.multiplatform.streaming.StreamingMarkdownParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.commonmark.node.Node

internal sealed interface MarkdownParseState {
    data object Loading : MarkdownParseState

    data class Success(
        val node: Node,
    ) : MarkdownParseState

    data class Error(
        val throwable: Throwable,
    ) : MarkdownParseState
}

@Composable
internal fun rememberMarkdownNode(
    text: String,
    isStreaming: Boolean,
    streamingParser: StreamingMarkdownParser?,
    fallbackParser: MarkdownParser,
): Node =
    remember(text, isStreaming, streamingParser, fallbackParser) {
        if (isStreaming && streamingParser != null) {
            streamingParser.parse(text, true)
        } else {
            fallbackParser.parse(text)
        }
    }

@Composable
internal fun rememberAsyncMarkdownNode(
    text: String,
    isStreaming: Boolean,
    streamingParser: StreamingMarkdownParser?,
    fallbackParser: MarkdownParser,
    dispatcher: CoroutineDispatcher,
): State<MarkdownParseState> =
    produceState<MarkdownParseState>(
        initialValue = MarkdownParseState.Loading,
        text,
        isStreaming,
        streamingParser,
        fallbackParser,
        dispatcher,
    ) {
        if (!isStreaming || streamingParser == null || value !is MarkdownParseState.Success) {
            value = MarkdownParseState.Loading
        }
        value =
            try {
                val parsedNode =
                    withContext(dispatcher) {
                        if (isStreaming && streamingParser != null) {
                            streamingParser.parse(text, true)
                        } else {
                            fallbackParser.parse(text)
                        }
                    }
                MarkdownParseState.Success(parsedNode)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (throwable: Throwable) {
                MarkdownParseState.Error(throwable)
            }
    }
