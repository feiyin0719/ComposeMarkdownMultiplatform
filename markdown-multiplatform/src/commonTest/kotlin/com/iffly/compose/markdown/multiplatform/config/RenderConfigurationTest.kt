package com.iffly.compose.markdown.multiplatform.config

import kotlin.test.Test
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class RenderConfigurationTest {
    @Test
    fun builderCreatesIndependentlyScopedConfigurations() {
        assertNotSame(MarkdownRenderConfig.Builder().build(), MarkdownRenderConfig.Builder().build())
    }

    @Test
    fun textModeRegistryIsCached() {
        val registry = MarkdownRenderConfig.Builder().build().renderRegistry

        assertSame(registry.textModeRegistry(), registry.textModeRegistry())
    }
}
