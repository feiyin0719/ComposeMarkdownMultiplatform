@file:Suppress("ktlint:compose:compositionlocal-allowlist")

package com.iffly.compose.markdown.multiplatform.config

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.iffly.compose.markdown.multiplatform.render.MarkdownParser

internal val LocalParserProvider =
    staticCompositionLocalOf<MarkdownParser> {
        error("No MarkdownParser provided")
    }

@Composable
@ReadOnlyComposable
fun currentParser(): MarkdownParser = LocalParserProvider.current
