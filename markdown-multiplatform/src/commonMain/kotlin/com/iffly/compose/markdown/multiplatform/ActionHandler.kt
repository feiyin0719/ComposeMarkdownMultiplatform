package com.iffly.compose.markdown.multiplatform

import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkInteractionListener
import org.intellij.markdown.ast.ASTNode

/** Marker interface for custom user-defined events that can be dispatched through the [ActionHandler]. */
interface CustomEvent

/**
 * Interface for handling user interactions within rendered markdown content.
 * Implement this interface to respond to link clicks, image clicks, copy actions, and custom events.
 */
interface ActionHandler {
    /**
     * Called when a user clicks a URL link in the markdown content.
     *
     * @param url The URL that was clicked.
     * @param node The AST node associated with the link.
     */
    fun handleUrlClick(
        url: String,
        node: ASTNode,
    ) {}

    /**
     * Called when a user triggers a copy action on a markdown node.
     *
     * @param node The AST node whose content is being copied.
     */
    fun handleCopyClick(node: ASTNode) {}

    /**
     * Called when a user clicks an image in the markdown content.
     *
     * @param imageUrl The URL of the clicked image.
     * @param node The AST node associated with the image.
     */
    fun handleImageClick(
        imageUrl: String,
        node: ASTNode,
    ) {}

    /**
     * Called when a custom event is dispatched from the markdown content.
     *
     * @param event The custom event that was triggered.
     * @param node The AST node associated with the event.
     */
    fun handleCustomEvent(
        event: CustomEvent,
        node: ASTNode,
    ) {}
}

/**
 * A [LinkInteractionListener] that delegates URL link clicks to an [ActionHandler].
 *
 * @param actionHandler The handler that will process the URL click event.
 * @param node The AST node associated with the link.
 */
class MarkdownLinkInteractionListener(
    private val actionHandler: ActionHandler,
    private val node: ASTNode,
) : LinkInteractionListener {
    override fun onClick(link: LinkAnnotation) {
        (link as? LinkAnnotation.Url)?.let {
            actionHandler.handleUrlClick(link.url, node)
        }
    }
}
