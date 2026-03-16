package com.iffly.compose.markdown.multiplatform.html.handlers

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

/**
 * Parses inline CSS style attributes from HTML tags into Compose [SpanStyle] objects.
 *
 * Supports CSS properties: `color`, `background-color`, `background`, `font-weight`,
 * `font-style`, `text-decoration`, and `font-size`. Recognizes named colors, hex colors
 * (3, 6, or 8 digit), and `rgb()` notation.
 */
internal object CssStyleParser {
    private val STYLE_REGEX = Regex("""style\s*=\s*["']([^"']*)["']""", RegexOption.IGNORE_CASE)

    private val NAMED_COLORS =
        mapOf(
            "red" to Color.Red,
            "blue" to Color.Blue,
            "green" to Color.Green,
            "black" to Color.Black,
            "white" to Color.White,
            "gray" to Color.Gray,
            "grey" to Color.Gray,
            "yellow" to Color.Yellow,
            "cyan" to Color.Cyan,
            "magenta" to Color.Magenta,
            "transparent" to Color.Transparent,
            "orange" to Color(0xFFFFA500),
            "purple" to Color(0xFF800080),
            "pink" to Color(0xFFFFC0CB),
            "brown" to Color(0xFFA52A2A),
            "darkred" to Color(0xFF8B0000),
            "darkblue" to Color(0xFF00008B),
            "darkgreen" to Color(0xFF006400),
            "lightgray" to Color(0xFFD3D3D3),
            "lightgrey" to Color(0xFFD3D3D3),
            "darkgray" to Color(0xFFA9A9A9),
            "darkgrey" to Color(0xFFA9A9A9),
        )

    /**
     * Parses the `style` attribute from a raw HTML tag string and converts it into a [SpanStyle].
     *
     * @param tag The raw HTML tag string (e.g., `<span style="color: red;">`).
     * @return A [SpanStyle] representing the parsed CSS properties, or null if no style attribute
     *   is found or no supported properties are present.
     */
    fun parseInlineCssStyle(tag: String): SpanStyle? {
        val styleMatch = STYLE_REGEX.find(tag) ?: return null
        val css = styleMatch.groupValues[1]
        if (css.isBlank()) return null

        val properties = parseCssProperties(css)

        val color = properties["color"]?.let { parseCssColor(it) }
        val background = (properties["background-color"] ?: properties["background"])?.let { parseCssColor(it) }
        val fontWeight = properties["font-weight"]?.let { parseFontWeight(it) }
        val fontStyle = properties["font-style"]?.let { parseFontStyle(it) }
        val textDecoration = properties["text-decoration"]?.let { parseTextDecoration(it) }
        val fontSize = properties["font-size"]?.let { parseFontSize(it) }

        if (color == null && background == null && fontWeight == null &&
            fontStyle == null && textDecoration == null && fontSize == null
        ) {
            return null
        }

        return SpanStyle(
            color = color ?: Color.Unspecified,
            background = background ?: Color.Unspecified,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
            textDecoration = textDecoration,
            fontSize = fontSize?.sp ?: androidx.compose.ui.unit.TextUnit.Unspecified,
        )
    }

    private fun parseCssProperties(css: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        css.split(";").forEach { declaration ->
            val parts = declaration.split(":", limit = 2)
            if (parts.size == 2) {
                map[parts[0].trim().lowercase()] = parts[1].trim()
            }
        }
        return map
    }

    private fun parseCssColor(value: String): Color? {
        val trimmed = value.trim().lowercase()

        NAMED_COLORS[trimmed]?.let { return it }

        if (trimmed.startsWith("#")) {
            return parseHexColor(trimmed)
        }

        if (trimmed.startsWith("rgb(") && trimmed.endsWith(")")) {
            return parseRgbColor(trimmed)
        }

        return null
    }

    private fun parseHexColor(hex: String): Color? {
        val h = hex.removePrefix("#")
        return when (h.length) {
            3 -> {
                val r = h[0].toString().repeat(2).toIntOrNull(16) ?: return null
                val g = h[1].toString().repeat(2).toIntOrNull(16) ?: return null
                val b = h[2].toString().repeat(2).toIntOrNull(16) ?: return null
                Color(r, g, b)
            }
            6 -> {
                val colorInt = h.toLongOrNull(16) ?: return null
                Color(
                    red = ((colorInt shr 16) and 0xFF).toInt(),
                    green = ((colorInt shr 8) and 0xFF).toInt(),
                    blue = (colorInt and 0xFF).toInt(),
                )
            }
            8 -> {
                val colorInt = h.toLongOrNull(16) ?: return null
                Color(
                    red = ((colorInt shr 24) and 0xFF).toInt(),
                    green = ((colorInt shr 16) and 0xFF).toInt(),
                    blue = ((colorInt shr 8) and 0xFF).toInt(),
                    alpha = (colorInt and 0xFF).toInt(),
                )
            }
            else -> null
        }
    }

    private fun parseRgbColor(rgb: String): Color? {
        val content = rgb.removePrefix("rgb(").removeSuffix(")")
        val parts = content.split(",").map { it.trim().toIntOrNull() }
        if (parts.size != 3 || parts.any { it == null }) return null
        return Color(parts[0]!!, parts[1]!!, parts[2]!!)
    }

    private fun parseFontWeight(value: String): FontWeight? =
        when (value.trim().lowercase()) {
            "bold" -> FontWeight.Bold
            "normal" -> FontWeight.Normal
            "lighter" -> FontWeight.Light
            "bolder" -> FontWeight.ExtraBold
            else -> value.trim().toIntOrNull()?.let { FontWeight(it) }
        }

    private fun parseFontStyle(value: String): FontStyle? =
        when (value.trim().lowercase()) {
            "italic" -> FontStyle.Italic
            "normal" -> FontStyle.Normal
            else -> null
        }

    private fun parseTextDecoration(value: String): TextDecoration? =
        when (value.trim().lowercase()) {
            "underline" -> TextDecoration.Underline
            "line-through" -> TextDecoration.LineThrough
            "none" -> TextDecoration.None
            else -> null
        }

    private fun parseFontSize(value: String): Float? {
        val trimmed = value.trim().lowercase()
        return when {
            trimmed.endsWith("px") -> trimmed.removeSuffix("px").toFloatOrNull()
            trimmed.endsWith("sp") -> trimmed.removeSuffix("sp").toFloatOrNull()
            trimmed.endsWith("em") -> trimmed.removeSuffix("em").toFloatOrNull()?.times(16f)
            trimmed.endsWith("pt") -> trimmed.removeSuffix("pt").toFloatOrNull()?.times(1.333f)
            else -> null
        }
    }
}
