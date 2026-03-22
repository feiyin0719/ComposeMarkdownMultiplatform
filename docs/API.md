# ComposeMarkdownMultiplatform API Reference

[English](API.md) | [简体中文](API_zh-CN.md)

> Detailed reference for the core composables and configuration types in the `markdown-multiplatform` module.
>
> - For an overview, installation and feature tour, see [README.md](../README.md).

## Table of Contents

- [Core Composables](#core-composables)
  - [MarkdownView](#markdownview)
  - [LazyMarkdownColumn](#lazymarkdowncolumn)
  - [MarkdownContent](#markdowncontent)
  - [MarkdownChildren](#markdownchildren)
  - [MarkdownText](#markdowntext)
- [Configuration](#configuration)
  - [MarkdownRenderConfig](#markdownrenderconfig)
  - [MarkdownRenderConfig.Builder](#markdownrenderconfigbuilder)
  - [MarkdownTheme](#markdowntheme)
- [Plugins & Extension Points](#plugins--extension-points)
  - [IMarkdownRenderPlugin](#imarkdownrenderplugin)
  - [IBlockRenderer](#iblockrenderer)
  - [IInlineNodeStringBuilder](#iinlinenodestringbuilder)
  - [MarkdownInlineView](#markdowninlineview)
  - [RenderRegistry & Core Renderers](#renderregistry--core-renderers)
- [Plugin Modules](#plugin-modules)
  - [TableMarkdownPlugin](#tablemarkdownplugin)
  - [ImageMarkdownPlugin](#imagemarkdownplugin)
  - [HtmlMarkdownPlugin](#htmlmarkdownplugin)
- [Other APIs](#other-apis)
  - [ActionHandler](#actionhandler)
  - [NodeStringBuilderContext](#nodestringbuildercontext)
  - [Composition Local Accessors](#composition-local-accessors)
- [Common Usage Patterns](#common-usage-patterns)

---

## Core Composables

### MarkdownView

The main entry point for rendering Markdown content. Parses and renders a Markdown string.

**Signature** (from `MarkdownView.kt`):

```kotlin
@Composable
fun MarkdownView(
    text: String,
    modifier: Modifier = Modifier,
    markdownRenderConfig: MarkdownRenderConfig =
        remember { MarkdownRenderConfig.Builder().build() },
    actionHandler: ActionHandler? = null,
    showNotSupported: Boolean = false,
)
```

**Parameters**

- `text`: The Markdown text to render.
- `modifier`: Standard Compose `Modifier` for sizing, padding, etc.
- `markdownRenderConfig`: Rendering configuration, created via `MarkdownRenderConfig.Builder()`. Defaults to a basic config if omitted.
- `actionHandler`: Optional handler for interactions (links, images, custom events).
- `showNotSupported`: When `true`, unsupported elements display fallback text instead of being silently ignored.

**Example**

```kotlin
@Composable
fun SimpleExample() {
    MarkdownView(
        text = "# Hello\n\nThis is **Markdown**.",
        modifier = Modifier.fillMaxSize(),
    )
}
```

---

### LazyMarkdownColumn

Renders Markdown content using a `LazyColumn` for efficient display of large documents. Each top-level block becomes an independent lazy item.

**Signature** (from `LazyMarkdownColumn.kt`):

```kotlin
@Composable
fun LazyMarkdownColumn(
    text: String,
    modifier: Modifier = Modifier,
    markdownRenderConfig: MarkdownRenderConfig =
        remember { MarkdownRenderConfig.Builder().build() },
    actionHandler: ActionHandler? = null,
    showNotSupported: Boolean = false,
    lazyListState: LazyListState = rememberLazyListState(),
)
```

**Parameters**

- `text`: The Markdown text to render.
- `markdownRenderConfig`: Rendering configuration.
- `actionHandler`: Optional interaction handler.
- `showNotSupported`: Whether to show fallback for unsupported elements.
- `lazyListState`: Optional `LazyListState` for external scroll control.

**Example**

```kotlin
@Composable
fun LazyExample() {
    val config = remember { MarkdownRenderConfig.Builder().build() }

    LazyMarkdownColumn(
        text = longMarkdownContent,
        markdownRenderConfig = config,
        modifier = Modifier.fillMaxSize(),
    )
}
```

---

### MarkdownContent

Renders a single parsed AST node and dispatches to the appropriate block renderer.

**Signature** (from `MarkdownContent.kt`):

```kotlin
@Composable
fun MarkdownContent(
    node: Node,
    modifier: Modifier = Modifier,
)
```

Typically used internally or within custom `IBlockRenderer` implementations when you need to recursively render child nodes.

---

### MarkdownChildren

A utility composable for iterating and rendering all children of a parent node with proper spacing.

**Signature** (from `MarkdownContent.kt`):

```kotlin
@Composable
fun MarkdownChildren(
    parent: Node,
    modifier: Modifier = Modifier,
    children: List<Node>? = null,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    spacerHeight: Dp = currentTheme().spacerTheme.spacerHeight,
    showSpacer: Boolean = currentTheme().spacerTheme.showSpacer,
    childModifierFactory: (child: Node) -> Modifier = {
        Modifier.wrapContentHeight().fillMaxWidth()
    },
    onBeforeChild: (@Composable (child: Node, parent: Node) -> Unit)? = null,
    onAfterChild: (@Composable (child: Node, parent: Node) -> Unit)? = null,
    onBeforeAll: (@Composable (parent: Node) -> Unit)? = null,
    onAfterAll: (@Composable (parent: Node) -> Unit)? = null,
)
```

**Parameters**

- `parent`: The `Node` whose children to render.
- `children`: Override list of children to render (defaults to `parent`'s children).
- `spacerHeight`: Vertical spacing between children. Defaults to `theme.spacerTheme.spacerHeight`.
- `showSpacer`: Whether to insert spacers. Defaults to `theme.spacerTheme.showSpacer`.
- `onBeforeChild` / `onAfterChild`: Optional composable callbacks before/after each child.
- `onBeforeAll` / `onAfterAll`: Optional composable callbacks before/after all children.

**Use Case**

When implementing a custom `IBlockRenderer` (e.g., a custom container block) and you need to render nested content with standard spacing rules.

---

### MarkdownText

Renders the inline children of a block node as styled text using `AnnotatedString`.

**Signature** (from `MarkdownText.kt`):

```kotlin
@Composable
fun MarkdownText(
    parent: Node,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    textStyle: TextStyle? = null,
)
```

**Parameters**

- `parent`: The block node whose inline children to render as text.
- `textAlign`: Text alignment.
- `textStyle`: Override text style (defaults to theme style).

---

## Configuration

### MarkdownRenderConfig

`MarkdownRenderConfig` holds everything needed for parsing and rendering Markdown:

- A `MarkdownTheme` describing typography, colors and component styles.
- A `MarkdownParser` (powered by `commonmark-kotlin`).
- A `RenderRegistry` mapping node types to renderers.

Instances are created via `MarkdownRenderConfig.Builder`:

```kotlin
val config = MarkdownRenderConfig.Builder()
    // configure theme, plugins, renderers...
    .build()
```

---

### MarkdownRenderConfig.Builder

Builder for customizing parsing, theming and rendering behavior using a fluent API.

**Key methods** (from `MarkdownRenderConfig.kt`):

```kotlin
class MarkdownRenderConfig {
    class Builder {
        fun markdownTheme(markdownTheme: MarkdownTheme): Builder
        fun addPlugin(plugin: IMarkdownRenderPlugin): Builder
        fun <T : Node> addInlineNodeStringBuilder(
            nodeClass: KClass<T>,
            builder: IInlineNodeStringBuilder<T>,
        ): Builder
        fun <T : Node> addBlockRenderer(
            nodeClass: KClass<T>,
            renderer: IBlockRenderer<T>,
        ): Builder
        fun addExtension(extension: Extension): Builder
        fun markdownTextRenderer(renderer: MarkdownTextRenderer): Builder
        fun markdownContentRenderer(renderer: MarkdownContentRenderer): Builder
        fun build(): MarkdownRenderConfig
    }
}
```

#### markdownTheme(markdownTheme: MarkdownTheme)

Sets the visual theme. If not set, uses default `MarkdownTheme()`.

#### addPlugin(plugin: IMarkdownRenderPlugin)

Registers a rendering plugin. Plugins can provide custom parser extensions, block renderers and inline string builders.

#### addInlineNodeStringBuilder / addBlockRenderer

Low-level hooks for customizing rendering of specific node types:

- `addInlineNodeStringBuilder(nodeClass, builder)`: Defines how an inline node type is converted to styled text spans.
- `addBlockRenderer(nodeClass, renderer)`: Defines how a block node type is rendered as Compose UI.

#### addExtension(extension: Extension)

Register a commonmark parser extension directly (e.g., `TablesExtension.create()`). Extensions registered via plugins are also collected automatically.

#### markdownTextRenderer / markdownContentRenderer

Advanced overrides:

- `markdownTextRenderer`: Override how text nodes are rendered.
- `markdownContentRenderer`: Override how content nodes are rendered.

---

### MarkdownTheme

`MarkdownTheme` is the **core theme model** controlling how Markdown content appears in Compose.

#### Data Structure

```kotlin
@Stable
data class MarkdownTheme(
    val breakLineHeight: Dp = 1.dp,
    val breakLineColor: Color = Color(0xFFE0E0E0),
    val textStyle: TextStyle = TextStyle(
        fontSize = 18.sp,
        fontFamily = FontFamily.Default,
        color = Color.Black,
        lineHeight = 20.sp,
    ),
    val strongEmphasis: SpanStyle = SpanStyle(fontWeight = FontWeight.Bold),
    val emphasis: SpanStyle = SpanStyle(fontStyle = FontStyle.Italic),
    val code: TextStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 14.sp,
        color = Color(0xFF37474F),
        background = Color(0xFFF5F5F5),
    ),
    val strikethrough: SpanStyle = SpanStyle(textDecoration = TextDecoration.LineThrough),
    val subscript: SpanStyle = SpanStyle(baselineShift = Subscript),
    val link: TextLinkStyles = TextLinkStyles(...),
    val headStyle: Map<Int, TextStyle> = mapOf(
        HEAD1 to TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, lineHeight = 36.sp),
        HEAD2 to TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, lineHeight = 32.sp),
        HEAD3 to TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp),
        HEAD4 to TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, lineHeight = 24.sp),
        HEAD5 to TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, lineHeight = 22.sp),
        HEAD6 to TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, lineHeight = 20.sp),
    ),
    val listTheme: ListTheme = ListTheme(),
    val blockQuoteTheme: BlockQuoteTheme = BlockQuoteTheme(),
    val spacerTheme: SpacerTheme = SpacerTheme(),
    val codeBlockTheme: CodeBlockTheme = CodeBlockTheme(),
) {
    companion object {
        const val HEAD1 = 1
        const val HEAD2 = 2
        // ... through HEAD6
    }
}
```

#### Heading Levels

Headings are configured via the `headStyle` map. Keys are integers 1-6, exposed as constants:

```kotlin
MarkdownTheme.HEAD1 // H1
MarkdownTheme.HEAD2 // H2
// ... through HEAD6
```

#### BlockQuoteTheme

```kotlin
@Immutable
data class BlockQuoteTheme(
    val borderColor: Color = Color.LightGray,
    val borderWidth: Dp = 5.dp,
    val backgroundColor: Color = Color(0xFFF5F5F5),
    val shape: Shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
    val padding: PaddingValues = PaddingValues(horizontal = 12.dp),
    val textStyle: TextStyle? = TextStyle(fontStyle = FontStyle.Italic),
)
```

#### SpacerTheme

```kotlin
@Immutable
data class SpacerTheme(
    val showSpacer: Boolean = true,
    val spacerHeight: Dp = 12.dp,
)
```

#### ListTheme

```kotlin
@Immutable
data class ListTheme(
    val markerSpacerWidth: Dp = 4.dp,
    val showSpacerInTightList: Boolean = true,
    val tightListSpacerHeight: Dp = 8.dp,
    val markerTextStyle: TextStyle? = TextStyle(
        lineHeight = 24.sp,
        fontSize = 17.sp,
        textAlign = TextAlign.End,
    ),
)
```

#### CodeBlockTheme

```kotlin
@Immutable
data class CodeBlockTheme(
    val backgroundColor: Color = Color.White,
    val shape: Shape = RoundedCornerShape(size = 16.dp),
    val borderColor: Color = Color.LightGray,
    val borderWidth: Dp = 0.5.dp,
    val contentTheme: CodeContentTheme = CodeContentTheme(),
    val codeTitleTextStyle: TextStyle = TextStyle(fontSize = 12.sp, color = Color.Gray),
    val codeCopyTextStyle: TextStyle = TextStyle(fontSize = 12.sp, color = Color.Blue),
    val blockModifier: Modifier = Modifier.padding(vertical = 12.dp),
    val headerModifier: Modifier = Modifier.padding(horizontal = 17.dp),
    val showHeader: Boolean = true,
    val showCopyButton: Boolean = true,
)
```

#### CodeContentTheme

```kotlin
@Immutable
data class CodeContentTheme(
    val showLineNumber: Boolean = true,
    val softWrap: Boolean = true,
    val maxLines: Int = Int.MAX_VALUE,
    val minLines: Int = 1,
    val contentPadding: PaddingValues = PaddingValues(4.dp),
    val lineNumberPadding: PaddingValues = PaddingValues(
        start = 4.dp, top = 4.dp, bottom = 4.dp, end = 16.dp,
    ),
    val overflow: TextOverflow = TextOverflow.Clip,
    val codeTextStyle: TextStyle = TextStyle(fontSize = 14.sp, lineHeight = 18.sp),
    val lineNumberTextStyle: TextStyle = TextStyle(
        fontSize = 14.sp, lineHeight = 18.sp, color = Color.Gray, textAlign = TextAlign.End,
    ),
    val modifier: Modifier = Modifier.padding(start = 17.dp, end = 17.dp, top = 17.dp),
    val height: Dp? = null,
    val disableSelection: Boolean = false,
)
```

**Example**

```kotlin
val theme = MarkdownTheme(
    textStyle = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    headStyle = mapOf(
        MarkdownTheme.HEAD1 to TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold),
    ),
    codeBlockTheme = CodeBlockTheme(
        backgroundColor = Color(0xFF1E1E1E),
        contentTheme = CodeContentTheme(
            codeTextStyle = TextStyle(color = Color.White, fontSize = 14.sp),
        ),
    ),
)

val config = MarkdownRenderConfig.Builder()
    .markdownTheme(theme)
    .build()
```

---

## Plugins & Extension Points

### IMarkdownRenderPlugin

The entry point for adding functionality to the Markdown engine.

```kotlin
interface IMarkdownRenderPlugin {
    fun parserExtensions(): List<Extension> = emptyList()
    fun blockRenderers(): Map<KClass<out Node>, IBlockRenderer<*>> = emptyMap()
    fun inlineNodeStringBuilders(): Map<KClass<out Node>, IInlineNodeStringBuilder<*>> = emptyMap()
}
```

A plugin can:

- Provide custom parser extensions via `parserExtensions()` (e.g., `TablesExtension.create()` for GFM tables).
- Register block renderers and inline string builders.

Plugins are added via `MarkdownRenderConfig.Builder.addPlugin()`.

You can also extend `AbstractMarkdownRenderPlugin` for convenience:

```kotlin
abstract class AbstractMarkdownRenderPlugin : IMarkdownRenderPlugin {
    override fun parserExtensions(): List<Extension> = emptyList()
    override fun blockRenderers(): Map<KClass<out Node>, IBlockRenderer<*>> = emptyMap()
    override fun inlineNodeStringBuilders(): Map<KClass<out Node>, IInlineNodeStringBuilder<*>> = emptyMap()
}
```

---

### IBlockRenderer

Renders a specific block node as Compose UI.

```kotlin
interface IBlockRenderer<T : Node> {
    @Composable
    fun Invoke(
        node: T,
        modifier: Modifier,
    )
}
```

**Parameters**

- `node`: The commonmark AST node to render.
- `modifier`: Modifier from the parent layout. Implementations should apply it to preserve layout consistency.

**Implementation Tips**

- Use `MarkdownChildren` to render nested children with standard spacing.
- Access the current theme via `currentTheme()`.
- Access the action handler via `currentActionHandler()`.

---

### IInlineNodeStringBuilder

Converts an inline node into styled text spans within an `AnnotatedString`.

```kotlin
interface IInlineNodeStringBuilder<T : Node> {
    fun AnnotatedString.Builder.buildInlineNodeString(
        node: T,
        inlineContentMap: MutableMap<String, MarkdownInlineView>,
        markdownTheme: MarkdownTheme,
        actionHandler: ActionHandler?,
        indentLevel: Int,
        isShowNotSupported: Boolean,
        renderRegistry: RenderRegistry,
        nodeStringBuilderContext: NodeStringBuilderContext,
    )
}
```

**Parameters**

- `node`: The inline AST node.
- `inlineContentMap`: Mutable map for registering rich inline content (key -> `MarkdownInlineView`).
- `markdownTheme`: Current theme for reading styles.
- `actionHandler`: Optional interaction handler for links, etc.
- `renderRegistry`: Used for recursively building child node strings.
- `nodeStringBuilderContext`: Context providing text measurement, density, clipboard, etc.

**Helper class:**

```kotlin
open class CompositeChildNodeStringBuilder : IInlineNodeStringBuilder<Node> {
    open fun getSpanStyle(node: Node, markdownTheme: MarkdownTheme): SpanStyle? = null
    open fun getParagraphStyle(node: Node, markdownTheme: MarkdownTheme): ParagraphStyle? = null
}
```

---

### MarkdownInlineView

Represents inline composable content that can be embedded within text.

```kotlin
sealed interface MarkdownInlineView {
    data class MarkdownRichTextInlineContent(
        val inlineContent: RichTextInlineContent,
    ) : MarkdownInlineView
}
```

`RichTextInlineContent` has two variants:

- **`EmbeddedRichTextInlineContent`** -- Small inline elements (icons, badges) that sit within the text flow.
- **`StandaloneInlineContent`** -- Full-width block elements (cards, media) rendered as separate sections.

---

### RenderRegistry & Core Renderers

`RenderRegistry` is built during `MarkdownRenderConfig.Builder.build()` and determines how each node type is rendered.

```kotlin
data class RenderRegistry(
    private val blockRenderers: Map<KClass<out Node>, IBlockRenderer<*>>,
    private val inlineNodeStringBuilders: Map<KClass<out Node>, IInlineNodeStringBuilder<*>>,
    val markdownContentRenderer: MarkdownContentRenderer? = null,
    val markdownTextRenderer: MarkdownTextRenderer? = null,
) {
    fun getBlockRenderer(nodeClass: KClass<out Node>): IBlockRenderer<*>?
    fun getInlineNodeStringBuilder(nodeClass: KClass<out Node>): IInlineNodeStringBuilder<*>?
}
```

You typically interact with it indirectly via `Builder.addBlockRenderer(...)` and `Builder.addInlineNodeStringBuilder(...)`.

**Custom renderer interfaces:**

```kotlin
fun interface MarkdownContentRenderer {
    @Composable
    operator fun invoke(node: Node, modifier: Modifier)
}

fun interface MarkdownTextRenderer {
    @Composable
    operator fun invoke(
        parent: Node,
        modifier: Modifier,
        textAlign: TextAlign,
        textStyle: TextStyle?,
    )
}
```

---

## Plugin Modules

### TableMarkdownPlugin

Provides GFM table rendering support. Uses `commonmark-kotlin-ext-gfm-tables` for parsing.

**Module:** `markdown-multiplatform-table`

```kotlin
class TableMarkdownPlugin(
    private val tableTheme: TableTheme = TableTheme(),
) : IMarkdownRenderPlugin
```

**TableTheme:**

```kotlin
data class TableTheme(
    val borderColor: Color = Color.Gray,
    val borderThickness: Dp = 1.dp,
    val titleBackgroundColor: Color = Color.LightGray,
    val tableHeaderBackgroundColor: Color = Color.White,
    val tableCellBackgroundColor: Color = Color.White,
    val cellTextStyle: TextStyle? = null,
    val headerTextStyle: TextStyle? = TextStyle(fontWeight = FontWeight.Bold),
    val copyTextStyle: TextStyle = TextStyle(fontSize = 12.sp, color = Color.Black),
    val shape: Shape = RoundedCornerShape(8.dp),
    val cellPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
)
```

**Custom renderers:**

```kotlin
fun interface TableWidgetRenderer {
    @Composable
    operator fun invoke(node: Node, modifier: Modifier)
}

class TableRenderer(
    private val tableTheme: TableTheme = TableTheme(),
    tableTitleRenderer: TableWidgetRenderer? = null,
    tableCellRenderer: TableWidgetRenderer? = null,
) : IBlockRenderer<TableBlock>
```

**Example:**

```kotlin
// Default
val config = MarkdownRenderConfig.Builder()
    .addPlugin(TableMarkdownPlugin())
    .build()

// Custom theme
val config = MarkdownRenderConfig.Builder()
    .addPlugin(TableMarkdownPlugin(
        tableTheme = TableTheme(
            borderColor = Color.Blue,
            borderThickness = 2.dp,
        )
    ))
    .build()
```

---

### ImageMarkdownPlugin

Provides Markdown image rendering support.

**Module:** `markdown-multiplatform-image`

```kotlin
class ImageMarkdownPlugin(
    private val imageTheme: ImageTheme = ImageTheme(),
    private val loadingView: ImageWidgetRenderer = LoadingImageWidgetRenderer(),
    errorView: ImageWidgetRenderer? = null,
) : IMarkdownRenderPlugin
```

**ImageTheme:**

```kotlin
@Immutable
data class ImageTheme(
    val alignment: Alignment = Alignment.Center,
    val contentScale: ContentScale = ContentScale.Inside,
    val shape: Shape = RoundedCornerShape(8.dp),
    val modifier: Modifier = Modifier,
    val errorPlaceholderColor: Color = Color(0xFFE0E0E0),
)
```

**Custom loading/error views:**

```kotlin
fun interface ImageWidgetRenderer {
    @Composable
    operator fun invoke(
        url: String,
        contentDescription: String?,
        node: Node,
        modifier: Modifier,
    )
}
```

**Example:**

```kotlin
val config = MarkdownRenderConfig.Builder()
    .addPlugin(ImageMarkdownPlugin(
        imageTheme = ImageTheme(
            contentScale = ContentScale.Crop,
            shape = RoundedCornerShape(16.dp),
        )
    ))
    .build()
```

---

### HtmlMarkdownPlugin

Provides HTML inline tag support within Markdown content.

**Module:** `markdown-multiplatform-html`

```kotlin
class HtmlMarkdownPlugin(
    customTagHandlers: List<HtmlInlineTagHandler> = emptyList(),
) : IMarkdownRenderPlugin
```

**Default supported tags:** `<b>`, `<strong>`, `<i>`, `<em>`, `<u>`, `<ins>`, `<del>`, `<s>`, `<strike>`, `<a>`, `<span>` (with inline CSS style support).

**Custom tag handler interface:**

```kotlin
interface HtmlInlineTagHandler {
    val tagNames: Set<String>

    fun onOpenTag(
        tagName: String,
        rawTag: String,
        builder: AnnotatedString.Builder,
        context: HtmlInlineTagContext,
    )

    fun onCloseTag(
        tagName: String,
        builder: AnnotatedString.Builder,
        context: HtmlInlineTagContext,
    ) {
        builder.pop()
    }
}
```

**HtmlInlineTagContext:**

```kotlin
data class HtmlInlineTagContext(
    val node: Node,
    val inlineContentMap: MutableMap<String, MarkdownInlineView>,
    val markdownTheme: MarkdownTheme,
    val actionHandler: ActionHandler?,
    val indentLevel: Int,
    val isShowNotSupported: Boolean,
    val renderRegistry: RenderRegistry,
    val nodeStringBuilderContext: NodeStringBuilderContext,
)
```

**Example:**

```kotlin
// Default
val config = MarkdownRenderConfig.Builder()
    .addPlugin(HtmlMarkdownPlugin())
    .build()

// Custom <mark> tag handler
class MarkTagHandler : HtmlInlineTagHandler {
    override val tagNames = setOf("mark")

    override fun onOpenTag(
        tagName: String,
        rawTag: String,
        builder: AnnotatedString.Builder,
        context: HtmlInlineTagContext,
    ) {
        builder.pushStyle(SpanStyle(background = Color.Yellow))
    }
}

val config = MarkdownRenderConfig.Builder()
    .addPlugin(HtmlMarkdownPlugin(customTagHandlers = listOf(MarkTagHandler())))
    .build()
```

---

## Other APIs

### ActionHandler

Interface for handling user interactions within rendered Markdown content.

```kotlin
interface ActionHandler {
    fun handleUrlClick(url: String, node: Node) {}
    fun handleCopyClick(node: Node) {}
    fun handleImageClick(imageUrl: String, node: Node) {}
    fun handleCustomEvent(event: CustomEvent, node: Node) {}
}

interface CustomEvent
```

**Example:**

```kotlin
val handler = object : ActionHandler {
    override fun handleUrlClick(url: String, node: Node) {
        // Open URL in browser
    }
    override fun handleCopyClick(node: Node) {
        // Copy code block content
    }
}

MarkdownView(
    text = markdownContent,
    actionHandler = handler,
)
```

---

### NodeStringBuilderContext

Provides context for inline node string builders, including text measurement, styles, and system capabilities.

```kotlin
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
```

---

### Composition Local Accessors

Convenience functions for accessing current rendering context within custom renderers:

```kotlin
@Composable @ReadOnlyComposable fun currentTheme(): MarkdownTheme
@Composable @ReadOnlyComposable fun currentParser(): MarkdownParser
@Composable @ReadOnlyComposable fun currentRenderRegistry(): RenderRegistry
@Composable @ReadOnlyComposable fun currentActionHandler(): ActionHandler?
@Composable @ReadOnlyComposable fun isShowNotSupported(): Boolean
```

---

## Common Usage Patterns

### Small to Medium Markdown Content

- Use `MarkdownView` with default configuration.

```kotlin
MarkdownView(text = markdownContent)
```

### Large Scrollable Documents

- Use `LazyMarkdownColumn` for `LazyColumn`-based scrolling.

```kotlin
LazyMarkdownColumn(
    text = longContent,
    markdownRenderConfig = config,
    modifier = Modifier.fillMaxSize(),
)
```

### Full-Featured Rendering

- Enable multiple plugins for tables, images, and HTML support.

```kotlin
val config = MarkdownRenderConfig.Builder()
    .addPlugin(TableMarkdownPlugin())
    .addPlugin(ImageMarkdownPlugin())
    .addPlugin(HtmlMarkdownPlugin())
    .build()

MarkdownView(
    text = richContent,
    markdownRenderConfig = config,
)
```

### Advanced Customization

- Create a shared `MarkdownRenderConfig`:
  - Set `markdownTheme` to match your design system.
  - Add plugins for extended syntax.
  - Register custom block renderers or inline builders as needed.
  - Use `addExtension()` for custom commonmark parser extensions.
