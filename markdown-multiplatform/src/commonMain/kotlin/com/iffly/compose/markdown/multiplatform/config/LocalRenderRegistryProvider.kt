@file:Suppress("ktlint:compose:compositionlocal-allowlist")

package com.iffly.compose.markdown.multiplatform.config

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.iffly.compose.markdown.multiplatform.render.RenderRegistry

internal val LocalRenderRegistryProvider =
    staticCompositionLocalOf<RenderRegistry> {
        error("No RenderRegistry provided")
    }

/**
 * Returns the current [RenderRegistry] from the composition local.
 *
 * @see LocalRenderRegistryProvider
 */
@Composable
@ReadOnlyComposable
fun currentRenderRegistry(): RenderRegistry = LocalRenderRegistryProvider.current
