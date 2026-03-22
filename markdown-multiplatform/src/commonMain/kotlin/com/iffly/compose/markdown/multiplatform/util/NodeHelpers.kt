package com.iffly.compose.markdown.multiplatform.util

import org.commonmark.node.Code
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.HardLineBreak
import org.commonmark.node.HtmlBlock
import org.commonmark.node.HtmlInline
import org.commonmark.node.IndentedCodeBlock
import org.commonmark.node.Node
import org.commonmark.node.SoftLineBreak
import org.commonmark.node.Text
import org.commonmark.node.ThematicBreak

/**
 * Returns the text content of a node.
 * For leaf nodes with literal content, returns the literal.
 * For composite nodes, recursively collects text from children.
 */
fun Node.nodeTextContent(): String =
    when (this) {
        is Text -> {
            literal
        }

        is Code -> {
            literal
        }

        is HtmlInline -> {
            literal ?: ""
        }

        is HtmlBlock -> {
            literal ?: ""
        }

        is FencedCodeBlock -> {
            literal ?: ""
        }

        is IndentedCodeBlock -> {
            literal ?: ""
        }

        is ThematicBreak -> {
            literal ?: ""
        }

        is SoftLineBreak -> {
            "\n"
        }

        is HardLineBreak -> {
            "\n"
        }

        else -> {
            val sb = StringBuilder()
            var child = firstChild
            while (child != null) {
                sb.append(child.nodeTextContent())
                child = child.next
            }
            sb.toString()
        }
    }
