package com.congnguyencn.stream_tv.core.designsystem.component.contentrow

import androidx.compose.foundation.lazy.layout.LazyLayoutItemProvider
import androidx.compose.foundation.lazy.layout.getDefaultLazyLayoutKey
import androidx.compose.runtime.Composable

internal class ContentRowItemProvider(
    private val intervals: List<ContentRowInterval>,
) : LazyLayoutItemProvider {
    override val itemCount: Int = intervals.sumOf(ContentRowInterval::count)

    @Composable
    override fun Item(index: Int, key: Any) {
        val resolved = resolve(index)
        resolved.interval.itemContent(ContentRowItemScopeInstance, resolved.localIndex)
    }

    override fun getKey(index: Int): Any {
        val resolved = resolve(index)
        return resolved.interval.key?.invoke(resolved.localIndex) ?: getDefaultLazyLayoutKey(index)
    }

    override fun getContentType(index: Int): Any? {
        val resolved = resolve(index)
        return resolved.interval.contentType(resolved.localIndex)
    }

    override fun getIndex(key: Any): Int {
        for (index in 0 until itemCount) {
            if (getKey(index) == key) return index
        }
        return -1
    }

    private fun resolve(index: Int): ResolvedContentRowItem {
        require(index in 0 until itemCount) { "ContentRow index $index is outside 0 until $itemCount" }

        var startIndex = 0
        intervals.forEach { interval ->
            val endIndex = startIndex + interval.count
            if (index < endIndex) {
                return ResolvedContentRowItem(
                    interval = interval,
                    localIndex = index - startIndex,
                )
            }
            startIndex = endIndex
        }
        error("Unable to resolve ContentRow item at index $index")
    }
}

/** Adds one temporary copy of item zero after the real collection for the forward reset animation. */
internal class ResetEdgeContentRowItemProvider(
    private val source: ContentRowItemProvider,
) : LazyLayoutItemProvider {
    override val itemCount: Int = source.itemCount + if (source.itemCount > 1) 1 else 0

    fun anchorIndex(realIndex: Int): Int = realIndex

    @Composable
    override fun Item(index: Int, key: Any) {
        val realIndex = realIndex(index)
        source.Item(realIndex, source.getKey(realIndex))
    }

    override fun getKey(index: Int): Any = "content-row-$index:${source.getKey(realIndex(index))}"

    override fun getContentType(index: Int): Any? = source.getContentType(realIndex(index))

    private fun realIndex(index: Int): Int {
        require(source.itemCount > 0) { "An empty ContentRow has no virtual items" }
        return index % source.itemCount
    }

}

internal class ContentRowScopeImpl : ContentRowScope {
    private val intervals = mutableListOf<ContentRowInterval>()

    override fun item(
        key: Any?,
        contentType: Any?,
        content: @Composable ContentRowItemScope.() -> Unit,
    ) {
        intervals += ContentRowInterval(
            count = 1,
            key = key?.let { itemKey -> { _: Int -> itemKey } },
            contentType = { contentType },
            itemContent = { content() },
        )
    }

    override fun items(
        count: Int,
        key: ((index: Int) -> Any)?,
        contentType: (index: Int) -> Any?,
        itemContent: @Composable ContentRowItemScope.(index: Int) -> Unit,
    ) {
        require(count >= 0) { "ContentRow item count must be non-negative" }
        if (count == 0) return

        intervals += ContentRowInterval(
            count = count,
            key = key,
            contentType = contentType,
            itemContent = itemContent,
        )
    }

    fun build(): ContentRowItemProvider = ContentRowItemProvider(intervals.toList())
}

internal data class ContentRowInterval(
    val count: Int,
    val key: ((localIndex: Int) -> Any)?,
    val contentType: (localIndex: Int) -> Any?,
    val itemContent: @Composable ContentRowItemScope.(localIndex: Int) -> Unit,
)

private data class ResolvedContentRowItem(
    val interval: ContentRowInterval,
    val localIndex: Int,
)

private object ContentRowItemScopeInstance : ContentRowItemScope
