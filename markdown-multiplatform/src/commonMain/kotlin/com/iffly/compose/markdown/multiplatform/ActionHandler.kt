package com.iffly.compose.markdown.multiplatform

import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkInteractionListener
import org.commonmark.node.Node

/** Marker interface for custom user-defined events that can be dispatched through the [ActionHandler]. */
interface CustomEvent

/**
 * Interface for handling user interactions within rendered markdown content.
 * Implement this interface to respond to link clicks, image clicks, copy actions, and custom events.
 */
interface ActionHandler {
    fun handleUrlClick(
        url: String,
        node: Node,
    ) {}

    fun handleCopyClick(node: Node) {}

    fun handleImageClick(
        imageUrl: String,
        node: Node,
    ) {}

    fun handleCustomEvent(
        event: CustomEvent,
        node: Node,
    ) {}
}

class MarkdownLinkInteractionListener(
    private val actionHandler: ActionHandler,
    private val node: Node,
) : LinkInteractionListener {
    override fun onClick(link: LinkAnnotation) {
        (link as? LinkAnnotation.Url)?.let {
            actionHandler.handleUrlClick(link.url, node)
        }
    }
}
