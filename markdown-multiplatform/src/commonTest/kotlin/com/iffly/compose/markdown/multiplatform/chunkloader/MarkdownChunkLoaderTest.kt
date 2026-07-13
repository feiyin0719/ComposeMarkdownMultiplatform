package com.iffly.compose.markdown.multiplatform.chunkloader

import com.iffly.compose.markdown.multiplatform.render.MarkdownParser
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.Heading
import org.commonmark.parser.IncludeSourceSpans
import org.commonmark.parser.Parser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class MarkdownChunkLoaderTest {
    private val parser =
        Parser
            .builder()
            .includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES)
            .build()

    @Test
    fun trailingBlockIsHeldUntilTheNextRangeConfirmsItsBoundary() =
        runTest {
            val source =
                StringMarkdownLineSource(
                    """
                    # Heading

                    ```kotlin
                    val value = 1
                    ```

                    After
                    """.trimIndent(),
                )
            val loader = MarkdownChunkLoader(source, MarkdownParser(parser::parse))

            val first = loader.load(3)
            assertEquals(1, first.nodes.size)
            assertIs<Heading>(first.nodes.single().node)
            assertEquals(0..0, first.nodes.single().startLine..first.nodes.single().endLine)
            assertFalse(first.endOfSource)

            val second = loader.load(3)
            assertEquals(1, second.nodes.size)
            assertIs<FencedCodeBlock>(second.nodes.single().node)
            assertEquals(2..4, second.nodes.single().startLine..second.nodes.single().endLine)
            assertFalse(second.endOfSource)

            val third = loader.load(3)
            assertEquals(1, third.nodes.size)
            assertEquals(6..6, third.nodes.single().startLine..third.nodes.single().endLine)
            assertTrue(third.endOfSource)
        }

    @Test
    fun nodeWindowRecyclesBehindTheViewportAndReloadsIt() =
        runTest {
            val text = (0 until 12).joinToString("\n\n") { index -> "# Heading $index" }
            val config =
                MarkdownChunkLoaderConfig(
                    initialLineCount = 8,
                    incrementalLineCount = 8,
                    minNodesAhead = 1,
                    minNodesBehind = 0,
                    maxCachedNodes = 4,
                    maxCachedSourceLines = 100,
                    minRecycleNodeCount = 2,
                )
            val loader =
                MarkdownChunkLoader(
                    source = StringMarkdownLineSource(text),
                    parser = MarkdownParser(parser::parse),
                )
            val window = MarkdownNodeWindow(loader, config)

            val initial = window.loadInitial()
            val firstKey = initial.nodes.first().key
            val after = window.loadAfter(initial.nodes.last().key)

            assertTrue(after.canLoadBefore)
            assertFalse(after.nodes.any { it.key == firstKey })

            val before = window.loadBefore(after.nodes.first().key)
            assertTrue(before.nodes.any { it.key == firstKey })
        }

    @Test
    fun readBatchMayExceedSourceLimitWhenRetainedBlocksDoNot() =
        runTest {
            val loader =
                MarkdownChunkLoader(
                    source = StringMarkdownLineSource("# One\n\n# Two\n\n# Three\n\n# Four"),
                    parser = MarkdownParser(parser::parse),
                    maxCachedSourceLines = 2,
                )

            val first = loader.load(6)
            assertEquals(3, first.nodes.size)
            assertFalse(first.endOfSource)

            val second = loader.load(6)
            assertEquals(1, second.nodes.size)
            assertTrue(second.endOfSource)
        }

    @Test
    fun sourcePressureRecyclesFewerNodesThanTheNormalMinimum() =
        runTest {
            val config =
                MarkdownChunkLoaderConfig(
                    initialLineCount = 6,
                    incrementalLineCount = 2,
                    minNodesAhead = 1,
                    minNodesBehind = 0,
                    maxCachedNodes = 10,
                    maxCachedSourceLines = 3,
                    minRecycleNodeCount = 5,
                )
            val loader =
                MarkdownChunkLoader(
                    source = StringMarkdownLineSource("# One\n\n# Two\n\n# Three\n\n# Four"),
                    parser = MarkdownParser(parser::parse),
                    maxCachedSourceLines = config.maxCachedSourceLines,
                )
            val window = MarkdownNodeWindow(loader, config)

            val initial = window.loadInitial()
            assertTrue(initial.needsRecycle)

            val recycled = window.recycle(initial.nodes[1].key, initial.nodes.last().key)
            assertEquals(2, recycled.nodes.size)
            assertEquals(3, recycled.cachedSourceLineCount)
            assertTrue(recycled.canLoadBefore)
        }

    @Test
    fun singleUnconfirmedBlockCannotExceedSourceLimit() {
        assertFailsWith<IllegalArgumentException> {
            runTest {
                val loader =
                    MarkdownChunkLoader(
                        source = StringMarkdownLineSource("```\none\ntwo\n```\n\n# Next"),
                        parser = MarkdownParser(parser::parse),
                        maxCachedSourceLines = 3,
                    )
                loader.load(4)
            }
        }
    }
}

private fun runTest(block: suspend () -> Unit) {
    kotlinx.coroutines.runBlocking { block() }
}
