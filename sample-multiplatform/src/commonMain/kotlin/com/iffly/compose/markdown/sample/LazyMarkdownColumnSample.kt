package com.iffly.compose.markdown.sample

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iffly.compose.markdown.multiplatform.LazyMarkdownColumn

@Composable
fun LazyMarkdownColumnExample(
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
) {
    SelectionContainer {
        LazyMarkdownColumn(
            text =
                """
                # LazyMarkdownColumn Example

                This example uses `LazyMarkdownColumn` which renders markdown content
                using `LazyColumn` for efficient display of large documents.

                ## Benefits

                - Only visible items are composed and laid out
                - Better performance for long markdown content
                - Supports scroll state control via `LazyListState`

                ## Text Formatting

                **Bold text** and *italic text* as well as `inline code`

                ## Lists

                ### Ordered Lists
                1. First item
                2. Second item
                   1. Nested item
                   2. Another nested item
                3. Third item

                ### Unordered Lists
                - Item A
                - Item B
                  - Nested item
                  - Another nested item
                - Item C

                ## Quotes

                > This is a quote block rendered inside LazyMarkdownColumn.
                > Each top-level markdown block is a separate lazy item.

                ## Code

                ```kotlin
                // LazyMarkdownColumn usage
                LazyMarkdownColumn(
                    text = markdownContent,
                    modifier = Modifier.fillMaxSize(),
                )
                ```

                ## More Content

                This section demonstrates that LazyMarkdownColumn handles
                long documents efficiently by only rendering visible blocks.

                ---

                ### Section A
                Lorem ipsum dolor sit amet, consectetur adipiscing elit.

                ### Section B
                Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.

                ### Section C
                Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris.

                ---

                *End of LazyMarkdownColumn example*
                """.trimIndent(),
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
        )
    }
}
