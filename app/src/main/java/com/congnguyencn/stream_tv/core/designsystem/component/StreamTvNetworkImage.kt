package com.congnguyencn.stream_tv.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors

@Composable
fun StreamTvNetworkImage(
  imageUrl: String,
  contentDescription: String?,
  modifier: Modifier = Modifier,
  contentScale: ContentScale = ContentScale.Crop,
  onSuccess: ((AsyncImagePainter.State.Success) -> Unit)? = null,
) {
  AsyncImage(
    model = imageUrl,
    contentDescription = contentDescription,
    modifier = modifier,
    placeholder = ColorPainter(StreamTvColors.Neutral90),
    error = ColorPainter(StreamTvColors.ImageErrorPlaceholder),
    fallback = ColorPainter(StreamTvColors.Neutral90),
    contentScale = contentScale,
    onSuccess = onSuccess,
  )
}
