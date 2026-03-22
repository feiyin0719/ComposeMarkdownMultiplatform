package com.iffly.compose.markdown.multiplatform.html

import androidx.compose.ui.text.AnnotatedString
import com.iffly.compose.markdown.multiplatform.ActionHandler
import com.iffly.compose.markdown.multiplatform.render.MarkdownInlineView
import com.iffly.compose.markdown.multiplatform.render.NodeStringBuilderContext
import com.iffly.compose.markdown.multiplatform.render.RenderRegistry
import com.iffly.compose.markdown.multiplatform.style.MarkdownTheme
import org.commonmark.node.Node

/**
 * Context provided to [HtmlInlineTagHandler] callbacks, containing the current node,
 * inline content map, theme, and other rendering state.
 *
 * @param node The node representing the HTML tag.
 * @param inlineContentMap Mutable map of inline content keyed by unique identifiers.
 * @param markdownTheme The current markdown theme providing text styles.
 * @param actionHandler Optional handler for user interactions such as link clicks.
 * @param indentLevel Current indentation level in the markdown structure.
 * @param isShowNotSupported Whether unsupported elements should be displayed.
 * @param renderRegistry Registry of available renderers.
 * @param nodeStringBuilderContext Context for building node strings.
 */
data class HtmlInlineTagContext(
    val node: Node,
    val inlineContentMap: MutableMap<String, MarkdownInlineView>,
    val markdownTheme: MarkdownTheme,
    val actionHandler: ActionHandler?,
    val indentLevel: Int,
    val isShowNotSupported: Boolean,
    val renderRegistry: RenderRegistry,
    val nodeStringBuilderContext: NodeStringBuilderContext,
)

/**
 * Interface for handling inline HTML tags within markdown content.
 *
 * Implementations define which tag names they handle via [tagNames] and provide
 * behavior for opening and closing tags. The default [onCloseTag] pops the most
 * recent style from the [AnnotatedString.Builder].
 *
 * @see HtmlInlineTagContext
 * @see HtmlMarkdownPlugin
 */
interface HtmlInlineTagHandler {
    /** The set of HTML tag names (lowercase) this handler supports. */
    val tagNames: Set<String>

    /**
     * Called when an opening HTML tag is encountered.
     *
     * @param tagName The lowercase tag name (e.g., "b", "span").
     * @param rawTag The full raw HTML tag string including attributes.
     * @param builder The [AnnotatedString.Builder] to push styles onto.
     * @param context The current rendering context.
     */
    fun onOpenTag(
        tagName: String,
        rawTag: String,
        builder: AnnotatedString.Builder,
        context: HtmlInlineTagContext,
    )

    /**
     * Called when a closing HTML tag is encountered. Default implementation pops
     * the most recent style from the builder.
     *
     * @param tagName The lowercase tag name.
     * @param builder The [AnnotatedString.Builder] to pop styles from.
     * @param context The current rendering context.
     */
    fun onCloseTag(
        tagName: String,
        builder: AnnotatedString.Builder,
        context: HtmlInlineTagContext,
    ) {
        builder.pop()
    }
}
