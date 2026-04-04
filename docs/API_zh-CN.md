# ComposeMarkdownMultiplatform API 参考

[English](API.md) | [简体中文](API_zh-CN.md)

> 本文是 `markdown-multiplatform` 模块中核心可组合函数（composable）和配置类型的详细参考文档。
>
> - 关于整体介绍、安装与特性说明，请查看 [README](../README_zh-CN.md)。

## 目录

- [核心 Composable](#核心-composable)
  - [MarkdownView](#markdownview)
  - [LazyMarkdownColumn](#lazymarkdowncolumn)
  - [MarkdownContent](#markdowncontent)
  - [MarkdownChildren](#markdownchildren)
  - [MarkdownInlineText](#markdowninlinetext)
  - [MarkdownText](#markdowntext-1)
- [配置](#配置)
  - [MarkdownRenderConfig](#markdownrenderconfig)
  - [MarkdownRenderConfig.Builder](#markdownrenderconfigbuilder)
  - [MarkdownTheme](#markdowntheme)
- [插件与扩展点](#插件与扩展点)
  - [IMarkdownRenderPlugin](#imarkdownrenderplugin)
  - [IBlockRenderer](#iblockrenderer)
  - [IInlineNodeStringBuilder](#iinlinenodestringbuilder)
  - [MarkdownInlineView](#markdowninlineview)
  - [RenderRegistry 与核心渲染器](#renderregistry-与核心渲染器)
- [插件模块](#插件模块)
  - [TableMarkdownPlugin](#tablemarkdownplugin)
  - [ImageMarkdownPlugin](#imagemarkdownplugin)
  - [HtmlMarkdownPlugin](#htmlmarkdownplugin)
- [其他 API](#其他-api)
  - [ActionHandler](#actionhandler)
  - [NodeStringBuilderContext](#nodestringbuildercontext)
  - [Composition Local 访问函数](#composition-local-访问函数)
- [常见使用模式](#常见使用模式)

---

## 核心 Composable

### MarkdownView

渲染 Markdown 内容的主要入口。解析并渲染 Markdown 字符串。

**函数签名**（来自 `MarkdownView.kt`）：

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

**参数说明**

- `text`：要渲染的 Markdown 文本。
- `modifier`：标准 Compose `Modifier`，用于尺寸、内边距等设置。
- `markdownRenderConfig`：渲染配置，通过 `MarkdownRenderConfig.Builder()` 创建。省略时使用默认配置。
- `actionHandler`：可选的交互处理器（链接、图片、自定义事件等）。
- `showNotSupported`：当为 `true` 时，不支持的元素会以文本回退方式显示，而不是静默忽略。

**示例**

```kotlin
@Composable
fun SimpleExample() {
    MarkdownView(
        text = "# Hello\n\n这是 **Markdown** 内容。",
        modifier = Modifier.fillMaxSize(),
    )
}
```

---

### LazyMarkdownColumn

使用 `LazyColumn` 高效渲染大型 Markdown 文档。每个顶层块作为一个独立的 lazy item。

**函数签名**（来自 `LazyMarkdownColumn.kt`）：

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

**参数说明**

- `text`：要渲染的 Markdown 文本。
- `markdownRenderConfig`：渲染配置。
- `actionHandler`：可选的交互处理器。
- `showNotSupported`：是否对不支持的元素显示文本提示。
- `lazyListState`：可选的 `LazyListState`，用于外部滚动控制。

**示例**

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

渲染单个已解析的 AST 节点，并分发到相应的块渲染器。

**函数签名**（来自 `MarkdownContent.kt`）：

```kotlin
@Composable
fun MarkdownContent(
    node: Node,
    modifier: Modifier = Modifier,
)
```

通常在内部使用，或在自定义 `IBlockRenderer` 实现中用于递归渲染子节点。

---

### MarkdownChildren

用于遍历并渲染父节点所有子节点的实用 Composable，自动处理间距逻辑。

**函数签名**（来自 `MarkdownContent.kt`）：

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

**参数说明**

- `parent`：需要渲染子节点的 `Node` 对象。
- `children`：要渲染的子节点列表（默认为 `parent` 的子节点）。
- `spacerHeight`：子节点间的垂直间距。默认为 `theme.spacerTheme.spacerHeight`。
- `showSpacer`：是否插入间距。默认为 `theme.spacerTheme.showSpacer`。
- `onBeforeChild` / `onAfterChild`：可选的 Composable 回调，在渲染每个子节点前/后调用。
- `onBeforeAll` / `onAfterAll`：可选的 Composable 回调，在渲染所有子节点之前/之后调用。

**适用场景**

当你实现自定义的 `IBlockRenderer`（例如自定义容器块）并需要以标准的间距规则渲染嵌套内容时使用。

---

### MarkdownInlineText

将块节点的内联子节点渲染为带样式的文本（使用 `AnnotatedString`）。

**函数签名**（来自 `MarkdownInlineText.kt`）：

```kotlin
@Composable
fun MarkdownInlineText(
    parent: Node,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    textStyle: TextStyle? = null,
)
```

**参数说明**

- `parent`：需要渲染内联子节点的块节点。
- `textAlign`：文本对齐方式。
- `textStyle`：覆盖文本样式（默认使用主题样式）。

---

### MarkdownText

基于文本的渲染方式，将整个 Markdown 文档通过单个 `RichText` 可组合函数渲染，支持跨段落文本选择。与 `MarkdownView` 将每个块作为 `Column` 中独立的可组合函数不同，`MarkdownText` 将文本块合并为单个 `AnnotatedString`，并将非文本块（代码块、引用块、列表等）作为内联内容嵌入。

**函数签名**（来自 `MarkdownText.kt`）：

```kotlin
@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    markdownRenderConfig: MarkdownRenderConfig = remember { MarkdownRenderConfig.Builder().build() },
    actionHandler: ActionHandler? = null,
    showNotSupported: Boolean = false,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    onTextLayout: (TextLayoutResult) -> Unit = {},
)
```

**参数说明**

- `text`：要解析和渲染的 Markdown 字符串。
- `markdownRenderConfig`：解析和渲染的配置。
- `actionHandler`：可选的链接点击和其他操作处理器。
- `showNotSupported`：是否显示不支持元素的文本。
- `overflow`：视觉溢出处理方式（`TextOverflow.Clip`、`Ellipsis` 等）。
- `softWrap`：是否在软换行处断行。
- `textAlign`：文本对齐方式。
- `maxLines` / `minLines`：渲染文本的行数约束。
- `letterSpacing`：字间距。
- `textDecoration`：文本装饰（下划线、删除线）。
- `onTextLayout`：文本布局后的回调，返回 `TextLayoutResult`。

**示例**

```kotlin
SelectionContainer {
    MarkdownText(
        text = markdownContent,
        markdownRenderConfig = config,
        modifier = Modifier.padding(16.dp),
        maxLines = 10,
        overflow = TextOverflow.Ellipsis,
    )
}
```

---

## 配置

### MarkdownRenderConfig

`MarkdownRenderConfig` 包含解析和渲染 Markdown 所需的一切：

- 一个 `MarkdownTheme`，描述排版、颜色和组件样式。
- 一个 `MarkdownParser`（基于 `commonmark-kotlin`）。
- 一个 `RenderRegistry`，描述如何渲染各类节点。

实例通过 `MarkdownRenderConfig.Builder` 创建：

```kotlin
val config = MarkdownRenderConfig.Builder()
    // 配置主题、插件、渲染器等...
    .build()
```

---

### MarkdownRenderConfig.Builder

用于以流式 API 自定义解析、主题和渲染行为的构建器。

**关键方法**（来自 `MarkdownRenderConfig.kt`）：

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

设置 Markdown 渲染的视觉主题。如果未设置，使用默认的 `MarkdownTheme()`。

#### addPlugin(plugin: IMarkdownRenderPlugin)

注册渲染插件。插件可以提供自定义解析器扩展、块渲染器和内联字符串构建器。

#### addInlineNodeStringBuilder / addBlockRenderer

自定义特定节点类型渲染逻辑的底层钩子：

- `addInlineNodeStringBuilder(nodeClass, builder)`：定义某个内联节点类型如何被转换为文本 span。
- `addBlockRenderer(nodeClass, renderer)`：定义某个块节点类型如何被渲染为 Compose UI。

#### addExtension(extension: Extension)

直接注册 commonmark 解析器扩展（例如 `TablesExtension.create()`）。通过插件注册的扩展也会被自动收集。

#### markdownTextRenderer / markdownContentRenderer

高级自定义入口：

- `markdownTextRenderer`：重写文本节点的渲染方式。
- `markdownContentRenderer`：重写内容节点的渲染方式。

---

### MarkdownTheme

`MarkdownTheme` 是控制 Markdown 内容在 Compose 中如何呈现的**核心主题模型**。

#### 数据结构

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
        // ... 到 HEAD6
    }
}
```

#### 标题级别

标题通过 `headStyle` 映射配置。key 为 1-6 的整数，通过常量暴露：

```kotlin
MarkdownTheme.HEAD1 // H1
MarkdownTheme.HEAD2 // H2
// ... 到 HEAD6
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

**综合示例**

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

## 插件与扩展点

### IMarkdownRenderPlugin

向 Markdown 引擎添加功能的入口。

```kotlin
interface IMarkdownRenderPlugin {
    fun parserExtensions(): List<Extension> = emptyList()
    fun blockRenderers(): Map<KClass<out Node>, IBlockRenderer<*>> = emptyMap()
    fun inlineNodeStringBuilders(): Map<KClass<out Node>, IInlineNodeStringBuilder<*>> = emptyMap()
}
```

插件可以：

- 通过 `parserExtensions()` 提供自定义解析器扩展（例如 `TablesExtension.create()` 用于 GFM 表格）。
- 注册块渲染器和内联字符串构建器。

插件通过 `MarkdownRenderConfig.Builder.addPlugin()` 添加。

也可以继承 `AbstractMarkdownRenderPlugin` 简化实现：

```kotlin
abstract class AbstractMarkdownRenderPlugin : IMarkdownRenderPlugin {
    override fun parserExtensions(): List<Extension> = emptyList()
    override fun blockRenderers(): Map<KClass<out Node>, IBlockRenderer<*>> = emptyMap()
    override fun inlineNodeStringBuilders(): Map<KClass<out Node>, IInlineNodeStringBuilder<*>> = emptyMap()
}
```

---

### IBlockRenderer

负责将特定的块节点渲染为 Compose UI。

```kotlin
interface IBlockRenderer<T : Node> {
    @Composable
    fun Invoke(
        node: T,
        modifier: Modifier,
    )
}
```

**参数说明**

- `node`：要渲染的 commonmark AST 节点。
- `modifier`：来自父布局的 Modifier。实现时应优先应用该 modifier 以保持布局一致性。

**实现建议**

- 使用 `MarkdownChildren` 渲染嵌套子节点并保持标准间距。
- 通过 `currentTheme()` 访问当前主题。
- 通过 `currentActionHandler()` 访问交互处理器。

---

### IInlineNodeStringBuilder

负责将内联节点转换为 `AnnotatedString` 中的带样式文本 span。

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

**参数说明**

- `node`：内联 AST 节点。
- `inlineContentMap`：用于注册富内联内容的可变 Map（key -> `MarkdownInlineView`）。
- `markdownTheme`：当前主题，用于读取样式。
- `actionHandler`：可选的交互处理器。
- `renderRegistry`：用于递归构建子节点字符串。
- `nodeStringBuilderContext`：提供文本测量、密度、剪贴板等上下文。

**辅助类：**

```kotlin
open class CompositeChildNodeStringBuilder : IInlineNodeStringBuilder<Node> {
    open fun getSpanStyle(node: Node, markdownTheme: MarkdownTheme): SpanStyle? = null
    open fun getParagraphStyle(node: Node, markdownTheme: MarkdownTheme): ParagraphStyle? = null
}
```

---

### MarkdownInlineView

表示可以嵌入文本中的内联 Composable 内容。

```kotlin
sealed interface MarkdownInlineView {
    data class MarkdownRichTextInlineContent(
        val inlineContent: RichTextInlineContent,
    ) : MarkdownInlineView
}
```

`RichTextInlineContent` 有两种形态：

- **`EmbeddedRichTextInlineContent`** -- 小型内联元素（图标、徽章），与文本处于同一行。
- **`StandaloneInlineContent`** -- 全宽块级元素（卡片、媒体），作为独立段落渲染。

---

### RenderRegistry 与核心渲染器

`RenderRegistry` 在 `MarkdownRenderConfig.Builder.build()` 中构建，决定每个节点类型如何渲染。

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

通常通过 `Builder.addBlockRenderer(...)` 和 `Builder.addInlineNodeStringBuilder(...)` 间接操作。

**自定义渲染器接口：**

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

## 插件模块

### TableMarkdownPlugin

提供 GFM 表格渲染支持。使用 `commonmark-kotlin-ext-gfm-tables` 进行解析。

**模块：** `markdown-multiplatform-table`

```kotlin
class TableMarkdownPlugin(
    private val tableTheme: TableTheme = TableTheme(),
) : IMarkdownRenderPlugin
```

**TableTheme：**

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

**自定义渲染器：**

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

**示例：**

```kotlin
// 默认配置
val config = MarkdownRenderConfig.Builder()
    .addPlugin(TableMarkdownPlugin())
    .build()

// 自定义主题
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

提供 Markdown 图片渲染支持。

**模块：** `markdown-multiplatform-image`

```kotlin
class ImageMarkdownPlugin(
    private val imageTheme: ImageTheme = ImageTheme(),
    private val loadingView: ImageWidgetRenderer = LoadingImageWidgetRenderer(),
    errorView: ImageWidgetRenderer? = null,
) : IMarkdownRenderPlugin
```

**ImageTheme：**

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

**自定义加载/错误视图：**

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

**示例：**

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

提供 Markdown 中 HTML 内联标签的支持。

**模块：** `markdown-multiplatform-html`

```kotlin
class HtmlMarkdownPlugin(
    customTagHandlers: List<HtmlInlineTagHandler> = emptyList(),
) : IMarkdownRenderPlugin
```

**默认支持的标签：** `<b>`、`<strong>`、`<i>`、`<em>`、`<u>`、`<ins>`、`<del>`、`<s>`、`<strike>`、`<a>`、`<span>`（支持内联 CSS 样式）。

**自定义标签处理器接口：**

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

**HtmlInlineTagContext：**

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

**示例：**

```kotlin
// 默认配置
val config = MarkdownRenderConfig.Builder()
    .addPlugin(HtmlMarkdownPlugin())
    .build()

// 自定义 <mark> 标签处理器
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

## 其他 API

### ActionHandler

用于处理渲染后 Markdown 内容中用户交互的接口。

```kotlin
interface ActionHandler {
    fun handleUrlClick(url: String, node: Node) {}
    fun handleCopyClick(node: Node) {}
    fun handleImageClick(imageUrl: String, node: Node) {}
    fun handleCustomEvent(event: CustomEvent, node: Node) {}
}

interface CustomEvent
```

**示例：**

```kotlin
val handler = object : ActionHandler {
    override fun handleUrlClick(url: String, node: Node) {
        // 在浏览器中打开 URL
    }
    override fun handleCopyClick(node: Node) {
        // 复制代码块内容
    }
}

MarkdownView(
    text = markdownContent,
    actionHandler = handler,
)
```

---

### NodeStringBuilderContext

为内联节点字符串构建器提供上下文，包括文本测量、样式和系统能力。

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

### Composition Local 访问函数

在自定义渲染器内部访问当前渲染上下文的便捷函数：

```kotlin
@Composable @ReadOnlyComposable fun currentTheme(): MarkdownTheme
@Composable @ReadOnlyComposable fun currentParser(): MarkdownParser
@Composable @ReadOnlyComposable fun currentRenderRegistry(): RenderRegistry
@Composable @ReadOnlyComposable fun currentActionHandler(): ActionHandler?
@Composable @ReadOnlyComposable fun isShowNotSupported(): Boolean
```

---

## 常见使用模式

### 小型 / 中型 Markdown 内容

- 使用 `MarkdownView` 配合默认配置。

```kotlin
MarkdownView(text = markdownContent)
```

### 大型可滚动文档

- 使用 `LazyMarkdownColumn` 进行基于 `LazyColumn` 的滚动。

```kotlin
LazyMarkdownColumn(
    text = longContent,
    markdownRenderConfig = config,
    modifier = Modifier.fillMaxSize(),
)
```

### 全功能渲染

- 启用多个插件以支持表格、图片和 HTML。

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

### 高级自定义

- 创建可共享的 `MarkdownRenderConfig`：
  - 设置 `markdownTheme` 以匹配你的设计体系。
  - 添加插件以支持扩展语法。
  - 按需注册自定义块渲染器或内联构建器。
  - 使用 `addExtension()` 注册自定义 commonmark 解析器扩展。
