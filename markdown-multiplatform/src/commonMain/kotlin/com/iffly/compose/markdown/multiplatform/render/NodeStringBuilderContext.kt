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
import org.commonmark.node.Node

/** Functional interface for parsing markdown source text into a node tree. */
fun interface MarkdownParser {
    /**
     * Parses the given markdown source text into a node tree.
     *
     * @param sourceText The raw markdown text to parse.
     * @return The root [Node] of the parsed tree.
     */
    fun parse(sourceText: String): Node
}

/**
 * Aggregated context used during inline node string building.
 * Bundles the markdown parser with layout, design, and system contexts needed to produce styled text.
 *
 * @property parser The markdown parser for re-parsing nested content if needed.
 * @property layoutContext Layout-related context such as density, text measurer, and size constraints.
 * @property designContext Design-related context such as text style, content color, and font resolver.
 * @property systemContext Platform system services such as clipboard, URI handler, and haptic feedback.
 */
data class NodeStringBuilderContext(
    val parser: MarkdownParser,
    val layoutContext: TextLayoutContext,
    val designContext: TextStyleContext,
    val systemContext: SystemContext,
)

/**
 * Layout-related context for text measurement and sizing during inline string building.
 *
 * @property density The current screen density.
 * @property textMeasurer The text measurer for computing text dimensions.
 * @property textAlign The horizontal text alignment.
 * @property sizeConstraints The size constraints for the text layout area.
 */
data class TextLayoutContext(
    val density: Density,
    val textMeasurer: TextMeasurer,
    val textAlign: TextAlign,
    val sizeConstraints: TextSizeConstraints,
)

/**
 * Design and styling context for text rendering during inline string building.
 *
 * @property contentColor The current content color.
 * @property textSelectionColors The colors used for text selection highlights.
 * @property textStyle The resolved text style.
 * @property fontFamilyResolver The font family resolver for font lookup.
 * @property layoutDirection The layout direction (LTR or RTL).
 */
data class TextStyleContext(
    val contentColor: Color,
    val textSelectionColors: TextSelectionColors,
    val textStyle: TextStyle,
    val fontFamilyResolver: FontFamily.Resolver,
    val layoutDirection: LayoutDirection,
)

/**
 * Platform system services context used during inline string building.
 *
 * @property clipboard The platform clipboard for copy operations.
 * @property uriHandler The URI handler for opening links.
 * @property hapticFeedback The haptic feedback provider.
 * @property softwareKeyboardController Optional software keyboard controller.
 * @property focusManager The focus manager for controlling focus.
 * @property coroutineScope A coroutine scope for launching asynchronous operations.
 */
data class SystemContext(
    val clipboard: Clipboard,
    val uriHandler: UriHandler,
    val hapticFeedback: HapticFeedback,
    val softwareKeyboardController: SoftwareKeyboardController?,
    val focusManager: FocusManager,
    val coroutineScope: CoroutineScope,
)

/**
 * Size constraints for the text layout area, expressed in [Dp].
 *
 * @property maxWidth The maximum width available for text layout.
 * @property maxHeight The maximum height available for text layout.
 * @property minHeight The minimum height for text layout.
 * @property minWidth The minimum width for text layout.
 */
data class TextSizeConstraints(
    val maxWidth: Dp = Dp.Unspecified,
    val maxHeight: Dp = Dp.Unspecified,
    val minHeight: Dp = Dp.Unspecified,
    val minWidth: Dp = Dp.Unspecified,
)

/**
 * Creates and remembers a [NodeStringBuilderContext] by capturing current composition locals
 * including density, text measurer, content color, clipboard, and other platform services.
 *
 * @param textSizeConstraints The size constraints for the text layout area.
 * @param textAlign The horizontal text alignment.
 * @param textStyle Optional text style override to merge with the current local text style.
 * @return A remembered [NodeStringBuilderContext] instance.
 */
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
