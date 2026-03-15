package com.iffly.compose.markdown.multiplatform.html.handlers

import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration
import com.iffly.compose.markdown.multiplatform.MarkdownLinkInteractionListener
import com.iffly.compose.markdown.multiplatform.html.HtmlInlineTagContext
import com.iffly.compose.markdown.multiplatform.html.HtmlInlineTagHandler

internal class BoldTagHandler : HtmlInlineTagHandler {
    override val tagNames = setOf("b", "strong")

    override fun onOpenTag(
        tagName: String,
        rawTag: String,
        builder: androidx.compose.ui.text.AnnotatedString.Builder,
        context: HtmlInlineTagContext,
    ) {
        builder.pushStyle(context.markdownTheme.strongEmphasis)
    }
}

internal class ItalicTagHandler : HtmlInlineTagHandler {
    override val tagNames = setOf("i", "em")

    override fun onOpenTag(
        tagName: String,
        rawTag: String,
        builder: androidx.compose.ui.text.AnnotatedString.Builder,
        context: HtmlInlineTagContext,
    ) {
        builder.pushStyle(context.markdownTheme.emphasis)
    }
}

internal class UnderlineTagHandler : HtmlInlineTagHandler {
    override val tagNames = setOf("u", "ins")

    override fun onOpenTag(
        tagName: String,
        rawTag: String,
        builder: androidx.compose.ui.text.AnnotatedString.Builder,
        context: HtmlInlineTagContext,
    ) {
        builder.pushStyle(SpanStyle(textDecoration = TextDecoration.Underline))
    }
}

internal class StrikethroughTagHandler : HtmlInlineTagHandler {
    override val tagNames = setOf("del", "s", "strike")

    override fun onOpenTag(
        tagName: String,
        rawTag: String,
        builder: androidx.compose.ui.text.AnnotatedString.Builder,
        context: HtmlInlineTagContext,
    ) {
        builder.pushStyle(context.markdownTheme.strikethrough)
    }
}

internal class LinkTagHandler : HtmlInlineTagHandler {
    companion object {
        private val HREF_REGEX = Regex("""href\s*=\s*["']([^"']*)["']""", RegexOption.IGNORE_CASE)
    }

    override val tagNames = setOf("a")

    override fun onOpenTag(
        tagName: String,
        rawTag: String,
        builder: androidx.compose.ui.text.AnnotatedString.Builder,
        context: HtmlInlineTagContext,
    ) {
        val url = HREF_REGEX.find(rawTag)?.groupValues?.get(1) ?: ""
        val linkInteractionListener =
            context.actionHandler?.let {
                MarkdownLinkInteractionListener(actionHandler = it, node = context.node)
            }
        val linkAnnotation =
            LinkAnnotation.Url(
                url = url,
                styles = context.markdownTheme.link,
                linkInteractionListener = linkInteractionListener,
            )
        builder.pushLink(linkAnnotation)
    }
}

internal class SpanTagHandler : HtmlInlineTagHandler {
    override val tagNames = setOf("span")

    override fun onOpenTag(
        tagName: String,
        rawTag: String,
        builder: androidx.compose.ui.text.AnnotatedString.Builder,
        context: HtmlInlineTagContext,
    ) {
        val style = CssStyleParser.parseInlineCssStyle(rawTag) ?: SpanStyle()
        builder.pushStyle(style)
    }
}

fun defaultHtmlInlineTagHandlers(): List<HtmlInlineTagHandler> =
    listOf(
        BoldTagHandler(),
        ItalicTagHandler(),
        UnderlineTagHandler(),
        StrikethroughTagHandler(),
        LinkTagHandler(),
        SpanTagHandler(),
    )
