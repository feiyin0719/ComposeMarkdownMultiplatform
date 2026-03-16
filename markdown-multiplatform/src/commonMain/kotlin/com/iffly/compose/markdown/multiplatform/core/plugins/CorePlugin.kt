package com.iffly.compose.markdown.multiplatform.core.plugins

import com.iffly.compose.markdown.multiplatform.config.IMarkdownRenderPlugin
import com.iffly.compose.markdown.multiplatform.core.renders.BacktickNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.BlockQuoteRenderer
import com.iffly.compose.markdown.multiplatform.core.renders.BreakLineRenderer
import com.iffly.compose.markdown.multiplatform.core.renders.CodeBlockRenderer
import com.iffly.compose.markdown.multiplatform.core.renders.CodeNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.ColonNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.DocumentRenderer
import com.iffly.compose.markdown.multiplatform.core.renders.DoubleQuoteNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.EmphTokenNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.EmphasisNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.EmptyNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.EolNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.ExclamationMarkNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.GtNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.HardLineBreakNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.HeadingNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.HtmlInlineNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.LBracketNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.LParenNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.LinkDefinitionNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.LinkNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.ListBlockRenderer
import com.iffly.compose.markdown.multiplatform.core.renders.ListItemRenderer
import com.iffly.compose.markdown.multiplatform.core.renders.LtNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.ParagraphNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.RBracketNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.RParenNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.ShortReferenceLinkNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.SingleQuoteNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.StrikethroughNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.StrongEmphasisNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.TextBlockRenderer
import com.iffly.compose.markdown.multiplatform.core.renders.TextNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.WhiteSpaceNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.render.IBlockRenderer
import com.iffly.compose.markdown.multiplatform.render.IInlineNodeStringBuilder
import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.flavours.gfm.GFMElementTypes

/**
 * The core rendering plugin that registers all built-in block renderers and inline
 * node string builders for standard Markdown and GFM elements.
 *
 * This plugin maps each markdown element type (headings, paragraphs, lists, code blocks,
 * blockquotes, links, emphasis, etc.) to its corresponding renderer or string builder
 * implementation.
 *
 * @see IMarkdownRenderPlugin
 */
class CorePlugin : IMarkdownRenderPlugin {
    /** Returns the mapping of markdown element types to their [IBlockRenderer] implementations. */
    override fun blockRenderers(): Map<IElementType, IBlockRenderer> {
        val blockRenderers = mutableMapOf<IElementType, IBlockRenderer>()
        blockRenderers[MarkdownElementTypes.MARKDOWN_FILE] = DocumentRenderer()
        blockRenderers[MarkdownElementTypes.PARAGRAPH] = TextBlockRenderer()
        blockRenderers[MarkdownElementTypes.ATX_1] = TextBlockRenderer()
        blockRenderers[MarkdownElementTypes.ATX_2] = TextBlockRenderer()
        blockRenderers[MarkdownElementTypes.ATX_3] = TextBlockRenderer()
        blockRenderers[MarkdownElementTypes.ATX_4] = TextBlockRenderer()
        blockRenderers[MarkdownElementTypes.ATX_5] = TextBlockRenderer()
        blockRenderers[MarkdownElementTypes.ATX_6] = TextBlockRenderer()
        blockRenderers[MarkdownElementTypes.SETEXT_1] = TextBlockRenderer()
        blockRenderers[MarkdownElementTypes.SETEXT_2] = TextBlockRenderer()
        blockRenderers[MarkdownElementTypes.ORDERED_LIST] = ListBlockRenderer()
        blockRenderers[MarkdownElementTypes.UNORDERED_LIST] = ListBlockRenderer()
        blockRenderers[MarkdownElementTypes.LIST_ITEM] = ListItemRenderer()
        blockRenderers[MarkdownElementTypes.CODE_FENCE] = CodeBlockRenderer()
        blockRenderers[MarkdownElementTypes.CODE_BLOCK] = CodeBlockRenderer()
        blockRenderers[MarkdownTokenTypes.HORIZONTAL_RULE] = BreakLineRenderer()
        blockRenderers[MarkdownElementTypes.BLOCK_QUOTE] = BlockQuoteRenderer()
        return blockRenderers
    }

    /** Returns the mapping of markdown token/element types to their [IInlineNodeStringBuilder] implementations. */
    override fun inlineNodeStringBuilders(): Map<IElementType, IInlineNodeStringBuilder> {
        val builders = mutableMapOf<IElementType, IInlineNodeStringBuilder>()
        builders[MarkdownTokenTypes.TEXT] = TextNodeStringBuilder()
        builders[MarkdownTokenTypes.SINGLE_QUOTE] = SingleQuoteNodeStringBuilder()
        builders[MarkdownTokenTypes.DOUBLE_QUOTE] = DoubleQuoteNodeStringBuilder()
        builders[MarkdownTokenTypes.LPAREN] = LParenNodeStringBuilder()
        builders[MarkdownTokenTypes.RPAREN] = RParenNodeStringBuilder()
        builders[MarkdownTokenTypes.LBRACKET] = LBracketNodeStringBuilder()
        builders[MarkdownTokenTypes.RBRACKET] = RBracketNodeStringBuilder()
        builders[MarkdownTokenTypes.LT] = LtNodeStringBuilder()
        builders[MarkdownTokenTypes.GT] = GtNodeStringBuilder()
        builders[MarkdownTokenTypes.COLON] = ColonNodeStringBuilder()
        builders[MarkdownTokenTypes.EXCLAMATION_MARK] = ExclamationMarkNodeStringBuilder()
        builders[MarkdownTokenTypes.BACKTICK] = BacktickNodeStringBuilder()
        builders[MarkdownTokenTypes.EMPH] = EmphTokenNodeStringBuilder()
        builders[MarkdownTokenTypes.EOL] = EolNodeStringBuilder()
        builders[MarkdownTokenTypes.WHITE_SPACE] = WhiteSpaceNodeStringBuilder()
        builders[MarkdownTokenTypes.BLOCK_QUOTE] = EmptyNodeStringBuilder()
        builders[MarkdownElementTypes.PARAGRAPH] = ParagraphNodeStringBuilder()
        builders[MarkdownElementTypes.CODE_SPAN] = CodeNodeStringBuilder()
        builders[MarkdownElementTypes.STRONG] = StrongEmphasisNodeStringBuilder()
        builders[MarkdownElementTypes.EMPH] = EmphasisNodeStringBuilder()
        builders[GFMElementTypes.STRIKETHROUGH] = StrikethroughNodeStringBuilder()
        builders[MarkdownTokenTypes.HARD_LINE_BREAK] = HardLineBreakNodeStringBuilder()
        builders[MarkdownTokenTypes.HTML_TAG] = HtmlInlineNodeStringBuilder()
        builders[MarkdownElementTypes.INLINE_LINK] = LinkNodeStringBuilder()
        builders[MarkdownElementTypes.SHORT_REFERENCE_LINK] = ShortReferenceLinkNodeStringBuilder()
        builders[MarkdownElementTypes.FULL_REFERENCE_LINK] = ShortReferenceLinkNodeStringBuilder()
        builders[MarkdownElementTypes.LINK_DEFINITION] = LinkDefinitionNodeStringBuilder()
        builders[MarkdownElementTypes.ATX_1] = HeadingNodeStringBuilder()
        builders[MarkdownElementTypes.ATX_2] = HeadingNodeStringBuilder()
        builders[MarkdownElementTypes.ATX_3] = HeadingNodeStringBuilder()
        builders[MarkdownElementTypes.ATX_4] = HeadingNodeStringBuilder()
        builders[MarkdownElementTypes.ATX_5] = HeadingNodeStringBuilder()
        builders[MarkdownElementTypes.ATX_6] = HeadingNodeStringBuilder()
        builders[MarkdownElementTypes.SETEXT_1] = HeadingNodeStringBuilder()
        builders[MarkdownElementTypes.SETEXT_2] = HeadingNodeStringBuilder()
        return builders
    }
}
