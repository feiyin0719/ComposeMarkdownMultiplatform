package com.iffly.compose.markdown.multiplatform.util

import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import com.iffly.compose.markdown.multiplatform.render.childNodes
import com.iffly.compose.markdown.multiplatform.style.MarkdownTheme
import org.commonmark.node.BlockQuote
import org.commonmark.node.BulletList
import org.commonmark.node.Heading
import org.commonmark.node.ListBlock
import org.commonmark.node.ListItem
import org.commonmark.node.Node
import org.commonmark.node.OrderedList

/**
 * Returns the heading level (1..6) for Heading nodes.
 * Returns null if the node is not a heading.
 */
fun Node.getHeadingLevel(): Int? = if (this is Heading) level else null

/**
 * Returns the [SpanStyle] for the given [node] based on this theme.
 * If the node is a heading, the corresponding heading style is used; otherwise the default text style is used.
 */
fun MarkdownTheme.getNodeSpanStyle(node: Node): SpanStyle {
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
fun MarkdownTheme.getNodeParagraphStyle(node: Node?): ParagraphStyle {
    if (node == null) return this.textStyle.toParagraphStyle()
    val level = node.getHeadingLevel()
    return if (level != null) {
        this.headStyle[level]?.toParagraphStyle() ?: this.textStyle.toParagraphStyle()
    } else {
        this.textStyle.toParagraphStyle()
    }
}

/** Unicode bullet point character used as the marker for unordered list items. */
const val BULLET_POINT = "\u2022"

/**
 * Returns the marker text for a ListItem node.
 * If the parent is a BulletList, returns a bullet point.
 * If the parent is an OrderedList, returns the numbered marker with proper spacing.
 */
fun Node.getMarkerText(): String {
    val parentNode = this.parent ?: return ""
    return when (parentNode) {
        is BulletList -> {
            BULLET_POINT
        }

        is OrderedList -> {
            val listItems = parentNode.childNodes().filterIsInstance<ListItem>()
            val startNumber = parentNode.markerStartNumber ?: 1
            var index = startNumber
            for ((i, item) in listItems.withIndex()) {
                if (item === this) {
                    index = startNumber + i
                    break
                }
            }
            val max = startNumber + listItems.size
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
 * Returns the indent level of a ListItem node by counting
 * ancestor ListItem nodes.
 */
fun Node.getIndentLevel(): Int {
    var level = 0
    var current = this.parent
    while (current != null) {
        if (current is ListItem) {
            level++
        }
        current = current.parent
    }
    return level
}

/**
 * Returns true if this node is nested inside a BlockQuote.
 */
fun Node.isInQuoteBlock(): Boolean {
    var current = this.parent
    while (current != null) {
        if (current is BlockQuote) {
            return true
        }
        current = current.parent
    }
    return false
}

/**
 * Determines if a list node is "loose".
 * Uses commonmark-kotlin's ListBlock.isTight property.
 */
fun Node.isLooseList(): Boolean {
    if (this is ListBlock) {
        return !isTight
    }
    return false
}
