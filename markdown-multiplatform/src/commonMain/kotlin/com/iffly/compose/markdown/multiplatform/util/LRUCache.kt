package com.iffly.compose.markdown.multiplatform.util

/**
 * LRU cache implementation using a simple linked list approach.
 * Compatible with all Kotlin targets including WasmJS.
 */
class LRUCache<K, V>(
    private val maxSize: Int,
) {
    private val map = LinkedHashMap<K, V>()

    /** The current number of entries in the cache. */
    val size: Int get() = map.size

    /** Returns the value associated with [key], or null if not present. Promotes the entry to most-recently-used. */
    operator fun get(key: K): V? {
        val value = map.remove(key) ?: return null
        map[key] = value
        return value
    }

    /** Inserts or updates the entry for [key] with [value], evicting the least-recently-used entry if the cache exceeds [maxSize]. */
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

    /** Removes all entries from the cache. */
    fun clear() {
        map.clear()
    }

    /** Returns the maximum number of entries this cache can hold. */
    fun maxSize(): Int = maxSize

    /** Returns a snapshot of the cache's current statistics. */
    fun getStats(): CacheStats =
        CacheStats(
            currentSize = size,
            maxSize = maxSize,
            loadFactor = size.toFloat() / maxSize,
        )

    /**
     * A snapshot of the cache's statistics.
     *
     * @param currentSize The current number of entries.
     * @param maxSize The maximum capacity.
     * @param loadFactor The ratio of current size to maximum size.
     */
    data class CacheStats(
        val currentSize: Int,
        val maxSize: Int,
        val loadFactor: Float,
    )
}
