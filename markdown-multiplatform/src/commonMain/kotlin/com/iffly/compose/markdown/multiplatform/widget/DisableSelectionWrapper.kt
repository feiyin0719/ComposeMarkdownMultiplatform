package com.iffly.compose.markdown.multiplatform.widget

import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key

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
