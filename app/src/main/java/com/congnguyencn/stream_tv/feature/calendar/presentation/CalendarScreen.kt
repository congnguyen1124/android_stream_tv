package com.congnguyencn.stream_tv.feature.calendar.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.congnguyencn.stream_tv.core.designsystem.component.StreamTvSurface
import com.congnguyencn.stream_tv.core.designsystem.component.lazyfocusedstack.LazyFocusedStack
import com.congnguyencn.stream_tv.core.designsystem.component.lazyfocusedstack.LazyFocusedStackColumn
import com.congnguyencn.stream_tv.core.designsystem.component.lazyfocusedstack.LazyFocusedStackItem
import com.congnguyencn.stream_tv.core.designsystem.component.lazyfocusedstack.LazyFocusedStackDefaults
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.core.designsystem.tokens.StreamTvDimensions
import com.congnguyencn.stream_tv.feature.calendar.presentation.model.CalendarChannelUiModel
import com.congnguyencn.stream_tv.feature.calendar.presentation.model.CalendarDayUiModel
import com.congnguyencn.stream_tv.feature.calendar.presentation.model.CalendarProgramUiModel
import java.util.Locale

private object CalendarScreenDefaults {
  val TopPadding = StreamTvDimensions.TopBarHeight + 2.dp
  val ScreenHorizontalPadding = 6.dp
  val BottomPadding = 6.dp
  val GuideShape = RoundedCornerShape(10.dp)
  val ProgramShape = RoundedCornerShape(6.dp)
  val ProgramContentPadding = 12.dp
  val HeaderDividerWidth = 1.dp
  const val ImageDurationThresholdMinutes = 60
}

@Composable
internal fun CalendarScreen(
  uiState: CalendarUiState,
  contentFocusRequester: FocusRequester,
  topBarFocusRequester: FocusRequester,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier.fillMaxSize()
  ) {
    when {
      uiState.isLoading -> CalendarMessage("Loading program guide…")
      uiState.errorMessage != null -> CalendarMessage(uiState.errorMessage)
      uiState.schedule != null -> CalendarGuide(
        schedule = uiState.schedule,
        contentFocusRequester = contentFocusRequester,
        topBarFocusRequester = topBarFocusRequester,
      )
      else -> CalendarMessage("No programs are available for this day")
    }
  }
}

@Composable
private fun CalendarGuide(
  schedule: CalendarDayUiModel,
  contentFocusRequester: FocusRequester,
  topBarFocusRequester: FocusRequester,
) {
  val columns = remember(schedule) {
    schedule.channels.map { channel ->
      LazyFocusedStackColumn(
        key = channel.id,
        header = channel,
        items = channel.programs.map { program ->
          LazyFocusedStackItem(
            key = program.id,
            startMinute = program.startMinute,
            endMinute = program.endMinute,
            value = program,
          )
        },
      )
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(
        start = CalendarScreenDefaults.ScreenHorizontalPadding,
        top = CalendarScreenDefaults.TopPadding,
        end = CalendarScreenDefaults.ScreenHorizontalPadding,
        bottom = CalendarScreenDefaults.BottomPadding,
      ),
  ) {
    LazyFocusedStack(
      columns = columns,
      modifier = Modifier
        .fillMaxSize()
        .clip(CalendarScreenDefaults.GuideShape)
        .testTag("calendar-focused-stack"),
      selectedItemModifier = Modifier
        .focusRequester(contentFocusRequester)
        .focusProperties { up = topBarFocusRequester }
        .testTag("calendar-selected-program"),
      leadingHeader = { CalendarDateHeader(schedule.dateLabel) },
      columnHeader = { channel -> CalendarChannelHeader(channel) },
      timeLabel = { minute -> CalendarTimeLabel(minute) },
      itemContent = { program, isSelected -> CalendarProgramCard(program, isSelected) },
      selectedItem = { isFocused ->
        LazyFocusedStackDefaults.SelectedItem(
          isFocused = isFocused,
          shape = CalendarScreenDefaults.ProgramShape,
        )
      },
    )
  }
}

@Composable
private fun CalendarDateHeader(dateLabel: String) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .calendarHeaderDividers()
      .padding(horizontal = 8.dp, vertical = 6.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
  ) {
    Text(
      text = dateLabel.substringBefore(','),
      color = StreamTvColors.Primary30,
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold,
    )
    Text(
      text = dateLabel.substringAfter(',').trim(),
      color = StreamTvColors.NeutralWhite,
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
      maxLines = 1,
    )
  }
}

@Composable
private fun CalendarChannelHeader(channel: CalendarChannelUiModel) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 8.dp, vertical = 5.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
  ) {
    Box(
      modifier = Modifier
        .size(28.dp)
        .clip(RoundedCornerShape(50))
        .background(channel.accentColor()),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = channel.initials(),
        color = StreamTvColors.NeutralWhite,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
      )
      if (channel.logoUrl.isNotBlank()) {
        AsyncImage(
          model = channel.logoUrl,
          contentDescription = null,
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop,
        )
      }
    }
    Spacer(Modifier.height(4.dp))
    Text(
      text = channel.title,
      color = StreamTvColors.NeutralWhite,
      fontSize = 11.sp,
      fontWeight = FontWeight.Medium,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

@Composable
private fun CalendarTimeLabel(minute: Int) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(end = 14.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = String.format(Locale.ENGLISH, "%02d", (minute / 60).coerceAtMost(24)),
      color = StreamTvColors.Neutral20,
      fontSize = 11.sp,
      fontWeight = FontWeight.Medium,
    )
  }
}

private fun Modifier.calendarHeaderDividers(): Modifier = drawBehind {
  val strokeWidth = CalendarScreenDefaults.HeaderDividerWidth.toPx()
  drawLine(
    color = StreamTvColors.TransparentWhite20,
    start = androidx.compose.ui.geometry.Offset(size.width - strokeWidth / 2f, 0f),
    end = androidx.compose.ui.geometry.Offset(size.width - strokeWidth / 2f, size.height),
    strokeWidth = strokeWidth,
  )
  drawLine(
    color = StreamTvColors.TransparentWhite20,
    start = androidx.compose.ui.geometry.Offset(0f, size.height - strokeWidth / 2f),
    end = androidx.compose.ui.geometry.Offset(size.width, size.height - strokeWidth / 2f),
    strokeWidth = strokeWidth,
  )
}

@Composable
private fun CalendarProgramCard(program: CalendarProgramUiModel, isSelected: Boolean) {
  val showsArtwork = program.durationMinutes >= CalendarScreenDefaults.ImageDurationThresholdMinutes &&
    program.thumbnailUrl.isNotBlank()
  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(vertical = 1.5.dp, horizontal = 2.dp)
      .clip(CalendarScreenDefaults.ProgramShape)
      .background(Color(0xFF162633)),
  ) {
    if (showsArtwork) {
      AsyncImage(
        model = program.thumbnailUrl,
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
      )
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              colors = listOf(Color(0x16000000), Color(0xD9000000)),
            ),
          ),
      )
    }

    Column(
      modifier = Modifier
        .align(Alignment.BottomStart)
        .fillMaxWidth()
        .padding(CalendarScreenDefaults.ProgramContentPadding),
      verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
      Text(
        text = program.title,
        color = if (isSelected) StreamTvColors.NeutralWhite else StreamTvColors.Neutral10,
        fontSize = 14.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        maxLines = if (program.durationMinutes < 60) 1 else 2,
        overflow = TextOverflow.Ellipsis,
      )
      if (program.durationMinutes >= CalendarScreenDefaults.ImageDurationThresholdMinutes) {
        Text(
          text = program.timeLabel,
          color = StreamTvColors.Neutral20,
          fontSize = 11.sp,
          maxLines = 1,
        )
      }
    }
  }
}

@Composable
private fun CalendarMessage(message: String) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(top = CalendarScreenDefaults.TopPadding),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = message,
      color = StreamTvColors.Neutral20,
      style = StreamTvTheme.typography.titleLarge,
    )
  }
}

private fun CalendarChannelUiModel.initials(): String = title
  .split(' ')
  .filter(String::isNotBlank)
  .take(2)
  .joinToString(separator = "") { word -> word.take(1) }
  .uppercase(Locale.ENGLISH)

private fun CalendarChannelUiModel.accentColor(): Color {
  val palette = listOf(
    Color(0xFF2E7D68),
    Color(0xFF28669B),
    Color(0xFF6D4A8F),
    Color(0xFF9A5A35),
    Color(0xFF8A3E59),
  )
  return palette[id.hashCode().and(Int.MAX_VALUE) % palette.size]
}

private val PreviewProgram = CalendarProgramUiModel(
  id = "preview-program",
  title = "Realm of the Bengal Tiger",
  description = "A journey into the wild.",
  thumbnailUrl = "",
  startMinute = 7 * 60,
  endMinute = 9 * 60,
  timeLabel = "07:00 – 09:00",
)

private val PreviewSchedule = CalendarDayUiModel(
  dateLabel = "WED, 02 SEP",
  channels = listOf(
    CalendarChannelUiModel("nature", "Stream Nature", "", listOf(PreviewProgram)),
    CalendarChannelUiModel(
      "sport",
      "Stream Sport",
      "",
      listOf(PreviewProgram.copy(id = "sport-1", title = "Live: Court Central")),
    ),
    CalendarChannelUiModel("local", "Stream Local", "", emptyList()),
    CalendarChannelUiModel(
      "asia",
      "Stream Asia",
      "",
      listOf(PreviewProgram.copy(id = "asia-1", title = "Tokyo in Motion")),
    ),
  ),
)

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF020407)
@Composable
private fun CalendarScreenPreview() {
  StreamTvTheme {
    StreamTvSurface {
      CalendarScreen(
        uiState = CalendarUiState(isLoading = false, schedule = PreviewSchedule),
        contentFocusRequester = remember { FocusRequester() },
        topBarFocusRequester = remember { FocusRequester() },
      )
    }
  }
}

@Preview(device = Devices.TV_720p, showBackground = true, backgroundColor = 0xFF020407)
@Composable
private fun CalendarProgramCardPreview() {
  StreamTvTheme {
    Box(Modifier.width(214.dp).height(150.dp)) {
      CalendarProgramCard(PreviewProgram, isSelected = true)
    }
  }
}

@Preview(device = Devices.TV_720p, showBackground = true, backgroundColor = 0xFF020407)
@Composable
private fun CalendarChannelHeaderPreview() {
  StreamTvTheme {
    Box(Modifier.width(214.dp).height(64.dp)) {
      CalendarChannelHeader(PreviewSchedule.channels.first())
    }
  }
}

@Preview(device = Devices.TV_720p, showBackground = true, backgroundColor = 0xFF020407)
@Composable
private fun CalendarDateAndTimePreview() {
  StreamTvTheme {
    Row(Modifier.width(180.dp).height(64.dp)) {
      Box(Modifier.width(90.dp).fillMaxHeight()) { CalendarDateHeader("WED, 02 SEP") }
      Box(Modifier.width(90.dp).fillMaxHeight()) { CalendarTimeLabel(8 * 60) }
    }
  }
}

@Preview(device = Devices.TV_720p, showBackground = true, backgroundColor = 0xFF020407)
@Composable
private fun CalendarMessagePreview() {
  StreamTvTheme {
    CalendarMessage("Loading program guide…")
  }
}
