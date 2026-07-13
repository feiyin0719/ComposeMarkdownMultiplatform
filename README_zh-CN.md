# Compose Markdown Multiplatform

[English](README.md) | [简体中文](README_zh-CN.md)

一个 Compose Multiplatform Markdown 渲染库，支持 Android、iOS、Desktop (JVM) 和 WebAssembly (Wasm)。

> **如果只需要 Android 平台和更好的 Markdown 兼容性？**
> 请使用 [ComposeMarkdown](https://github.com/feiyin0719/ComposeMarkdown) — 它基于 Flexmark 解析引擎，拥有更深入的 Markdown 规范支持和更丰富的渲染特性。

## 示例截图

| Desktop | Android | WebAssembly (Wasm) |
| :---: | :---: | :---: |
| ![Desktop](images/desktop.png) | ![Android](images/android.png) | ![WasmJS](images/wasmJS.png) |

## 特性

- **Kotlin Multiplatform** — 一套代码同时支持 Android、iOS、Desktop 和 Web
- **Compose Multiplatform** — 基于 JetBrains Compose Multiplatform 构建
- **CommonMark 支持** — 使用 `commonmark-kotlin` 解析器（纯 Kotlin Multiplatform）
- **插件系统** — 模块化的插件架构，支持表格、图片、HTML 等扩展；支持自定义解析器扩展
- **可定制主题** — 完全控制排版、颜色和组件样式
- **MarkdownText** — 基于文本的渲染模式，像 Compose `Text` 一样支持 `maxLines` / `overflow` 行数限制和跨段落文本选择
- **LazyMarkdownView** — 为大型数据源提供增量行读取、解析和有界 AST 回收
- **异步解析** — `MarkdownView` 与 `MarkdownText` 重载可指定解析 dispatcher，并提供 loading/error 内容
- **Streaming 解析** — append-only 更新只重新解析旧尾 block，结束时再执行一次全量解析

## 支持的平台

| 平台 | 状态 |
| :---: | :---: |
| Android | 已支持 |
| iOS (arm64, x64, simulator) | 已支持 |
| Desktop (JVM) | 已支持 |
| WebAssembly (Wasm) | 已支持 |

## 安装

### 系统要求

- **Kotlin**：2.0.21+
- **Compose Multiplatform**：最新版本
- **Android API**：24+（Android 7.0）
- **Java**：11+

### 添加依赖

在项目的 `build.gradle.kts` 中添加依赖：

```kotlin
// 在共享模块的 build.gradle.kts 中
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.feiyin0719:markdown-multiplatform:<version>")
            }
        }
    }
}
```

### 插件模块

| 插件 | Artifact | 描述 |
|--------|----------|-------------|
| 表格 | `markdown-multiplatform-table` | GFM 表格支持 |
| 图片 | `markdown-multiplatform-image` | Markdown 图片渲染 |
| HTML | `markdown-multiplatform-html` | HTML 内联标签支持 |

```kotlin
dependencies {
    implementation("io.github.feiyin0719:markdown-multiplatform-table:<version>")
    implementation("io.github.feiyin0719:markdown-multiplatform-image:<version>")
    implementation("io.github.feiyin0719:markdown-multiplatform-html:<version>")
}
```

## 快速开始

```kotlin
import io.github.feiyin0719.markdown.multiplatform.MarkdownView

@Composable
fun SimpleMarkdownExample() {
    val markdownContent = """
        # Hello Compose Markdown Multiplatform

        这是一个**跨平台** Markdown 渲染库。

        - Android
        - iOS
        - Desktop
        - Web (Wasm)
    """.trimIndent()

    MarkdownView(
        content = markdownContent,
        modifier = Modifier.fillMaxSize(),
    )
}
```

### 增量加载大型文档

`LazyMarkdownView` 按需读取稳定的行数据源，并回收远离视口的 AST 节点。删除或重新加载节点时，
稳定的 lazy item key 会保持当前可见锚点，避免滚动位置抖动。
也可以直接传入 Markdown 字符串，方便与 Android API 使用同一份文本进行对比。

```kotlin
@Composable
fun LargeMarkdownExample(markdown: String) {
    val source = remember(markdown) { StringMarkdownLineSource(markdown) }

    LazyMarkdownView(
        source = source,
        modifier = Modifier.fillMaxSize(),
        chunkLoaderConfig = MarkdownChunkLoaderConfig(
            initialLineCount = 1000,
            incrementalLineCount = 500,
            minNodesAhead = 100,
            minNodesBehind = 30,
            maxCachedNodes = 500,
            maxCachedSourceLines = 10_000,
        ),
    )
}
```

如果需要真正的懒加载 I/O，可基于稳定的文件、asset、数据库或 range API 实现
`MarkdownLineSource`。向上滚动会重新读取已回收区间，因此数据源必须支持重读旧范围。
各 chunk 不共享 reference definition 状态；需要完整文档的 reference 解析时，请使用 inline link
或 `LazyMarkdownColumn`。

`onLoadingChanged` 只表示首屏尚无内容时的等待。需要观察后台向前/向后加载时，可使用
`onStateChanged` 和 `LazyMarkdownViewState`。AST 回收保持静默，内部并发使用独立操作锁。
`maxCachedSourceLines` 同时也是单个未确认尾部 block 或源码上下文的硬上限。

### Streaming 生成内容

当文本只在尾部追加时，在 `MarkdownView` 或 `MarkdownText` 中设置 `isStreaming = true`。
稳定前缀节点会被复用，仅重新解析旧文档最后一个 block。生成结束时设置
`isStreaming = false`，以强制执行一次最终全量解析。可传入自定义
`StreamingMarkdownParser`，并通过 `MarkdownRenderConfig.Builder.streamingMarkdownParserFactory`
替换默认流程；其 `parse` 方法只接收完整 content 与 streaming 状态，可自主控制整个解析生命周期。
factory 默认为 `null`，未配置时 streaming 请求使用普通全量解析。需显式配置
`::DefaultStreamingMarkdownParser` 才启用内置流程；该实现会创建自己的 parser，并保证至少包含
block source span。普通 parser 的 source span 可配置且默认是 `IncludeSourceSpans.BLOCKS`；
`LazyMarkdownView` 同样强制至少使用 `BLOCKS`。

```kotlin
MarkdownView(
    text = streamedMarkdown,
    parseDispatcher = Dispatchers.Default,
    isStreaming = streamInProgress,
)
```

## 技术栈

| 技术 | 作用 |
|------------|---------|
| **Compose Multiplatform** | 跨平台 UI 框架 |
| **commonmark-kotlin** | Markdown 解析引擎（纯 Kotlin Multiplatform） |
| **Kotlin Coroutines** | 异步处理 |
| **Material Design 3** | 设计语言规范 |

## API 参考

关于完整函数签名和详细参数说明，请参考专门的 API 文档：

- **完整 API 参考**：[docs/API_zh-CN.md](docs/API_zh-CN.md)

## 贡献指南

欢迎贡献代码！开始之前：

1. Fork 本仓库
2. 创建特性分支：`git checkout -b feat/my-feature`
3. 完成你的修改
4. 格式化代码：`./gradlew ktlintFormat`
5. 运行检查：`./gradlew ktlintCheck`
6. 构建：`./gradlew assemble`
7. 使用约定前缀提交（`feat:`、`fix:`、`docs:` 等）
8. 提交 Pull Request

## 许可证

本项目基于 MIT License 开源发布。详情请查看 [LICENSE](LICENSE)。

---

<div align="center">

**[回到顶部](#compose-markdown-multiplatform)**

由 Compose Markdown 团队用心打造

</div>
