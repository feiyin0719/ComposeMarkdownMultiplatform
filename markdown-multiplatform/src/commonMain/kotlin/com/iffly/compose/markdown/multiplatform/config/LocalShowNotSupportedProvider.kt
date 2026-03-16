@file:Suppress("ktlint:compose:compositionlocal-allowlist")

package com.iffly.compose.markdown.multiplatform.config

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

internal val LocalShowNotSupportedProvider =
    staticCompositionLocalOf {
        false
    }

/**
 * Returns whether unsupported Markdown elements should be visually displayed.
 *
 * @see LocalShowNotSupportedProvider
 */
@Composable
@ReadOnlyComposable
fun isShowNotSupported(): Boolean = LocalShowNotSupportedProvider.current
