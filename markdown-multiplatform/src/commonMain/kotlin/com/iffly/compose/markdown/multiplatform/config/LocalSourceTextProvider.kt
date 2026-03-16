@file:Suppress("ktlint:compose:compositionlocal-allowlist")

package com.iffly.compose.markdown.multiplatform.config

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

internal val LocalSourceTextProvider =
    staticCompositionLocalOf<String> {
        error("No source text provided")
    }

/**
 * Returns the current raw Markdown source text from the composition local.
 *
 * @see LocalSourceTextProvider
 */
@Composable
@ReadOnlyComposable
fun currentSourceText(): String = LocalSourceTextProvider.current
