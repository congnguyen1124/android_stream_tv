package com.congnguyencn.stream_tv.core.designsystem.component.contentrow

import androidx.compose.runtime.Composable

@DslMarker
annotation class ContentRowDsl

@ContentRowDsl
interface ContentRowScope {
  fun item(key: Any? = null, contentType: Any? = null, content: @Composable ContentRowItemScope.() -> Unit)

  fun items(
    count: Int,
    key: ((index: Int) -> Any)? = null,
    contentType: (index: Int) -> Any? = { null },
    itemContent: @Composable ContentRowItemScope.(index: Int) -> Unit,
  )
}

@ContentRowDsl
interface ContentRowItemScope

fun <T> ContentRowScope.items(
  items: List<T>,
  key: ((item: T) -> Any)? = null,
  contentType: (item: T) -> Any? = { null },
  itemContent: @Composable ContentRowItemScope.(item: T) -> Unit,
) {
  items(
    count = items.size,
    key = key?.let { keyFactory -> { index -> keyFactory(items[index]) } },
    contentType = { index -> contentType(items[index]) },
  ) { index ->
    itemContent(items[index])
  }
}

fun <T> ContentRowScope.itemsIndexed(
  items: List<T>,
  key: ((index: Int, item: T) -> Any)? = null,
  contentType: (index: Int, item: T) -> Any? = { _, _ -> null },
  itemContent: @Composable ContentRowItemScope.(index: Int, item: T) -> Unit,
) {
  items(
    count = items.size,
    key = key?.let { keyFactory -> { index -> keyFactory(index, items[index]) } },
    contentType = { index -> contentType(index, items[index]) },
  ) { index ->
    itemContent(index, items[index])
  }
}
