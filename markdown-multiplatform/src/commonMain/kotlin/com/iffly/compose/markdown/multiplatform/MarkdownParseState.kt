package com.iffly.compose.markdown.multiplatform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
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
    parser: StreamingMarkdownParser,
): Node = remember(text, isStreaming, parser) { parser.parse(text, isStreaming) }

@Composable
internal fun rememberAsyncMarkdownNode(
    text: String,
    isStreaming: Boolean,
    parser: StreamingMarkdownParser,
    dispatcher: CoroutineDispatcher,
): State<MarkdownParseState> =
    produceState<MarkdownParseState>(
        initialValue = MarkdownParseState.Loading,
        text,
        isStreaming,
        parser,
        dispatcher,
    ) {
        if (!isStreaming || value !is MarkdownParseState.Success) {
            value = MarkdownParseState.Loading
        }
        value =
            try {
                val parsedNode =
                    withContext(dispatcher) {
                        parser.parse(text, isStreaming)
                    }
                MarkdownParseState.Success(parsedNode)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (throwable: Throwable) {
                MarkdownParseState.Error(throwable)
            }
    }
