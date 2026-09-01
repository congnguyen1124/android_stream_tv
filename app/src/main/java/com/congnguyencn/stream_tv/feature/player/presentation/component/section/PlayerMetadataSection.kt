package com.congnguyencn.stream_tv.feature.player.presentation.component.section

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.R
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerMetadataUiState
import kotlinx.coroutines.launch

private const val MetadataScrollOffset = 200f

@Composable
internal fun PlayerMetadataSection(
  title: String,
  metadata: PlayerMetadataUiState,
  isFocusEnabled: Boolean,
  focusRequester: FocusRequester,
  onBack: () -> Unit,
  dismissOnLeft: Boolean,
  modifier: Modifier = Modifier,
) {
  val listState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()

  LaunchedEffect(isFocusEnabled) {
    if (isFocusEnabled) {
      awaitPlayerSectionFrame()
      focusRequester.requestFocus()
    }
  }

  Surface(
    onClick = {},
    modifier = modifier
      .fillMaxSize()
      .focusRequester(focusRequester)
      .focusProperties { canFocus = isFocusEnabled }
      .testTag("player-metadata-section")
      .handlePlayerSectionExit(onBack = onBack, dismissOnLeft = dismissOnLeft)
      .onPreviewKeyEvent { event ->
        if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
        when (event.key) {
          Key.DirectionUp -> coroutineScope.launch { listState.animateScrollBy(-MetadataScrollOffset) }
          Key.DirectionDown -> coroutineScope.launch { listState.animateScrollBy(MetadataScrollOffset) }
          else -> return@onPreviewKeyEvent false
        }
        true
      },
    colors = ClickableSurfaceDefaults.colors(
      containerColor = StreamTvColors.Transparent,
      focusedContainerColor = StreamTvColors.Transparent,
      contentColor = StreamTvColors.NeutralWhite,
      focusedContentColor = StreamTvColors.NeutralWhite,
    ),
    scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      state = listState,
      horizontalAlignment = Alignment.Start,
    ) {
      item(contentType = "CollectionTitle") {
        Text(
          text = metadata.collectionTitle,
          color = StreamTvColors.Neutral20,
          style = StreamTvTheme.typography.labelMedium,
        )
      }
      item { Spacer(modifier = Modifier.height(6.dp)) }
      item(contentType = "VideoTitle") {
        Text(
          text = title,
          modifier = Modifier.fillMaxWidth(),
          style = StreamTvTheme.typography.headlineLarge,
        )
      }
      item { Spacer(modifier = Modifier.height(16.dp)) }
      item(contentType = "Description") {
        Text(
          text = metadata.description,
          modifier = Modifier.fillMaxWidth(),
          textAlign = TextAlign.Justify,
          style = StreamTvTheme.typography.bodyLarge,
        )
      }
      item { Spacer(modifier = Modifier.height(12.dp)) }
      item(contentType = "LongDescription") {
        Text(
          text = metadata.longDescription,
          modifier = Modifier.fillMaxWidth(),
          textAlign = TextAlign.Justify,
          style = StreamTvTheme.typography.bodyLarge,
        )
      }
      item { Spacer(modifier = Modifier.height(20.dp)) }
      item(contentType = "MetadataTable") {
        PlayerMetadataTable(
          metadata = metadata,
          modifier = Modifier
            .fillMaxWidth()
            .background(StreamTvColors.Neutral60.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(16.dp),
        )
      }
    }
  }
}

@Composable
private fun PlayerMetadataTable(metadata: PlayerMetadataUiState, modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    MetadataRow(
      label = androidx.compose.ui.res.stringResource(R.string.player_collection),
      value = metadata.collectionTitle,
    )
    MetadataRow(label = androidx.compose.ui.res.stringResource(R.string.player_season), value = metadata.seasonTitle)
    MetadataRow(
      label = androidx.compose.ui.res.stringResource(R.string.player_release_year),
      value = metadata.releaseYear,
    )
    Spacer(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)
        .height(1.dp)
        .background(StreamTvColors.Neutral50),
    )
    MetadataRow(label = androidx.compose.ui.res.stringResource(R.string.player_genres), value = metadata.genres)
    MetadataRow(label = androidx.compose.ui.res.stringResource(R.string.player_directors), value = metadata.directors)
    MetadataRow(label = androidx.compose.ui.res.stringResource(R.string.player_producers), value = metadata.producers)
    MetadataRow(label = androidx.compose.ui.res.stringResource(R.string.player_writers), value = metadata.writers)
    MetadataRow(label = androidx.compose.ui.res.stringResource(R.string.player_cast), value = metadata.cast)
    MetadataRatingRow(rating = metadata.ageRestriction)
  }
}

@Composable
private fun MetadataRow(label: String, value: String) {
  if (value.isBlank()) return
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    verticalAlignment = Alignment.Top,
  ) {
    Text(
      text = label,
      modifier = Modifier.width(120.dp),
      style = StreamTvTheme.typography.labelMedium,
    )
    Text(
      text = value,
      modifier = Modifier.weight(1f),
      style = StreamTvTheme.typography.labelMedium,
    )
  }
}

@Composable
private fun MetadataRatingRow(rating: String) {
  if (rating.isBlank()) return
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = androidx.compose.ui.res.stringResource(R.string.player_rating),
      modifier = Modifier.width(120.dp),
      style = StreamTvTheme.typography.labelMedium,
    )
    Text(
      text = rating,
      modifier = Modifier
        .border(1.dp, StreamTvColors.TransparentWhite20, RoundedCornerShape(6.dp))
        .padding(horizontal = 8.dp, vertical = 2.dp),
      style = StreamTvTheme.typography.labelMedium,
    )
  }
}
