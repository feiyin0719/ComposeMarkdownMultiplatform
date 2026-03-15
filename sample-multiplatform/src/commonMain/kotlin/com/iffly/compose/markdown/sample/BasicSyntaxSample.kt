package com.iffly.compose.markdown.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iffly.compose.markdown.multiplatform.ActionHandler
import com.iffly.compose.markdown.multiplatform.MarkdownView
import org.intellij.markdown.ast.ASTNode

@Composable
fun BasicSyntaxExample(
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
    ) {
        SelectionContainer {
            MarkdownView(
            text =
                """
                # Basic Syntax Example

                This is an example demonstrating basic Markdown syntax.

                ## Text Formatting

                **Bold text** and *italic text* as well as `inline code`

                ## Lists

                ### Ordered Lists
                1. First item
                2. Second item
                   1. Nested item
                      - 1
                      - 2
                   2. Another nested item
                3. Third item
                4. 1. Fourth item with number
                   2. Another fourth item

                ### Loose List
                1. Item 1

                    This is a loose list item with multiple paragraphs.

                    Another paragraph in the same list item.

                    > Blockquote inside loose list item.
                    > Another line of blockquote.

                    1. Nested ordered list inside loose item.

                    2. ``` kotlin
                       val x = 10
                       val y = 10
                       ```

                2. > quote as second item
                   >> another line of quote

                   test paragraph after quote.

                   ```java
                     System.out.println("Hello, World!");
                   ```


                ### Unordered Lists
                - Item A
                - Item B
                  - Nested item
                  - Another nested item
                - Item C

                ## Quotes

                > This is a quote block
                > Can contain multiple lines of content
                
                > This is a blockquote. 
                > It can span multiple lines.
                >
                > > Nested blockquote.

                ### Quote with List
                > - Quote list item 1
                > - Quote list item 2
                > 1. Nested quote list item
                > 2. Another nested quote list item
                >    new line for nested quote list item
                >>  - Nested quote list item
                >>  - Another nested quote list item
                > # Quote with Heading1
                > ## Quote with Heading2
                > Regular paragraph inside quote.
                > ```python
                > def hello():
                >     print("Hello from quote!")
                > ```

                ## Dividers

                ---

                ## Links

                [GitHub](https://github.com) | [Google](https://google.com)

                ### Reference Links

                This is a [reference link][ref1].

                This is another [reference link][ref2].

                [ref1]: https://kotlinlang.org
                [ref2]: https://android.com

                ### Collapsed Reference Links

                This is a [collapsed reference link][].

                [collapsed reference link]: https://compose.google.com

                ### Shortcut Reference Links

                [Shortcut Link]

                [Shortcut Link]: https://developer.android.com

                ## Code

                ```kotlin
                fun greetUser(name: String) {
                    println("Hello, ${'$'}name!")
                }

                greetUser("Compose")
                """.trimIndent(),
            modifier = Modifier.padding(16.dp),
            actionHandler =
                object : ActionHandler {
                    override fun handleUrlClick(
                        url: String,
                        node: ASTNode,
                    ) {
                        println("BasicSyntax: Clicked link: $url")
                    }
                },
        )
        }
    }
}
