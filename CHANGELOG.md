# Changelog

## 0.2.0

### New Features

#### Render Dependency Context
- Added `renderDependencies` to the public Markdown rendering components so callers can provide services and other external objects to custom renderers
- Added `currentRenderDependencies()` for Composable renderers and `NodeStringBuilderContext.renderDependencies` for node string builders

#### Stable Text-Mode Content Identity
- Replaced identity-only text-mode block keys with bounded, iterative content hashes
- Added `NodeContentHashProvider` so custom nodes can define the fields that affect their rendered identity

#### Shared Annotated Text Pipeline
- Added `rememberMarkdownAnnotatedStringResult()` as the shared text and inline-content builder for `MarkdownText` and `MarkdownInlineText`
- Text-mode registry overrides now flow through `ProvideMarkdownLocals`

### Bug Fixes

#### Placeholder Geometry Under Font Scaling
- Added a linear pixel-to-sp conversion for measured placeholder dimensions, preserving adaptive inline content geometry across font scales

### Performance Improvements

- Made `MarkdownRenderConfig` immutable, reused parser instances, and kept custom/default instances stable with `remember`
- Cached text-mode render registries and syntax-highlighted code results
- Avoided line-layout state and offset calculations when line numbers are hidden
- Reused RichText inline-content groups, adaptive entries, segment callbacks, and immutable measurement maps
- Cached static shimmer colors and shapes during loading animations

---

## 更新日志

## 0.2.0

### 新增功能

#### 渲染依赖上下文
- 所有公开 Markdown 渲染组件新增 `renderDependencies`，调用方可向自定义 renderer 提供服务或其他外部对象
- Composable renderer 可通过 `currentRenderDependencies()` 获取依赖，节点字符串构建器可通过 `NodeStringBuilderContext.renderDependencies` 获取依赖

#### 稳定的文本模式内容标识
- 将仅依赖对象 identity 的文本模式块 key 替换为有节点数上限的迭代式内容哈希
- 新增 `NodeContentHashProvider`，自定义节点可声明影响渲染标识的字段

#### 共享 AnnotatedString 构建流程
- 新增 `rememberMarkdownAnnotatedStringResult()`，供 `MarkdownText` 与 `MarkdownInlineText` 共用文本及内联内容构建流程
- 文本模式 registry 覆盖改为通过 `ProvideMarkdownLocals` 向下传递

### Bug 修复

#### 字体缩放下的 Placeholder 几何尺寸
- 新增用于测量型 placeholder 的线性 px-to-sp 转换，在不同字体缩放下保持 adaptive inline content 几何尺寸

### 性能优化

- 将 `MarkdownRenderConfig` 改为不可变对象、复用 parser 实例，并通过 `remember` 保持自定义及默认实例稳定
- 缓存文本模式 render registry 与语法高亮结果
- 关闭行号时不再维护文本布局状态或计算行偏移
- 复用 RichText 内联内容分组、adaptive entries、segment 回调与测量阶段 immutable map
- Loading 动画复用静态 shimmer colors 与 shape

---

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
