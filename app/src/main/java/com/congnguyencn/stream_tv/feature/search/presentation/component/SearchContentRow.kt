package com.congnguyencn.stream_tv.feature.search.presentation.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.core.designsystem.component.StreamTvNetworkImage
import com.congnguyencn.stream_tv.core.designsystem.component.contentrow.ContentRow
import com.congnguyencn.stream_tv.core.designsystem.component.contentrow.itemsIndexed
import com.congnguyencn.stream_tv.core.designsystem.component.contentrow.rememberContentRowState
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.feature.search.presentation.model.SearchContentTypeUi
import com.congnguyencn.stream_tv.feature.search.presentation.model.SearchContentUiItem
import com.congnguyencn.stream_tv.feature.search.presentation.model.SearchSectionUiItem

private val SearchCardShape = RoundedCornerShape(8.dp)
private val SearchFocusShape = RoundedCornerShape(10.dp)

private enum class SearchContentRowStyle(
  val cardWidth: Dp,
  val aspectRatio: Float,
  val detailHeight: Dp,
) {
  Video(
    cardWidth = 190.dp,
    aspectRatio = 16f / 9f,
    detailHeight = 46.dp,
  ),
  Short(
    cardWidth = 112.dp,
    aspectRatio = 2f / 3f,
    detailHeight = 54.dp,
  ),
  ;

  val thumbnailHeight: Dp
    get() = cardWidth / aspectRatio
}

@Composable
internal fun SearchContentRow(
  section: SearchSectionUiItem,
  focusRequester: FocusRequester,
  modifier: Modifier = Modifier,
  upFocusRequester: FocusRequester? = null,
  downFocusRequester: FocusRequester? = null,
  onNavigateUp: (() -> Boolean)? = null,
  onItemClick: (SearchContentUiItem) -> Unit = {},
) {
  if (section.items.isEmpty()) return

  val style = when (section.type) {
    SearchContentTypeUi.Video -> SearchContentRowStyle.Video
    SearchContentTypeUi.Short -> SearchContentRowStyle.Short
  }
  val state = rememberContentRowState()

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Text(
      text = section.title,
      color = StreamTvColors.Neutral10,
      style = StreamTvTheme.typography.titleLarge,
    )

    ContentRow(
      state = state,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("search-content-row-${section.id}"),
      itemSpacing = 16.dp,
      contentPadding = PaddingValues(0.dp),
      startEdgeFocusRequester = FocusRequester.Cancel,
      selectedItemModifier = Modifier
        .focusRequester(focusRequester)
        .focusProperties {
          upFocusRequester?.let { up = it }
          downFocusRequester?.let { down = it }
        }
        .then(
          if (onNavigateUp != null) {
            Modifier.onPreviewKeyEvent { event ->
              if (event.key != Key.DirectionUp) return@onPreviewKeyEvent false
              if (event.type == KeyEventType.KeyDown) onNavigateUp()
              true
            }
          } else {
            Modifier
          },
        )
        .testTag("search-content-row-${section.id}-selection"),
      selectedItem = { isFocused ->
        SearchContentFocusFrame(
          style = style,
          isFocused = isFocused,
        )
      },
      onSelectedItemClick = { selectedIndex ->
        section.items.getOrNull(selectedIndex)?.let(onItemClick)
      },
    ) {
      itemsIndexed(
        items = section.items,
        key = { _, item -> item.id },
        contentType = { _, item -> item.type },
      ) { index, item ->
        SearchContentCard(
          item = item,
          style = style,
          isSelected = index == state.selectedIndex,
          modifier = Modifier.testTag("search-content-${item.id}"),
        )
      }
    }
  }
}

@Composable
private fun SearchContentFocusFrame(
  style: SearchContentRowStyle,
  isFocused: Boolean,
  modifier: Modifier = Modifier,
) {
  val borderColor by animateColorAsState(
    targetValue = if (isFocused) StreamTvColors.NeutralWhite else StreamTvColors.Transparent,
    label = "SearchContentFocusBorder",
  )

  Box(modifier = modifier.fillMaxSize()) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(style.thumbnailHeight + 4.dp)
        .border(3.dp, borderColor, SearchFocusShape),
    )
  }
}

@Composable
private fun SearchContentCard(
  item: SearchContentUiItem,
  style: SearchContentRowStyle,
  isSelected: Boolean,
  modifier: Modifier = Modifier,
) {
  val titleColor by animateColorAsState(
    targetValue = if (isSelected) StreamTvColors.NeutralWhite else StreamTvColors.Neutral20,
    label = "SearchContentTitleColor",
  )

  Column(modifier = modifier.width(style.cardWidth)) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(style.aspectRatio)
        .clip(SearchCardShape)
        .background(StreamTvColors.Neutral90),
    ) {
      StreamTvNetworkImage(
        imageUrl = item.thumbnailUrl,
        contentDescription = item.title,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
      )
      Row(
        modifier = Modifier
          .align(Alignment.BottomStart)
          .padding(8.dp)
          .background(StreamTvColors.TransparentBlack80, RoundedCornerShape(4.dp))
          .padding(horizontal = 7.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = if (item.type == SearchContentTypeUi.Short) "SHORT" else "VIDEO",
          color = StreamTvColors.NeutralWhite,
          style = StreamTvTheme.typography.labelMedium,
        )
      }
      item.ageRestriction?.takeIf(String::isNotBlank)?.let { ageRestriction ->
        Text(
          text = ageRestriction,
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(8.dp)
            .background(StreamTvColors.TransparentBlack60, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp),
          color = StreamTvColors.Neutral10,
          style = StreamTvTheme.typography.labelMedium,
        )
      }
    }

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .height(style.detailHeight)
        .padding(top = 8.dp),
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      Text(
        text = item.title,
        color = titleColor,
        style = StreamTvTheme.typography.bodyLarge,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = item.description,
        color = StreamTvColors.Neutral50,
        style = StreamTvTheme.typography.labelMedium,
        maxLines = if (item.type == SearchContentTypeUi.Short) 2 else 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF010810)
@Composable
private fun SearchContentRowPreview() {
  StreamTvTheme {
    SearchContentRow(
      section = SearchSectionUiItem(
        id = "preview-videos",
        title = "Videos",
        type = SearchContentTypeUi.Video,
        items = listOf(
          SearchContentUiItem(
            id = "preview-1",
            videoUrl = "",
            thumbnailUrl = "",
            title = "Realm of the Bengal tiger",
            description = "A quiet journey through the wild.",
            ageRestriction = "T13",
            type = SearchContentTypeUi.Video,
          ),
          SearchContentUiItem(
            id = "preview-2",
            videoUrl = "",
            thumbnailUrl = "",
            title = "Tokyo: Tradition in motion",
            description = "Ancient temples meet modern city life.",
            ageRestriction = "P",
            type = SearchContentTypeUi.Video,
          ),
        ),
      ),
      focusRequester = remember { FocusRequester() },
      modifier = Modifier.padding(vertical = 24.dp),
    )
  }
}
