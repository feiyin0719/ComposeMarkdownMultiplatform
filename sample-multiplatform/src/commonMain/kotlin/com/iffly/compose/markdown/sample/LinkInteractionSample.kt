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
fun LinkInteractionExample(
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
                    # Link Interaction Example

                    This example demonstrates different types of link handling:

                    ## External Links
                    [GitHub](https://github.com) - Will open in browser

                    [Google](https://google.com) - Another external link

                    ## Internal Links
                    [Internal Page A](/internal/page-a) - In-app navigation

                    [Internal Page B](/internal/page-b) - Another internal page

                    ## Special Links
                    [Email Contact](mailto:example@example.com) - Email link

                    [Phone Contact](tel:+1234567890) - Phone link

                    ## Custom Protocol
                    [Open App](myapp://custom/action) - Custom protocol

                    Different links will be handled in different ways!
                    """.trimIndent(),
                modifier = Modifier.padding(16.dp),
                actionHandler =
                    object : ActionHandler {
                        override fun handleUrlClick(
                            url: String,
                            node: ASTNode,
                        ) {
                            when {
                                url.startsWith("http") -> {
                                    println("LinkInteraction: Open external link: $url")
                                }

                                url.startsWith("/internal") -> {
                                    println("LinkInteraction: Navigate to internal page: $url")
                                }

                                url.startsWith("mailto:") -> {
                                    println("LinkInteraction: Open email: $url")
                                }

                                url.startsWith("tel:") -> {
                                    println("LinkInteraction: Dial phone: $url")
                                }

                                else -> {
                                    println("LinkInteraction: Other link: $url")
                                }
                            }
                        }
                    },
            )
        }
    }
}
