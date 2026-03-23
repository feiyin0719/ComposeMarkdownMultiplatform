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
import com.iffly.compose.markdown.multiplatform.autolink.AutolinkMarkdownPlugin
import com.iffly.compose.markdown.multiplatform.config.MarkdownRenderConfig

@Composable
fun AutolinkExample(
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
                        .addPlugin(AutolinkMarkdownPlugin())
                        .build()
                }
            MarkdownView(
                text =
                    """
                    # Autolink Example

                    ## URL Autolinks

                    Visit https://github.com for code hosting.

                    Check out https://kotlinlang.org/docs/multiplatform.html for KMP docs.

                    ## WWW Autolinks

                    Visit www.example.com for more info.

                    ## Email Autolinks

                    Contact us at support@example.com for help.

                    Send feedback to feedback@example.org.

                    ## Mixed Content

                    You can mix autolinks with **bold** and *italic* text.
                    Visit https://compose.multiplatform.org and email hello@example.com.
                    """.trimIndent(),
                modifier = Modifier.padding(16.dp),
                markdownRenderConfig = markdownRenderConfig,
            )
        }
    }
}
