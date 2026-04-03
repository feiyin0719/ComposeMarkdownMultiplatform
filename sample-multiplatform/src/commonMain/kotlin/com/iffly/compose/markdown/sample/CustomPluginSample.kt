package com.iffly.compose.markdown.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iffly.compose.markdown.multiplatform.MarkdownView
import com.iffly.compose.markdown.multiplatform.config.AbstractMarkdownRenderPlugin
import com.iffly.compose.markdown.multiplatform.config.MarkdownRenderConfig
import com.iffly.compose.markdown.multiplatform.render.CompositeChildNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.render.IBlockRenderer
import com.iffly.compose.markdown.multiplatform.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.render.MarkdownInlineView
import com.iffly.compose.markdown.multiplatform.render.MarkdownInlineText
import com.iffly.compose.markdown.multiplatform.render.NodeStringBuilderContext
import com.iffly.compose.markdown.multiplatform.render.RenderRegistry
import com.iffly.compose.markdown.multiplatform.style.MarkdownTheme
import org.commonmark.Extension
import org.commonmark.node.CustomBlock
import org.commonmark.node.CustomNode
import org.commonmark.node.Node
import org.commonmark.parser.InlineParser
import org.commonmark.parser.Parser
import org.commonmark.parser.SourceLine
import org.commonmark.parser.SourceLines
import org.commonmark.parser.beta.InlineContentParser
import org.commonmark.parser.beta.InlineContentParserFactory
import org.commonmark.parser.beta.InlineParserState
import org.commonmark.parser.beta.ParsedInline
import org.commonmark.parser.block.AbstractBlockParser
import org.commonmark.parser.block.AbstractBlockParserFactory
import org.commonmark.parser.block.BlockContinue
import org.commonmark.parser.block.BlockStart
import org.commonmark.parser.block.MatchedBlockParser
import org.commonmark.parser.block.ParserState
import kotlin.reflect.KClass

// region AST Nodes

class AlertBlock : CustomBlock() {
    var alertType: String = TYPE_INFO
    var title: String? = null

    companion object {
        const val TYPE_INFO = "info"
        const val TYPE_WARNING = "warning"
        const val TYPE_SUCCESS = "success"
        const val TYPE_ERROR = "error"
    }
}

class MentionNode(
    var username: String,
) : CustomNode()

class HashtagNode(
    var hashtag: String,
) : CustomNode()

class HighlightNode(
    var highlightText: String,
) : CustomNode()

class BadgeNode(
    var badgeType: String,
    var badgeText: String,
) : CustomNode()

// endregion

// region Block Parser

class AlertBlockParser(
    private val alertBlock: AlertBlock,
) : AbstractBlockParser() {
    private var finished = false
    private val contentLines = mutableListOf<SourceLine>()

    override val block: AlertBlock = alertBlock

    override fun tryContinue(parserState: ParserState): BlockContinue? {
        if (finished) return BlockContinue.none()
        val line = parserState.line.content
        val nextNonSpace = parserState.nextNonSpaceIndex
        if (nextNonSpace + 3 <= line.length &&
            line.subSequence(nextNonSpace, nextNonSpace + 3).toString() == ":::"
        ) {
            finished = true
            return BlockContinue.finished()
        }
        return BlockContinue.atIndex(parserState.index)
    }

    override fun addLine(line: SourceLine) {
        if (!finished) {
            contentLines.add(line)
        }
    }

    override fun parseInlines(inlineParser: InlineParser) {
        if (contentLines.isEmpty()) return
        val firstLineText = contentLines[0].content.toString().trim()
        alertBlock.title = firstLineText

        if (contentLines.size > 1) {
            val remaining = SourceLines.of(contentLines.drop(1))
            inlineParser.parse(remaining, alertBlock)
        }
    }

    class Factory : AbstractBlockParserFactory() {
        override fun tryStart(
            state: ParserState,
            matchedBlockParser: MatchedBlockParser,
        ): BlockStart? {
            if (state.indent >= 4) return BlockStart.none()
            val nextNonSpace = state.nextNonSpaceIndex
            val line = state.line.content
            if (nextNonSpace + 3 > line.length) return BlockStart.none()
            val marker = line.subSequence(nextNonSpace, nextNonSpace + 3).toString()
            if (marker != ":::") return BlockStart.none()
            val rest = line.subSequence(nextNonSpace + 3, line.length).toString().trim()
            val alertType =
                when {
                    rest.startsWith("info") -> AlertBlock.TYPE_INFO
                    rest.startsWith("warning") -> AlertBlock.TYPE_WARNING
                    rest.startsWith("success") -> AlertBlock.TYPE_SUCCESS
                    rest.startsWith("error") -> AlertBlock.TYPE_ERROR
                    else -> AlertBlock.TYPE_INFO
                }
            val alertBlock = AlertBlock().apply { this.alertType = alertType }
            return BlockStart.of(AlertBlockParser(alertBlock)).atIndex(line.length)
        }
    }
}

// endregion

// region Inline Parsers

class MentionInlineParser : InlineContentParser {
    override fun tryParse(inlineParserState: InlineParserState): ParsedInline? {
        val scanner = inlineParserState.scanner()
        // skip '@'
        scanner.next()
        val start = scanner.position()
        var count = 0
        while (scanner.peek() != '\u0000') {
            val c = scanner.peek()
            if (c.isLetterOrDigit() || c == '_' || c == '-') {
                count++
                scanner.next()
            } else {
                break
            }
        }
        if (count < 2 || count > 20) return ParsedInline.none()
        val username = scanner.getSource(start, scanner.position()).getContent()
        return ParsedInline.of(MentionNode(username), scanner.position())
    }

    class Factory : InlineContentParserFactory {
        override val triggerCharacters: Set<Char> = setOf('@')

        override fun create(): InlineContentParser = MentionInlineParser()
    }
}

class HashtagInlineParser : InlineContentParser {
    override fun tryParse(inlineParserState: InlineParserState): ParsedInline? {
        val scanner = inlineParserState.scanner()
        // skip '#'
        scanner.next()
        val start = scanner.position()
        var count = 0
        while (scanner.peek() != '\u0000') {
            val c = scanner.peek()
            if (c.isLetterOrDigit() || c == '_' || isCjk(c)) {
                count++
                scanner.next()
            } else {
                break
            }
        }
        if (count < 1 || count > 30) return ParsedInline.none()
        val hashtag = scanner.getSource(start, scanner.position()).getContent()
        return ParsedInline.of(HashtagNode(hashtag), scanner.position())
    }

    class Factory : InlineContentParserFactory {
        override val triggerCharacters: Set<Char> = setOf('#')

        override fun create(): InlineContentParser = HashtagInlineParser()
    }
}

class HighlightInlineParser : InlineContentParser {
    override fun tryParse(inlineParserState: InlineParserState): ParsedInline? {
        val scanner = inlineParserState.scanner()
        // skip first '='
        scanner.next()
        // expect second '='
        if (!scanner.next('=')) return ParsedInline.none()
        val start = scanner.position()
        // find closing '=='
        while (true) {
            val found = scanner.find('=')
            if (found == -1) return ParsedInline.none()
            val contentEnd = scanner.position()
            scanner.next()
            if (scanner.next('=')) {
                val content = scanner.getSource(start, contentEnd).getContent()
                if (content.isEmpty()) return ParsedInline.none()
                return ParsedInline.of(HighlightNode(content), scanner.position())
            }
        }
    }

    class Factory : InlineContentParserFactory {
        override val triggerCharacters: Set<Char> = setOf('=')

        override fun create(): InlineContentParser = HighlightInlineParser()
    }
}

class BadgeInlineParser : InlineContentParser {
    override fun tryParse(inlineParserState: InlineParserState): ParsedInline? {
        val scanner = inlineParserState.scanner()
        // skip first '!'
        scanner.next()
        // expect second '!'
        if (!scanner.next('!')) return ParsedInline.none()
        val start = scanner.position()
        // find closing '!!'
        while (true) {
            val found = scanner.find('!')
            if (found == -1) return ParsedInline.none()
            val contentEnd = scanner.position()
            scanner.next()
            if (scanner.next('!')) {
                val raw = scanner.getSource(start, contentEnd).getContent()
                if (raw.isEmpty()) return ParsedInline.none()
                val parts = raw.split(":", limit = 2)
                val (type, text) = if (parts.size == 2) parts[0] to parts[1] else "default" to parts[0]
                return ParsedInline.of(BadgeNode(type, text), scanner.position())
            }
        }
    }

    class Factory : InlineContentParserFactory {
        override val triggerCharacters: Set<Char> = setOf('!')

        override fun create(): InlineContentParser = BadgeInlineParser()
    }
}

private fun isCjk(char: Char): Boolean = char.code in 0x4e00..0x9fff

// endregion

// region Parser Extension

class CustomPluginExtension : Parser.ParserExtension {
    override fun extend(parserBuilder: Parser.Builder) {
        parserBuilder.customBlockParserFactory(AlertBlockParser.Factory())
        parserBuilder.customInlineContentParserFactory(MentionInlineParser.Factory())
        parserBuilder.customInlineContentParserFactory(HashtagInlineParser.Factory())
        parserBuilder.customInlineContentParserFactory(HighlightInlineParser.Factory())
        parserBuilder.customInlineContentParserFactory(BadgeInlineParser.Factory())
    }
}

// endregion

// region Renderers & String Builders

class AlertBlockRenderer : IBlockRenderer<AlertBlock> {
    @Composable
    override fun Invoke(
        node: AlertBlock,
        modifier: Modifier,
    ) {
        val (indicator, containerColor, contentColor) =
            when (node.alertType) {
                AlertBlock.TYPE_INFO -> Triple("ℹ️", Color(0xFFE3F2FD), Color(0xFF1976D2))
                AlertBlock.TYPE_WARNING -> Triple("⚠️", Color(0xFFFFF8E1), Color(0xFFF57C00))
                AlertBlock.TYPE_SUCCESS -> Triple("✅", Color(0xFFE8F5E8), Color(0xFF2E7D32))
                AlertBlock.TYPE_ERROR -> Triple("❌", Color(0xFFFFEBEE), Color(0xFFD32F2F))
                else -> Triple("ℹ️", Color(0xFFE3F2FD), Color(0xFF1976D2))
            }
        Card(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            shape = RoundedCornerShape(8.dp),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = indicator,
                    fontSize = 20.sp,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    val nodeTitle = node.title
                    if (!nodeTitle.isNullOrBlank()) {
                        Text(
                            text = nodeTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = contentColor,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    MarkdownInlineText(node)
                }
            }
        }
    }
}

class MentionNodeStringBuilder : IInlineNodeStringBuilder<MentionNode> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: MentionNode,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: com.iffly.compose.markdown.multiplatform.ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    ) {
        withStyle(
            SpanStyle(
                color = Color(0xFF1976D2),
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.Medium,
            ),
        ) {
            append("@${node.username}")
        }
    }
}

class HashtagNodeStringBuilder : IInlineNodeStringBuilder<HashtagNode> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: HashtagNode,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: com.iffly.compose.markdown.multiplatform.ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    ) {
        withStyle(SpanStyle(color = Color(0xFF2E7D32), fontWeight = FontWeight.Medium)) {
            append("#${node.hashtag}")
        }
    }
}

class HighlightNodeStringBuilder : IInlineNodeStringBuilder<HighlightNode> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: HighlightNode,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: com.iffly.compose.markdown.multiplatform.ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    ) {
        withStyle(
            SpanStyle(
                background = Color(0xFFFFEB3B),
                color = Color(0xFF212121),
                fontWeight = FontWeight.Medium,
            ),
        ) {
            append(node.highlightText)
        }
    }
}

class BadgeNodeStringBuilder : IInlineNodeStringBuilder<BadgeNode> {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: BadgeNode,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: com.iffly.compose.markdown.multiplatform.ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    ) {
        val (bg, fg) =
            when (node.badgeType.lowercase()) {
                "primary" -> Color(0xFF1976D2) to Color.White
                "success" -> Color(0xFF2E7D32) to Color.White
                "warning" -> Color(0xFFF57C00) to Color.White
                "error", "danger" -> Color(0xFFD32F2F) to Color.White
                "info" -> Color(0xFF0288D1) to Color.White
                else -> Color(0xFF616161) to Color.White
            }
        withStyle(
            SpanStyle(
                background = bg,
                color = fg,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Default,
            ),
        ) {
            append(" ${node.badgeText} ")
        }
    }
}

// endregion

// region Plugin

class CustomMarkdownPlugin : AbstractMarkdownRenderPlugin() {
    override fun parserExtensions(): List<Extension> = listOf(CustomPluginExtension())

    override fun blockRenderers(): Map<KClass<out Node>, IBlockRenderer<*>> = mapOf(AlertBlock::class to AlertBlockRenderer())

    override fun inlineNodeStringBuilders(): Map<KClass<out Node>, IInlineNodeStringBuilder<*>> =
        mapOf(
            AlertBlock::class to CompositeChildNodeStringBuilder(),
            MentionNode::class to MentionNodeStringBuilder(),
            HashtagNode::class to HashtagNodeStringBuilder(),
            HighlightNode::class to HighlightNodeStringBuilder(),
            BadgeNode::class to BadgeNodeStringBuilder(),
        )
}

// endregion

// region Sample Composable

@Composable
fun CustomPluginExample(
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
                        .addPlugin(CustomMarkdownPlugin())
                        .build()
                }
            MarkdownView(
                text =
                    """
                    # Custom Plugin Example

                    This example demonstrates various custom Markdown plugin features.

                    ## 1. Alert Blocks

                    ### Information Notice
                    :::info
                    Important Information
                    This is an info-type alert block for displaying general information.
                    :::

                    ### Warning Notice
                    :::warning
                    Notice
                    This is a warning-type alert block to remind users of certain matters.
                    :::

                    ### Success Notice
                    :::success
                    Operation Successful
                    This is a success-type alert block for displaying successful operation messages.
                    :::

                    ### Error Notice
                    :::error
                    Operation Failed
                    This is an error-type alert block for displaying error messages.
                    :::

                    ## 2. User Mentions

                    Welcome @developer and @designer to join our team!

                    Thanks to @admin for the help, and @user123 for the feedback.

                    ## 3. Hashtags

                    This project uses #Android #Compose #Kotlin technology stack.

                    Related topics: #MobileDevelopment #UIFramework #OpenSource

                    ## 4. Highlight Text

                    This text contains ==important highlighted content== that needs special attention.

                    Please note that ==this feature== has been improved in the new version.

                    ## 5. Badges

                    Project status: !!success:Completed!! !!primary:v1.0.0!! !!warning:Testing!!

                    Tech stack: !!info:Kotlin!! !!primary:Compose!! !!success:Stable!!

                    Important reminder: !!error:Deprecated!! !!danger:High Risk!! !!warning:Update Required!!

                    ## 6. Mixed Usage Example

                    :::info
                    Project Update Notification

                    Thanks to @team_lead for releasing !!primary:v2.0.0!! version!

                    Main improvements include:
                    - ==Performance optimization== #Performance
                    - ==UI improvements== #Interface
                    - New features !!success:Completed!!

                    Related personnel: @developer @designer @tester
                    Topic tags: #Update #Release #TeamCollaboration
                    :::
                    """.trimIndent(),
                modifier = Modifier.padding(16.dp),
                markdownRenderConfig = markdownRenderConfig,
            )
        }
    }
}

// endregion
