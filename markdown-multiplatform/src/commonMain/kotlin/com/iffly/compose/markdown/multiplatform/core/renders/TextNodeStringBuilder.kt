package com.iffly.compose.markdown.multiplatform.core.renders

import androidx.compose.ui.text.AnnotatedString
import com.iffly.compose.markdown.multiplatform.ActionHandler
import com.iffly.compose.markdown.multiplatform.render.IInlineNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.render.MarkdownInlineView
import com.iffly.compose.markdown.multiplatform.render.NodeStringBuilderContext
import com.iffly.compose.markdown.multiplatform.render.RenderRegistry
import com.iffly.compose.markdown.multiplatform.style.MarkdownTheme
import com.iffly.compose.markdown.multiplatform.util.EntityConverter
import com.iffly.compose.markdown.multiplatform.util.contentText
import com.iffly.compose.markdown.multiplatform.util.isBlockQuoteContinuationMarker
import com.iffly.compose.markdown.multiplatform.util.previousSibling
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode

/**
 * Inline node string builder for plain text tokens.
 *
 * Appends the node's text content after processing escape sequences via [EntityConverter].
 *
 * @see IInlineNodeStringBuilder
 * @see EntityConverter
 */
class TextNodeStringBuilder : IInlineNodeStringBuilder {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: ASTNode,
        sourceText: String,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    ) {
        append(
            EntityConverter.replaceEntities(
                node.contentText(sourceText),
                processEntities = false,
                processEscapes = true,
            ),
        )
    }
}

/** Inline node string builder that appends a single quote character (`'`). */
class SingleQuoteNodeStringBuilder : IInlineNodeStringBuilder {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: ASTNode,
        sourceText: String,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    ) {
        append('\'')
    }
}

/** Inline node string builder that appends a double quote character (`"`). */
class DoubleQuoteNodeStringBuilder : IInlineNodeStringBuilder {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: ASTNode,
        sourceText: String,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    ) {
        append('\"')
    }
}

/** Inline node string builder that appends a left parenthesis character (`(`). */
class LParenNodeStringBuilder : IInlineNodeStringBuilder {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: ASTNode,
        sourceText: String,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    ) {
        append('(')
    }
}

/** Inline node string builder that appends a right parenthesis character (`)`). */
class RParenNodeStringBuilder : IInlineNodeStringBuilder {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: ASTNode,
        sourceText: String,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    ) {
        append(')')
    }
}

/** Inline node string builder that appends a left bracket character (`[`). */
class LBracketNodeStringBuilder : IInlineNodeStringBuilder {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: ASTNode,
        sourceText: String,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    ) {
        append('[')
    }
}

/** Inline node string builder that appends a right bracket character (`]`). */
class RBracketNodeStringBuilder : IInlineNodeStringBuilder {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: ASTNode,
        sourceText: String,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    ) {
        append(']')
    }
}

/** Inline node string builder that appends a less-than character (`<`). */
class LtNodeStringBuilder : IInlineNodeStringBuilder {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: ASTNode,
        sourceText: String,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    ) {
        append('<')
    }
}

/**
 * Inline node string builder that appends a greater-than character (`>`).
 *
 * When a multi-line blockquote is nested inside a list item, the intellij-markdown
 * parser may tokenize continuation `>` markers as [MarkdownTokenTypes.GT] instead
 * of [MarkdownTokenTypes.BLOCK_QUOTE]. This builder detects that case (GT following
 * an EOL inside a BLOCK_QUOTE element) and suppresses the token so it is not
 * rendered as a literal `>` character.
 */
class GtNodeStringBuilder : IInlineNodeStringBuilder {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: ASTNode,
        sourceText: String,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    ) {
        if (node.isBlockQuoteContinuationMarker()) {
            return
        }
        append('>')
    }
}

/** Inline node string builder that appends a colon character (`:`). */
class ColonNodeStringBuilder : IInlineNodeStringBuilder {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: ASTNode,
        sourceText: String,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    ) {
        append(':')
    }
}

/** Inline node string builder that appends an exclamation mark character (`!`). */
class ExclamationMarkNodeStringBuilder : IInlineNodeStringBuilder {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: ASTNode,
        sourceText: String,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    ) {
        append('!')
    }
}

/** Inline node string builder that appends a backtick character (`` ` ``). */
class BacktickNodeStringBuilder : IInlineNodeStringBuilder {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: ASTNode,
        sourceText: String,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    ) {
        append('`')
    }
}

/**
 * Inline node string builder for emphasis delimiter tokens (`*` or `_`).
 *
 * Only appends the delimiter text when the token is not inside an emphasis or
 * strong emphasis element (i.e., when the delimiter is orphaned/unmatched).
 *
 * @see IInlineNodeStringBuilder
 */
class EmphTokenNodeStringBuilder : IInlineNodeStringBuilder {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: ASTNode,
        sourceText: String,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    ) {
        val parentType = node.parent?.type
        if (parentType != MarkdownElementTypes.EMPH && parentType != MarkdownElementTypes.STRONG) {
            append(node.contentText(sourceText))
        }
    }
}

/**
 * Inline node string builder for whitespace tokens.
 *
 * Appends a single space unless the preceding sibling is a block quote marker,
 * in which case the whitespace is suppressed. Also suppresses whitespace after
 * a GT token that serves as a blockquote continuation marker (GT following EOL
 * inside a BLOCK_QUOTE element).
 *
 * @see IInlineNodeStringBuilder
 */
class WhiteSpaceNodeStringBuilder : IInlineNodeStringBuilder {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: ASTNode,
        sourceText: String,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    ) {
        val prevSibling = node.previousSibling()
        if (prevSibling?.type == MarkdownTokenTypes.BLOCK_QUOTE) {
            return
        }
        // Also suppress whitespace after a GT that is a blockquote continuation marker
        if (prevSibling != null && prevSibling.isBlockQuoteContinuationMarker()) {
            return
        }
        append(' ')
    }
}

/**
 * Inline node string builder for end-of-line tokens.
 *
 * Appends a single space to join lines, unless the preceding sibling is a hard
 * line break (in which case the hard break already produced a newline).
 *
 * @see IInlineNodeStringBuilder
 */
class EolNodeStringBuilder : IInlineNodeStringBuilder {
    override fun AnnotatedString.Builder.buildInlineNodeString(
        node: ASTNode,
        sourceText: String,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    ) {
        if (node.previousSibling()?.type != MarkdownTokenTypes.HARD_LINE_BREAK) {
            append(' ')
        }
    }
}
