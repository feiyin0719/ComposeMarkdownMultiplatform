package com.iffly.compose.markdown.multiplatform.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A Composable that renders invisible text for selection purposes.
 * On Android, this text does not take up any space in the layout and is hidden from accessibility services.
 * On other platforms, this is a no-op.
 * @param text The text to be rendered for selection.
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
expect fun SelectionFormatText(
    text: String,
    modifier: Modifier = Modifier,
)
