package com.iffly.compose.markdown.multiplatform.autolink

import com.iffly.compose.markdown.multiplatform.config.IMarkdownRenderPlugin
import org.commonmark.Extension
import org.commonmark.ext.autolink.AutolinkExtension

/**
 * Markdown render plugin that adds autolink support.
 *
 * Automatically detects URLs, email addresses, and www links in plain text
 * and converts them to clickable links. Uses the commonmark-kotlin autolink
 * extension for parsing; rendering is handled by the core [LinkNodeStringBuilder]
 * since autolinked text produces standard [org.commonmark.node.Link] nodes.
 *
 * @see AutolinkExtension
 * @see IMarkdownRenderPlugin
 */
class AutolinkMarkdownPlugin : IMarkdownRenderPlugin {
    override fun parserExtensions(): List<Extension> = listOf(AutolinkExtension.create())
}
