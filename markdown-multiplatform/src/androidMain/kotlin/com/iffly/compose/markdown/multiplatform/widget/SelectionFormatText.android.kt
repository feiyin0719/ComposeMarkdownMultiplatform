package com.iffly.compose.markdown.multiplatform.widget

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.focused
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp

@Composable
actual fun SelectionFormatText(
    text: String,
    modifier: Modifier,
) {
    Layout(
        content = {
            Text(
                text = text,
                modifier =
                    Modifier
                        .invisible()
                        .disableAccessibility(),
                style =
                    TextStyle(
                        color = Color.Transparent,
                    ),
            )
        },
        modifier =
            modifier
                .invisible()
                .disableAccessibility(),
    ) { measurables, constraints ->
        measurables.map { measurable ->
            measurable.measure(Constraints())
        }
        layout(0, 0) {
        }
    }
}

private fun Modifier.invisible(): Modifier =
    this
        .alpha(0f)
        .size(0.dp)

private fun Modifier.disableAccessibility(): Modifier =
    this.semantics {
        hideFromAccessibility()
        text = AnnotatedString("")
        contentDescription = ""
        disabled()
        focused = false
    }
