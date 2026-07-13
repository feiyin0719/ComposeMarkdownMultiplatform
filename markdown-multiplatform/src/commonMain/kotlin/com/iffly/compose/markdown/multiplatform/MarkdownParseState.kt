package com.iffly.compose.markdown.multiplatform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import com.iffly.compose.markdown.multiplatform.render.MarkdownParser
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
internal fun rememberAsyncMarkdownNode(
    text: String,
    parser: MarkdownParser,
    dispatcher: CoroutineDispatcher,
): State<MarkdownParseState> =
    produceState<MarkdownParseState>(
        initialValue = MarkdownParseState.Loading,
        text,
        parser,
        dispatcher,
    ) {
        value = MarkdownParseState.Loading
        value =
            try {
                MarkdownParseState.Success(
                    withContext(dispatcher) {
                        parser.parse(text)
                    },
                )
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (throwable: Throwable) {
                MarkdownParseState.Error(throwable)
            }
    }
