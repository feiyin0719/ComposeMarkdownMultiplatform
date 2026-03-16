# Compose Markdown Multiplatform

[English](README.md) | [简体中文](README_zh-CN.md)

A Compose Multiplatform Markdown rendering library that supports Android, iOS, Desktop (JVM), and WebAssembly (Wasm).

> **Looking for Android-only with richer Markdown compatibility?**
> Check out [ComposeMarkdown](https://github.com/niceFuture0723/ComposeMarkdown) — it offers deeper Markdown spec support (powered by Flexmark) and more rendering features for Android projects.

## Sample Screenshots

| Desktop | Android | WebAssembly (Wasm) |
| :---: | :---: | :---: |
| ![Desktop](images/desktop.png) | ![Android](images/android.png) | ![WasmJS](images/wasmJS.png) |

## Features

- **Kotlin Multiplatform** — Single codebase for Android, iOS, Desktop, and Web
- **Compose Multiplatform** — Built on JetBrains Compose Multiplatform
- **CommonMark Support** — Powered by `intellij-markdown` parser
- **Plugin System** — Modular plugin architecture for tables, images, HTML, and more
- **Customizable Themes** — Full control over typography, colors, and component styles

## Supported Platforms

| Platform | Status |
| :---: | :---: |
| Android | Supported |
| iOS (arm64, x64, simulator) | Supported |
| Desktop (JVM) | Supported |
| WebAssembly (Wasm) | Supported |

## Installation

> **Note:** This library will be published to **Maven Central**. Publication is in progress — the coordinates below are placeholders.

### System Requirements

- **Kotlin**: 2.0.21+
- **Compose Multiplatform**: Latest
- **Android API**: 24+ (Android 7.0)
- **Java**: 11+

### Add Dependency

Add the dependency to your project's `build.gradle.kts`:

```kotlin
// In your shared module's build.gradle.kts
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.iffly.compose:markdown-multiplatform:<version>")
            }
        }
    }
}
```

### Plugin Modules

| Plugin | Artifact | Description |
|--------|----------|-------------|
| Table | `markdown-multiplatform-table` | GFM table support |
| Image | `markdown-multiplatform-image` | Markdown image rendering |
| HTML | `markdown-multiplatform-html` | HTML inline tag support |

```kotlin
dependencies {
    implementation("com.iffly.compose:markdown-multiplatform-table:<version>")
    implementation("com.iffly.compose:markdown-multiplatform-image:<version>")
    implementation("com.iffly.compose:markdown-multiplatform-html:<version>")
}
```

## Quick Start

```kotlin
import com.iffly.compose.markdown.multiplatform.MarkdownView

@Composable
fun SimpleMarkdownExample() {
    val markdownContent = """
        # Hello Compose Markdown Multiplatform

        This is a **cross-platform** Markdown rendering library.

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

## Tech Stack

| Technology | Purpose |
|------------|---------|
| **Compose Multiplatform** | Cross-platform UI framework |
| **intellij-markdown** | Markdown parsing engine (pure Kotlin) |
| **Kotlin Coroutines** | Asynchronous processing |
| **Material Design 3** | Design language specification |

## API Reference

For full API signatures and detailed parameter explanations, see the dedicated API document:

- **Full API Reference**: [docs/API.md](docs/API.md)

## Contributing

We welcome contributions! To get started:

1. Fork the repository
2. Create a feature branch: `git checkout -b feat/my-feature`
3. Make your changes
4. Format code: `./gradlew ktlintFormat`
5. Run checks: `./gradlew ktlintCheck`
6. Build: `./gradlew assemble`
7. Commit using conventional prefixes (`feat:`, `fix:`, `docs:`, etc.)
8. Open a Pull Request

## License

Released under the MIT License. See [LICENSE](LICENSE) for details.

---

<div align="center">

**[Back to top](#compose-markdown-multiplatform)**

Made with love by the Compose Markdown team

</div>
