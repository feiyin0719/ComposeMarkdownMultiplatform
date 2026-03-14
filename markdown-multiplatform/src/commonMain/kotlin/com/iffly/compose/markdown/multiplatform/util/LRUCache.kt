package com.iffly.compose.markdown.multiplatform.util

/**
 * LRU cache implementation using a simple linked list approach.
 * Compatible with all Kotlin targets including WasmJS.
 */
class LRUCache<K, V>(
    private val maxSize: Int,
) {
    private val map = LinkedHashMap<K, V>()

    val size: Int get() = map.size

    operator fun get(key: K): V? {
        val value = map.remove(key) ?: return null
        map[key] = value
        return value
    }

    operator fun set(
        key: K,
        value: V,
    ) {
        map.remove(key)
        map[key] = value
        if (map.size > maxSize) {
            val eldest = map.keys.first()
            map.remove(eldest)
        }
    }

    fun clear() {
        map.clear()
    }

    fun maxSize(): Int = maxSize

    fun getStats(): CacheStats =
        CacheStats(
            currentSize = size,
            maxSize = maxSize,
            loadFactor = size.toFloat() / maxSize,
        )

    data class CacheStats(
        val currentSize: Int,
        val maxSize: Int,
        val loadFactor: Float,
    )
}
