package com.iffly.compose.markdown.multiplatform.util

import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import com.iffly.compose.markdown.multiplatform.style.MarkdownTheme
import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode

/**
 * Finds the first direct child node of the given [type], or null if none exists.
 */
fun ASTNode.findChildOfType(type: IElementType): ASTNode? = children.firstOrNull { it.type == type }

/**
 * Extracts the raw text of this node from the original [sourceText].
 */
fun ASTNode.getTextInNode(sourceText: String): String = sourceText.substring(startOffset, endOffset)

/**
 * Returns the heading level (1..6) for ATX_1..ATX_6 and SETEXT_1/SETEXT_2 nodes.
 * Returns null if the node is not a heading.
 */
fun ASTNode.getHeadingLevel(): Int? =
    when (type) {
        MarkdownElementTypes.ATX_1 -> 1
        MarkdownElementTypes.ATX_2 -> 2
        MarkdownElementTypes.ATX_3 -> 3
        MarkdownElementTypes.ATX_4 -> 4
        MarkdownElementTypes.ATX_5 -> 5
        MarkdownElementTypes.ATX_6 -> 6
        MarkdownElementTypes.SETEXT_1 -> 1
        MarkdownElementTypes.SETEXT_2 -> 2
        else -> null
    }

/**
 * Returns the [SpanStyle] for the given [node] based on this theme.
 * If the node is a heading, the corresponding heading style is used; otherwise the default text style is used.
 */
fun MarkdownTheme.getNodeSpanStyle(node: ASTNode): SpanStyle {
    val level = node.getHeadingLevel()
    return if (level != null) {
        this.headStyle[level]?.toSpanStyle() ?: this.textStyle.toSpanStyle()
    } else {
        this.textStyle.toSpanStyle()
    }
}

/**
 * Returns the [ParagraphStyle] for the given [node] based on this theme.
 * If the node is a heading, the corresponding heading style is used; otherwise the default text style is used.
 */
fun MarkdownTheme.getNodeParagraphStyle(node: ASTNode?): ParagraphStyle {
    if (node == null) return this.textStyle.toParagraphStyle()
    val level = node.getHeadingLevel()
    return if (level != null) {
        this.headStyle[level]?.toParagraphStyle() ?: this.textStyle.toParagraphStyle()
    } else {
        this.textStyle.toParagraphStyle()
    }
}

/**
 * Returns the raw text content of this node extracted from [sourceText].
 *
 * @see getTextInNode
 */
fun ASTNode.contentText(sourceText: String): String = getTextInNode(sourceText)

/** Unicode bullet point character used as the marker for unordered list items. */
const val BULLET_POINT = "\u2022"

/**
 * Returns the marker text for a LIST_ITEM node.
 * If the parent is an UNORDERED_LIST, returns a bullet point.
 * If the parent is an ORDERED_LIST, returns the numbered marker with proper spacing.
 */
fun ASTNode.getMarkerText(): String {
    val parentNode = this.parent ?: return ""
    return when (parentNode.type) {
        MarkdownElementTypes.UNORDERED_LIST -> {
            BULLET_POINT
        }

        MarkdownElementTypes.ORDERED_LIST -> {
            val listItems = parentNode.children.filter { it.type == MarkdownElementTypes.LIST_ITEM }
            val max = listItems.size + 1
            var index = 1
            for ((i, item) in listItems.withIndex()) {
                if (item === this) {
                    index = i + 1
                    break
                }
            }
            val maxLength = max.toString().length
            val indexString = index.toString()
            val indexLength = indexString.length
            "${StringExt.FIGURE_SPACE.repeat(maxLength - indexLength)}$indexString."
        }

        else -> {
            ""
        }
    }
}

/**
 * Returns the indent level of a LIST_ITEM node by counting
 * ancestor LIST_ITEM nodes.
 */
fun ASTNode.getIndentLevel(): Int {
    var level = 0
    var current = this.parent
    while (current != null) {
        if (current.type == MarkdownElementTypes.LIST_ITEM) {
            level++
        }
        current = current.parent
    }
    return level
}

/**
 * Returns true if this node is nested inside a BLOCK_QUOTE.
 */
fun ASTNode.isInQuoteBlock(): Boolean {
    var current = this.parent
    while (current != null) {
        if (current.type == MarkdownElementTypes.BLOCK_QUOTE) {
            return true
        }
        current = current.parent
    }
    return false
}

/**
 * Determines if a list (ORDERED_LIST or UNORDERED_LIST) is "loose".
 * A loose list has blank lines between its items.
 * We approximate this by checking if there are multiple consecutive EOL tokens between list items.
 */
fun ASTNode.isLooseList(): Boolean {
    var consecutiveEol = 0
    for (child in children) {
        if (child.type == MarkdownTokenTypes.EOL) {
            consecutiveEol++
            if (consecutiveEol >= 2) return true
        } else {
            consecutiveEol = 0
        }
    }
    return false
}

/**
 * Returns the previous sibling of this node, or null if this is the first child.
 */
fun ASTNode.previousSibling(): ASTNode? {
    val parent = this.parent ?: return null
    val siblings = parent.children
    val index = siblings.indexOf(this)
    return if (index > 0) siblings[index - 1] else null
}

/**
 * Returns true if this GT token is a blockquote continuation marker that was
 * incorrectly tokenized by intellij-markdown.
 *
 * When a multi-line blockquote is nested inside a list item, continuation `>`
 * markers may be tokenized as [MarkdownTokenTypes.GT] instead of
 * [MarkdownTokenTypes.BLOCK_QUOTE]. This function detects that case by walking
 * backwards through previous siblings, skipping [MarkdownTokenTypes.WHITE_SPACE]
 * and other [MarkdownTokenTypes.GT] tokens, and checking whether an
 * [MarkdownTokenTypes.EOL] is found — indicating this GT is at the logical start
 * of a line inside a BLOCK_QUOTE element.
 */
fun ASTNode.isBlockQuoteContinuationMarker(): Boolean {
    if (type != MarkdownTokenTypes.GT) return false
    if (!isInQuoteBlock()) return false
    var current = previousSibling()
    while (current != null) {
        when (current.type) {
            MarkdownTokenTypes.WHITE_SPACE,
            MarkdownTokenTypes.GT,
            -> current = current.previousSibling()

            MarkdownTokenTypes.EOL -> return true

            else -> return false
        }
    }
    return false
}
