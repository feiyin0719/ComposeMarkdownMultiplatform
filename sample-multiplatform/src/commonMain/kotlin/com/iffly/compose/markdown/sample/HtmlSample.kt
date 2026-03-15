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
fun HtmlExample(
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val htmlConfig =
        remember {
            MarkdownRenderConfig
                .Builder()
                .addPlugin(HtmlMarkdownPlugin())
                .build()
        }

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
                    # HTML Inline Example

                    ## Basic Inline Styles

                    Text with <b>bold</b> and <i>italic</i> and <u>underline</u> and <del>strikethrough</del> formatting.

                    Nested: <b><i>bold italic</i></b> and <b><u>bold underline</u></b>.

                    Alternative tags: <strong>strong</strong> and <em>emphasis</em> and <s>strike</s>.

                    ## Line Breaks

                    Line one<br>Line two<br/>Line three<br />Line four.

                    ## Links

                    Visit <a href="https://github.com">GitHub</a> or <a href="https://google.com">Google</a> for more info.

                    Mixed: **Markdown bold** with <a href="https://example.com">HTML link</a> inside.

                    ## Span with Inline CSS

                    <span style="color: red">Red text</span> and <span style="color: blue">blue text</span> and <span style="color: green">green text</span>.

                    <span style="color: #FF6600">Orange hex color</span> and <span style="color: rgb(128, 0, 255)">Purple RGB color</span>.

                    <span style="background-color: yellow; color: black">Highlighted text</span> with background.

                    <span style="font-weight: bold; color: darkred">Bold dark red</span> and <span style="font-style: italic; color: darkblue">Italic dark blue</span>.

                    <span style="font-size: 24px; color: purple">Large purple text</span> and <span style="font-size: 12px">small text</span>.

                    <span style="text-decoration: underline; color: #1976D2">Underlined styled text</span>.

                    ## Mixed Markdown and HTML Inline

                    **Bold markdown** with <span style="color: red">red span</span> and *italic markdown* together.

                    A paragraph with <b>HTML bold</b>, **Markdown bold**, <i>HTML italic</i>, and *Markdown italic* all mixed.

                    ## Back to Markdown

                    This is regular **Markdown** content after the HTML sections.

                    - Markdown list item 1
                    - Markdown list item 2
                    """.trimIndent(),
                markdownRenderConfig = htmlConfig,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
