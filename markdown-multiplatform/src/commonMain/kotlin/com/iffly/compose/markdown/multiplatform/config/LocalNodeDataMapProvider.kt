@file:Suppress("ktlint:compose:compositionlocal-allowlist")

package com.iffly.compose.markdown.multiplatform.config

import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.staticCompositionLocalOf
import org.commonmark.node.Node

/**
 * CompositionLocal that provides a shared [SnapshotStateMap] for cross-layer
 * data passing between renderers during markdown rendering.
 *
 * The map is keyed by AST [Node] reference and the value is [Any],
 * allowing different renderers to store and retrieve typed data
 * (e.g. [com.iffly.compose.markdown.multiplatform.core.renders.FirstLineMetrics])
 * without per-node [CompositionLocalProvider] nesting.
 *
 * Provided once at the top level in [ProvideMarkdownLocals].
 */
val LocalNodeDataMap =
    staticCompositionLocalOf<SnapshotStateMap<Node, Any>> {
        error("No NodeDataMap provided")
    }
