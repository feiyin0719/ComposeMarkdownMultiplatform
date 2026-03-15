package com.iffly.compose.markdown.multiplatform.render

import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import com.iffly.compose.markdown.multiplatform.config.currentParser
import kotlinx.coroutines.CoroutineScope
import org.intellij.markdown.ast.ASTNode

fun interface MarkdownParser {
    fun parse(sourceText: String): ASTNode
}

data class NodeStringBuilderContext(
    val parser: MarkdownParser,
    val layoutContext: TextLayoutContext,
    val designContext: TextStyleContext,
    val systemContext: SystemContext,
)

data class TextLayoutContext(
    val density: Density,
    val textMeasurer: TextMeasurer,
    val textAlign: TextAlign,
    val sizeConstraints: TextSizeConstraints,
)

data class TextStyleContext(
    val contentColor: Color,
    val textSelectionColors: TextSelectionColors,
    val textStyle: TextStyle,
    val fontFamilyResolver: FontFamily.Resolver,
    val layoutDirection: LayoutDirection,
)

data class SystemContext(
    val clipboard: Clipboard,
    val uriHandler: UriHandler,
    val hapticFeedback: HapticFeedback,
    val softwareKeyboardController: SoftwareKeyboardController?,
    val focusManager: FocusManager,
    val coroutineScope: CoroutineScope,
)

data class TextSizeConstraints(
    val maxWidth: Dp = Dp.Unspecified,
    val maxHeight: Dp = Dp.Unspecified,
    val minHeight: Dp = Dp.Unspecified,
    val minWidth: Dp = Dp.Unspecified,
)

@Composable
fun rememberNodeStringBuilderContext(
    textSizeConstraints: TextSizeConstraints,
    textAlign: TextAlign,
    textStyle: TextStyle? = null,
): NodeStringBuilderContext {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val contentColor = LocalContentColor.current
    val textSelectionColors = LocalTextSelectionColors.current
    val systemTextStyle = LocalTextStyle.current
    val layoutDirection = LocalLayoutDirection.current
    val focusManager = LocalFocusManager.current
    val clipboard = LocalClipboard.current
    val uriHandler = LocalUriHandler.current
    val hapticFeedback = LocalHapticFeedback.current
    val fontFamilyResolver = LocalFontFamilyResolver.current
    val softwareKeyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val parser = currentParser()

    val mergedTextStyle = systemTextStyle.merge(textStyle)

    return remember(
        density,
        textMeasurer,
        textSizeConstraints,
        textAlign,
        contentColor,
        textSelectionColors,
        mergedTextStyle,
        fontFamilyResolver,
        layoutDirection,
        clipboard,
        uriHandler,
        hapticFeedback,
        softwareKeyboardController,
        focusManager,
        scope,
        parser,
    ) {
        NodeStringBuilderContext(
            parser = parser,
            layoutContext =
                TextLayoutContext(
                    density = density,
                    textMeasurer = textMeasurer,
                    textAlign = textAlign,
                    sizeConstraints = textSizeConstraints,
                ),
            designContext =
                TextStyleContext(
                    contentColor = contentColor,
                    textSelectionColors = textSelectionColors,
                    textStyle = mergedTextStyle,
                    fontFamilyResolver = fontFamilyResolver,
                    layoutDirection = layoutDirection,
                ),
            systemContext =
                SystemContext(
                    clipboard = clipboard,
                    uriHandler = uriHandler,
                    hapticFeedback = hapticFeedback,
                    softwareKeyboardController = softwareKeyboardController,
                    focusManager = focusManager,
                    coroutineScope = scope,
                ),
        )
    }
}
