package com.iffly.compose.markdown.multiplatform.core.plugins

import com.iffly.compose.markdown.multiplatform.config.IMarkdownRenderPlugin
import com.iffly.compose.markdown.multiplatform.core.renders.BlockQuoteRenderer
import com.iffly.compose.markdown.multiplatform.core.renders.BreakLineRenderer
import com.iffly.compose.markdown.multiplatform.core.renders.CodeBlockRenderer
import com.iffly.compose.markdown.multiplatform.core.renders.CodeNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.DocumentRenderer
import com.iffly.compose.markdown.multiplatform.core.renders.EmphasisNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.HardLineBreakNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.HeadingNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.HtmlInlineNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.ImageNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.LinkNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.ListBlockRenderer
import com.iffly.compose.markdown.multiplatform.core.renders.ListItemRenderer
import com.iffly.compose.markdown.multiplatform.core.renders.ParagraphNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.SoftLineBreakNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.StrongEmphasisNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.core.renders.TextBlockRenderer
import com.iffly.compose.markdown.multiplatform.core.renders.TextNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.render.IBlockRenderer
import com.iffly.compose.markdown.multiplatform.render.IInlineNodeStringBuilder
import org.commonmark.node.BlockQuote
import org.commonmark.node.BulletList
import org.commonmark.node.Code
import org.commonmark.node.Document
import org.commonmark.node.Emphasis
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.HardLineBreak
import org.commonmark.node.Heading
import org.commonmark.node.HtmlInline
import org.commonmark.node.Image
import org.commonmark.node.IndentedCodeBlock
import org.commonmark.node.Link
import org.commonmark.node.ListItem
import org.commonmark.node.Node
import org.commonmark.node.OrderedList
import org.commonmark.node.Paragraph
import org.commonmark.node.SoftLineBreak
import org.commonmark.node.StrongEmphasis
import org.commonmark.node.Text
import org.commonmark.node.ThematicBreak
import kotlin.reflect.KClass

class CorePlugin : IMarkdownRenderPlugin {
    override fun blockRenderers(): Map<KClass<out Node>, IBlockRenderer<*>> =
        mapOf(
            Document::class to DocumentRenderer(),
            Paragraph::class to TextBlockRenderer(),
            Heading::class to TextBlockRenderer(),
            OrderedList::class to ListBlockRenderer(),
            BulletList::class to ListBlockRenderer(),
            ListItem::class to ListItemRenderer(),
            FencedCodeBlock::class to CodeBlockRenderer(),
            IndentedCodeBlock::class to CodeBlockRenderer(),
            ThematicBreak::class to BreakLineRenderer(),
            BlockQuote::class to BlockQuoteRenderer(),
        )

    override fun inlineNodeStringBuilders(): Map<KClass<out Node>, IInlineNodeStringBuilder<*>> =
        mapOf(
            Text::class to TextNodeStringBuilder(),
            Paragraph::class to ParagraphNodeStringBuilder(),
            Code::class to CodeNodeStringBuilder(),
            StrongEmphasis::class to StrongEmphasisNodeStringBuilder(),
            Emphasis::class to EmphasisNodeStringBuilder(),
            HardLineBreak::class to HardLineBreakNodeStringBuilder(),
            SoftLineBreak::class to SoftLineBreakNodeStringBuilder(),
            HtmlInline::class to HtmlInlineNodeStringBuilder(),
            Link::class to LinkNodeStringBuilder(),
            Image::class to ImageNodeStringBuilder(),
            Heading::class to HeadingNodeStringBuilder(),
        )
}
