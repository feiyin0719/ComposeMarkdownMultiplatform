package com.iffly.compose.markdown.multiplatform.util

import com.iffly.compose.markdown.multiplatform.render.NodeContentHashProvider
import org.commonmark.node.CustomNode
import org.commonmark.node.Paragraph
import org.commonmark.node.Text
import org.commonmark.parser.Parser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class NodeContentHashTest {
    private val parser = Parser.builder().build()

    @Test
    fun equalContentParsedSeparatelyHasEqualHash() {
        val markdown = "# Heading\n\nA [link](https://example.com).\n\n```kotlin\nval value = 1\n```"

        assertEquals(parser.parse(markdown).contentHash(), parser.parse(markdown).contentHash())
    }

    @Test
    fun renderedFieldAndChildOrderChangesAffectHash() {
        val original = parser.parse("[first](https://one.example) then second")
        val changedDestination = parser.parse("[first](https://two.example) then second")
        val changedOrder = parser.parse("second then [first](https://one.example)")

        assertNotEquals(original.contentHash(), changedDestination.contentHash())
        assertNotEquals(original.contentHash(), changedOrder.contentHash())
    }

    @Test
    fun customNodeControlsItsRenderContentHash() {
        val first = Paragraph().apply { appendChild(CustomHashNode("first")) }
        val second = Paragraph().apply { appendChild(CustomHashNode("second")) }

        assertNotEquals(first.contentHash(), second.contentHash())
    }

    @Test
    fun largeTreesUseStableRootIdentityFallback() {
        val first = largeParagraph()
        val second = largeParagraph()

        assertEquals(first.contentHash(), first.contentHash())
        assertNotEquals(first.contentHash(), second.contentHash())
    }

    private fun largeParagraph(): Paragraph =
        Paragraph().apply {
            repeat(1024) { appendChild(Text("item")) }
        }

    private class CustomHashNode(
        private val value: String,
    ) : CustomNode(),
        NodeContentHashProvider {
        override fun contentHash(): Int = value.hashCode()
    }
}
