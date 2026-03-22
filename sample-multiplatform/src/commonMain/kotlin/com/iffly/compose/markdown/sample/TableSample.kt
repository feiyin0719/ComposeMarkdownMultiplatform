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
import com.iffly.compose.markdown.multiplatform.table.TableMarkdownPlugin

@Composable
fun TableExample(
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
            val markdownRenderConfig = remember {
                MarkdownRenderConfig.Builder()
                    .addPlugin(TableMarkdownPlugin())
                    .build()
            }
            MarkdownView(
                text =
                    """
                    # GFM Table Example

                    ## Basic Table

                    | Header 1 | Header 2 | Header 3 |
                    |----------|----------|----------|
                    | Cell 1   | Cell 2   | Cell 3   |
                    | Cell 4   | Cell 5   | Cell 6   |

                    ## Aligned Table

                    | Left | Center | Right |
                    |:-----|:------:|------:|
                    | L1   | C1     | R1    |
                    | L2   | C2     | R2    |

                    ## Table with Inline Formatting

                    | Feature | Status | Notes |
                    |---------|--------|-------|
                    | **Bold** | `done` | Works *great* |
                    | ~~Removed~~ | `pending` | Simple text |
                    | Normal | `active` | [link](https://example.com) |

                    ## Wide Table

                    | Col 1 | Col 2 | Col 3 | Col 4 | Col 5 | Col 6 | Col 7 | Col 8 |
                    |-------|-------|-------|-------|-------|-------|-------|-------|
                    | A | B | C | D | E | F | G | H |
                    | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 |

                    ## Two Column Table

                    | Key | Value |
                    |-----|-------|
                    | Name | Compose Multiplatform |
                    | Version | 1.8.0 |
                    """.trimIndent(),
                modifier = Modifier.padding(16.dp),
                markdownRenderConfig = markdownRenderConfig,
            )
        }
    }
}
