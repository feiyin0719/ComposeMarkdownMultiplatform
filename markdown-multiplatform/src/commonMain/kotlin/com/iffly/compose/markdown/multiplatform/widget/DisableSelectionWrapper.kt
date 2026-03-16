package com.iffly.compose.markdown.multiplatform.widget

import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key

/**
 * A composable wrapper that conditionally disables text selection for its content.
 *
 * When [disabled] is true, the content is wrapped in [DisableSelection]; otherwise
 * the content is rendered directly.
 *
 * @param disabled Whether text selection should be disabled.
 * @param content The composable content to wrap.
 */
@Composable
fun DisableSelectionWrapper(
    disabled: Boolean,
    content: @Composable () -> Unit,
) {
    key(disabled) {
        if (disabled) {
            DisableSelection(content = content)
        } else {
            content()
        }
    }
}
