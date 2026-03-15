package com.iffly.compose.markdown.sample

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iffly.compose.markdown.multiplatform.MarkdownView
import kotlinx.coroutines.delay

private val dynamicChunks =
    listOf(
        """
        # Dynamic Markdown Example

        Content is appended every 500ms, demonstrating real-time updates.

        ## Text Formatting

        **Bold text** and *italic text* as well as `inline code`
        """.trimIndent(),
        """
        ## Lists

        1. First item
        2. Second item
           - Nested item A
           - Nested item B
        3. Third item
        """.trimIndent(),
        """
        ## Blockquotes

        > This is a blockquote that was dynamically appended.
        > It demonstrates real-time rendering of complex elements.

        > Nested blockquotes:
        >> This is nested inside the outer quote.
        """.trimIndent(),
        """
        ## Code Blocks

        ```kotlin
        fun streamContent(text: String) {
            text.forEachIndexed { index, _ ->
                delay(20)
                updateDisplay(text.substring(0, index + 1))
            }
        }
        ```
        """.trimIndent(),
        """
        ---

        ## Links

        [Kotlin](https://kotlinlang.org) | [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/)

        ---

        *Dynamic content complete!*
        """.trimIndent(),
    )

@Composable
fun DynamicContentExample(
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
) {
    var appended by remember { mutableStateOf(listOf(dynamicChunks[0])) }

    LaunchedEffect(Unit) {
        for (i in 1 until dynamicChunks.size) {
            delay(500)
            appended = appended + dynamicChunks[i]
        }
    }

    val content =
        remember(appended) {
            appended.joinToString(separator = "\n\n")
        }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
    ) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                ),
        ) {
            Text(
                text = "Dynamic Markdown content is appended every 500ms.",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }

        SelectionContainer {
            MarkdownView(
                text = content,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
