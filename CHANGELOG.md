# Changelog

## 0.1.9

### New Features

#### Syntax Highlighting (`BasicSyntaxHighlighter`)
Added `BasicSyntaxHighlighter`, a built-in regex-based implementation of `CodeAnnotator` that applies token-level syntax highlighting to code blocks.
- Supports Kotlin, Java, Python, JavaScript, TypeScript, Go, Rust, C/C++, Swift, and more
- Six configurable token categories: keyword, string, comment, number, annotation, type
- `CodeColors` data class added to the `style` package for per-token color customization
- `codeColors` field added to `CodeBlockTheme` for theme-level color configuration
- `CodeBlockRenderer` defaults to `BasicSyntaxHighlighter()`; pass `null` to disable highlighting

#### List Marker Alignment (`MarkerAlignment`)
Added `MarkerAlignment` enum to `ListTheme` for controlling the vertical alignment of list markers relative to the first line of content.
- Options: `Top`, `Center`, `Bottom`, `Baseline` (default)
- Uses `FirstLineMetrics` for precise baseline-relative positioning

### Improvements

#### List Marker Rendering
Replaced the `Row + Text + Spacer` layout for list markers with a custom `Layout + drawBehind` canvas approach. Markers no longer participate in text selection, eliminating spurious newlines when copying list content.

#### `IBlockRenderer.supportTextMode()`
Added `supportTextMode()` default method to `IBlockRenderer`. Renderers returning `false` are excluded from `RenderRegistry.textModeRegistry()`, giving fine-grained control over which blocks render in text mode.

#### Text-Mode Spacer Handling
`DocumentInlineStringBuilder` now respects the `showSpacer` theme setting when building text-mode output, preventing unwanted blank lines in single-string rendering.

---

## 更新日志

## 0.1.9

### 新增功能

#### 语法高亮（`BasicSyntaxHighlighter`）
新增内置正则表达式语法高亮器 `BasicSyntaxHighlighter`，实现 `CodeAnnotator` 接口，对代码块进行 Token 级别着色。
- 支持 Kotlin、Java、Python、JavaScript、TypeScript、Go、Rust、C/C++、Swift 等语言
- 六种可配置 Token 类型：关键字、字符串、注释、数字、注解、类型
- `style` 包新增 `CodeColors` 数据类，用于自定义各 Token 颜色
- `CodeBlockTheme` 新增 `codeColors` 字段，支持主题层面统一配置颜色
- `CodeBlockRenderer` 默认使用 `BasicSyntaxHighlighter()`，传入 `null` 可禁用高亮

#### 列表标记对齐（`MarkerAlignment`）
`ListTheme` 新增 `MarkerAlignment` 枚举，控制列表标记相对于内容首行的垂直对齐方式。
- 可选值：`Top`、`Center`、`Bottom`、`Baseline`（默认）
- 使用 `FirstLineMetrics` 实现精确的基线对齐定位

### 改进

#### 列表标记渲染
将列表标记的 `Row + Text + Spacer` 布局替换为 `Layout + drawBehind` Canvas 绘制方案。标记不再参与文字选择，彻底消除复制列表内容时产生的多余换行符。

#### `IBlockRenderer.supportTextMode()`
`IBlockRenderer` 接口新增 `supportTextMode()` 默认方法。返回 `false` 的渲染器将从 `RenderRegistry.textModeRegistry()` 中排除，可精细控制哪些块参与文本模式渲染。

#### 文本模式 Spacer 处理
`DocumentInlineStringBuilder` 在构建文本模式输出时现遵循主题的 `showSpacer` 设置，避免单字符串渲染时产生多余空行。

---

## 0.1.8

### Bug Fixes

#### `AutoLineHeightText` Bounds Check
- Added guard check for `startIndex < contentEnd` in `buildAdjustLineHeightText`
  to prevent `IllegalArgumentException` when trailing newline causes `contentEnd`
  to equal or precede `startIndex` (e.g. placeholder alternateText containing `\n`)

---

## 更新日志

## 0.1.8

### Bug 修复

#### `AutoLineHeightText` 边界检查
- 在 `buildAdjustLineHeightText` 中增加 `startIndex < contentEnd` 的边界检查，
  防止尾部换行符导致 `contentEnd` 等于或小于 `startIndex` 时抛出
  `IllegalArgumentException`（例如 placeholder 的 alternateText 包含 `\n` 的情况）

---

## 0.1.7

### New Features

#### `MarkdownText` Component
A new `MarkdownText` composable that complements the existing `MarkdownView`.
Instead of rendering each block as a separate composable in a Column, it renders
the entire Markdown document through a single `RichText` composable.
- **Supports `maxLines` and `overflow`** — ideal for message previews, list summaries,
  and any scenario that requires line-limited rendering
- Enables cross-paragraph text selection
- Non-text blocks (code blocks, block quotes, lists, etc.) are embedded as InlineContent
  using existing `IBlockRenderer` implementations

```kotlin
MarkdownText(
    text = markdownString,
    maxLines = 3,
    overflow = TextOverflow.Ellipsis
)
```

#### `IBlockRenderer.shouldSkipRender()`
Added `shouldSkipRender(node)` to the `IBlockRenderer` interface (defaults to `false`).
When it returns `true`, the block and its surrounding spacers are skipped entirely,
allowing conditional filtering of specific blocks.

#### HtmlBlock Rendering
Added `HtmlMarkdownPlugin` with `HtmlBlockRenderer` for rendering raw HTML blocks.
Upgraded `commonmark-kotlin` to 0.0.2.

#### Autolink & Task List Plugins
Added multiplatform plugins for autolink detection and task list (checkbox) rendering.

### Bug Fixes

#### `AutoLineHeightText` Recomposition Stability
- Fixed `AnnotatedString` equality check: excluded `LinkAnnotation` listeners that
  change on every recomposition, which caused the line height adjustment calculation to be skipped
- Fixed trailing newline removal logic: replaced unconditional removal with a binary
  search check to avoid truncating `ParagraphStyle` annotations

### Internal Changes

- Renamed `MarkdownText` → `MarkdownInlineText` to clarify its role as an internal
  inline text renderer (not a breaking change for library consumers)

---

## 更新日志

## 0.1.7

### 新增功能

#### `MarkdownText` 组件
新增 `MarkdownText` 组件，作为现有 `MarkdownView` 的补充。将整个 Markdown 文档通过
单个 `RichText` 渲染，段落和标题合并为一个 `AnnotatedString`。
- **支持 `maxLines` 和 `overflow`**，适用于消息预览、列表摘要等需要行数限制的场景
- 支持跨段落文本选择
- 非文本块（代码块、引用、列表等）以 InlineContent 嵌入，复用现有 `IBlockRenderer` 实现

```kotlin
MarkdownText(
    text = markdownString,
    maxLines = 3,
    overflow = TextOverflow.Ellipsis
)
```

#### `IBlockRenderer.shouldSkipRender()`
`IBlockRenderer` 接口新增 `shouldSkipRender(node)` 方法（默认 `false`）。
返回 `true` 时该块及其前后间距将被完全跳过，方便按条件过滤特定块。

#### HtmlBlock 渲染
新增 `HtmlMarkdownPlugin`，支持渲染原始 HTML 块。升级 `commonmark-kotlin` 至 0.0.2。

#### Autolink 与 Task List 插件
新增多平台 autolink 自动链接检测和 task list（复选框）渲染插件。

### Bug 修复

#### `AutoLineHeightText` 重组稳定性
- 修复 `AnnotatedString` 相等性判断：排除每次重组都会变化的 `LinkAnnotation` 回调，该问题会导致行高调整计算被跳过
- 修复段落尾部换行符移除逻辑，改用二分查找精确判断是否会截断 `ParagraphStyle` 注解

### 内部变更

- 将 `MarkdownText` 重命名为 `MarkdownInlineText`，明确其作为内部 inline 文本渲染器的角色（不影响库使用者）
