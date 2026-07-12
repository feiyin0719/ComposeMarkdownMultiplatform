package com.iffly.compose.markdown.multiplatform.render

import kotlin.test.Test
import kotlin.test.assertEquals

class InlineContentIdTest {
    @Test
    fun duplicateContentIdsPreserveEveryInlineRenderer() {
        val inlineContent = mutableMapOf<String, String>()

        val firstId = inlineContent.putUniqueInlineContent("CodeBlock_42", "first")
        val secondId = inlineContent.putUniqueInlineContent("CodeBlock_42", "second")

        assertEquals("CodeBlock_42", firstId)
        assertEquals("CodeBlock_42_1", secondId)
        assertEquals(mapOf(firstId to "first", secondId to "second"), inlineContent)
    }
}
