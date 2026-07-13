@file:Suppress("ktlint:compose:compositionlocal-allowlist")

package com.iffly.compose.markdown.multiplatform.config

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.iffly.compose.markdown.multiplatform.ActionHandlerState

internal val LocalActionHandlerProvider =
    staticCompositionLocalOf<ActionHandlerState> {
        mutableStateOf(null)
    }

/**
 * Returns the current [ActionHandler] from the composition local, or `null` if none is provided.
 *
 * @see LocalActionHandlerProvider
 */
@Composable
@ReadOnlyComposable
fun currentActionHandler(): ActionHandlerState = LocalActionHandlerProvider.current
