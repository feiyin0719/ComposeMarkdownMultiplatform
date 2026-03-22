package com.iffly.compose.markdown.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iffly.compose.markdown.multiplatform.ActionHandler
import com.iffly.compose.markdown.multiplatform.MarkdownView
import com.iffly.compose.markdown.multiplatform.config.IMarkdownRenderPlugin
import com.iffly.compose.markdown.multiplatform.config.MarkdownRenderConfig
import com.iffly.compose.markdown.multiplatform.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.render.MarkdownInlineView
import com.iffly.compose.markdown.multiplatform.render.NodeStringBuilderContext
import com.iffly.compose.markdown.multiplatform.render.RenderRegistry
import com.iffly.compose.markdown.multiplatform.style.MarkdownTheme
import com.iffly.compose.markdown.multiplatform.widget.richtext.RichTextInlineContent
import org.commonmark.node.Code
import org.commonmark.node.Node
import kotlin.reflect.KClass

@Composable
fun InlineViewExample(
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
                    # Inline View Example

                    ## FixedSize Inline Content

                    Status indicators with fixed dimensions:

                    Server is `status:online` and running normally. more `status: offline` with more text in same line.

                    Database is `status:offline` due to maintenance.

                    Cache is `status:warning` and needs attention.

                    ## DynamicSize Inline Content

                    Tags that adapt to their content size:

                    This feature is tagged as `tag:Kotlin` and `tag:Compose Multiplatform`.

                    Priority: `tag:High Priority` | Status: `tag:In Progress`
                    """.trimIndent(),
                markdownRenderConfig =
                    MarkdownRenderConfig
                        .Builder()
                        .addPlugin(InlineViewPlugin())
                        .build(),
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

/**
 * Plugin that overrides CODE_SPAN rendering to demonstrate inline views.
 *
 * Recognizes special patterns in inline code:
 * - `status:xxx` → FixedSize colored dot (adjustSizeByContent = false)
 * - `tag:xxx` → DynamicSize badge (adjustSizeByContent = true)
 * - Other code → default inline code rendering
 */
private class InlineViewPlugin : IMarkdownRenderPlugin {
    override fun inlineNodeStringBuilders(): Map<KClass<out Node>, IInlineNodeStringBuilder<*>> =
        mapOf(Code::class to InlineViewCodeNodeStringBuilder())
}

private class InlineViewCodeNodeStringBuilder : IInlineNodeStringBuilder<Code> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: Code,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    ) {
        val codeText = node.literal

        when {
            codeText.startsWith("status:") -> {
                val status = codeText.removePrefix("status:")
                appendFixedSizeStatusDot(status, inlineContentMap)
            }

            codeText.startsWith("tag:") -> {
                val tag = codeText.removePrefix("tag:")
                appendDynamicSizeTag(tag, inlineContentMap)
            }

            else -> {
                // Default inline code rendering
                withStyle(markdownTheme.code.toSpanStyle()) {
                    append(" $codeText ")
                }
            }
        }
    }

    /**
     * FixedSize inline view: a colored status dot with predetermined dimensions.
     * The placeholder size is set explicitly and the content won't be measured.
     */
    private fun AnnotatedString.Builder.appendFixedSizeStatusDot(
        status: String,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
    ) {
        val id = "inline_status_$status"
        val color =
            when (status) {
                "online" -> Color(0xFF4CAF50)
                "offline" -> Color(0xFFF44336)
                "warning" -> Color(0xFFFF9800)
                else -> Color.Gray
            }

        inlineContentMap[id] =
            MarkdownInlineView.MarkdownRichTextInlineContent(
                RichTextInlineContent.EmbeddedRichTextInlineContent(
                    placeholder =
                        Placeholder(
                            width = 32.sp,
                            height = 32.sp,
                            placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
                        ),
                    adjustSizeByContent = false,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color),
                    )
                },
            )
        appendInlineContent(id, "[$status]")
    }

    /**
     * DynamicSize inline view: a tag badge whose placeholder size is determined
     * by measuring the actual content via SubcomposeLayout.
     */
    private fun AnnotatedString.Builder.appendDynamicSizeTag(
        tag: String,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
    ) {
        val id = "inline_tag_$tag"

        inlineContentMap[id] =
            MarkdownInlineView.MarkdownRichTextInlineContent(
                RichTextInlineContent.EmbeddedRichTextInlineContent(
                    placeholder =
                        Placeholder(
                            width = 1.sp,
                            height = 1.sp,
                            placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
                        ),
                    adjustSizeByContent = true,
                ) {
                    Text(
                        text = tag,
                        modifier =
                            Modifier
                                .background(
                                    color = Color(0xFF6200EE).copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(4.dp),
                                ).padding(horizontal = 16.dp, vertical = 32.dp),
                        color = Color(0xFF6200EE),
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
            )
        appendInlineContent(id, "[$tag]")
    }
}
