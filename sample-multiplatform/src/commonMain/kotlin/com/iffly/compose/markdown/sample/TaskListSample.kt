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
import com.iffly.compose.markdown.multiplatform.task.TaskMarkdownPlugin

@Composable
fun TaskListExample(
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
                        .addPlugin(TaskMarkdownPlugin())
                        .build()
                }
            MarkdownView(
                text =
                    """
                    # Task List Example

                    ## Shopping List

                    - [x] Apples
                    - [x] Bananas
                    - [ ] Oranges
                    - [ ] Grapes

                    ## Project Tasks

                    - [x] Set up project structure
                    - [x] Implement core features
                    - [ ] Write unit tests
                    - [ ] Update documentation

                    ## Mixed List

                    - Regular item
                    - [x] Completed task
                    - Another regular item
                    - [ ] Pending task

                    ## Nested Tasks

                    - [x] Parent task
                        - [x] Sub-task 1
                        - [ ] Sub-task 2
                    - [ ] Another parent
                        - [ ] Sub-task A
                    """.trimIndent(),
                modifier = Modifier.padding(16.dp),
                markdownRenderConfig = markdownRenderConfig,
            )
        }
    }
}
