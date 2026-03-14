package com.iffly.compose.markdown.multiplatform.core.plugins

import com.iffly.compose.markdown.multiplatform.config.IMarkdownRenderPlugin
import com.iffly.compose.markdown.multiplatform.core.renders.BlockQuoteRenderer
import com.iffly.compose.markdown.multiplatform.core.renders.BreakLineRenderer
import com.iffly.compose.markdown.multiplatform.core.renders.CodeBlockRenderer
import com.iffly.compose.markdown.multiplatform.core.renders.CodeNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.DocumentRenderer
import com.iffly.compose.markdown.multiplatform.core.renders.EmphasisNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.EmptyNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.EmptyRenderer
import com.iffly.compose.markdown.multiplatform.core.renders.HardLineBreakNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.HeadingNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.HtmlInlineNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.LinkDefinitionNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.LinkNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.ListBlockRenderer
import com.iffly.compose.markdown.multiplatform.core.renders.ListItemRenderer
import com.iffly.compose.markdown.multiplatform.core.renders.ParagraphNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.ShortReferenceLinkNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.StrikethroughNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.StrongEmphasisNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.TextBlockRenderer
import com.iffly.compose.markdown.multiplatform.core.renders.TextNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.render.IBlockRenderer
import com.iffly.compose.markdown.multiplatform.render.IInlineNodeStringBuilder
import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.flavours.gfm.GFMElementTypes

class CorePlugin : IMarkdownRenderPlugin {
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
        blockRenderers[MarkdownTokenTypes.EOL] = EmptyRenderer()
        blockRenderers[MarkdownTokenTypes.WHITE_SPACE] = EmptyRenderer()
        return blockRenderers
    }

    override fun inlineNodeStringBuilders(): Map<IElementType, IInlineNodeStringBuilder> {
        val builders = mutableMapOf<IElementType, IInlineNodeStringBuilder>()
        builders[MarkdownTokenTypes.TEXT] = TextNodeStringBuilder()
        builders[MarkdownElementTypes.PARAGRAPH] = ParagraphNodeStringBuilder()
        builders[MarkdownElementTypes.CODE_SPAN] = CodeNodeStringBuilder()
        builders[MarkdownElementTypes.STRONG] = StrongEmphasisNodeStringBuilder()
        builders[MarkdownElementTypes.EMPH] = EmphasisNodeStringBuilder()
        builders[GFMElementTypes.STRIKETHROUGH] = StrikethroughNodeStringBuilder()
        builders[MarkdownTokenTypes.EOL] = EmptyNodeStringBuilder()
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
