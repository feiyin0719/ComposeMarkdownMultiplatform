package com.iffly.compose.markdown.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iffly.compose.markdown.multiplatform.MarkdownView
import com.iffly.compose.markdown.multiplatform.config.MarkdownRenderConfig
import com.iffly.compose.markdown.multiplatform.html.HtmlMarkdownPlugin

@Composable
fun HtmlBlockExample(
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
            val markdownRenderConfig =
                remember {
                    MarkdownRenderConfig
                        .Builder()
                        .addPlugin(HtmlMarkdownPlugin())
                        .build()
                }
            MarkdownView(
                text =
                    """
                    # HTML Block Rendering

                    HTML blocks are converted to Markdown AST for unified styling.

                    ## Paragraph

                    <p>This is a <b>bold</b> paragraph rendered from an HTML block.</p>

                    ## Div with Nested Content

                    <div>
                      <p>Paragraph inside a div with <i>italic</i> and <b>bold</b> text.</p>
                    </div>

                    ## Unordered List

                    <ul>
                      <li>HTML List Item 1</li>
                      <li>HTML List Item 2</li>
                      <li>HTML List Item 3</li>
                    </ul>

                    ## Ordered List

                    <ol>
                      <li>First item</li>
                      <li>Second item</li>
                      <li>Third item</li>
                    </ol>

                    ## Blockquote

                    <blockquote>
                      <p>This is a blockquote rendered from an HTML block.</p>
                    </blockquote>

                    ## Headings

                    <h2>HTML Heading 2</h2>

                    <h3>HTML Heading 3</h3>

                    ## Mixed Markdown and HTML Blocks

                    Regular **Markdown** content before and after HTML blocks.

                    <p>An HTML paragraph in between Markdown content.</p>

                    Back to regular *Markdown* again.
                    """.trimIndent(),
                modifier = Modifier.padding(16.dp),
                markdownRenderConfig = markdownRenderConfig,
            )
        }
    }
}
