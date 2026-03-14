package com.iffly.compose.markdown.multiplatform

import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkInteractionListener
import org.intellij.markdown.ast.ASTNode

interface CustomEvent

interface ActionHandler {
    fun handleUrlClick(
        url: String,
        node: ASTNode,
    ) {}

    fun handleCopyClick(node: ASTNode) {}

    fun handleImageClick(
        imageUrl: String,
        node: ASTNode,
    ) {}

    fun handleCustomEvent(
        event: CustomEvent,
        node: ASTNode,
    ) {}
}

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
