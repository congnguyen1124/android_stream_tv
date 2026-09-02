package com.congnguyencn.stream_tv.feature.calendar.data.source

import com.congnguyencn.stream_tv.feature.calendar.data.model.CalendarChannelData
import com.congnguyencn.stream_tv.feature.calendar.data.model.CalendarDayData
import com.congnguyencn.stream_tv.feature.calendar.data.model.CalendarProgramData
import java.util.Locale

internal class CalendarDummyDataSource {
  fun getSchedule(): CalendarDayData = CalendarDayData(
    date = ScheduleDate,
    channels = listOf(
      CalendarChannelData(
        channelId = "stream-nature",
        title = "Stream Nature",
        logoUrl = TigerImage,
        programs = programs(
          channelId = "nature",
          entries = listOf(
            entry("00:00", "02:00", "Wild Asia: Night Hunters", TigerImage),
            entry("02:00", "03:30", "Secrets of the Rainforest", ForestImage),
            entry("03:30", "04:00", "Nature Briefing", ""),
            entry("04:00", "06:00", "Ocean Frontiers", OceanImage),
            entry("06:00", "07:00", "Planet at Dawn", ForestImage),
            entry("07:00", "09:00", "Realm of the Bengal Tiger", TigerImage),
            entry("09:00", "09:45", "Wildlife Update", ""),
            entry("09:45", "12:00", "The Great Migration", ForestImage),
            entry("12:00", "14:00", "Earth From Above", OceanImage),
            entry("14:00", "16:30", "Giants of the Deep", OceanImage),
            entry("16:30", "18:00", "Forest Families", TigerImage),
            entry("18:00", "20:00", "Asia Untamed", ForestImage),
            entry("20:00", "22:30", "Blue Planet Stories", OceanImage),
            entry("22:30", "24:00", "Night in the Wild", TigerImage),
          ),
        ),
      ),
      CalendarChannelData(
        channelId = "stream-sport",
        title = "Stream Sport",
        logoUrl = BasketballImage,
        programs = programs(
          channelId = "sport",
          entries = listOf(
            entry("00:00", "01:00", "Matchday Review", FootballImage),
            entry("01:00", "03:00", "Classic Football", FootballImage),
            entry("03:00", "04:00", "Sports Desk", BasketballImage),
            entry("04:00", "06:30", "Live: International Cricket", CricketImage),
            entry("06:30", "07:15", "Morning Scores", ""),
            entry("07:15", "09:00", "Basketball Focus", BasketballImage),
            entry("09:00", "11:30", "Live: Court Central", BasketballImage),
            entry("11:30", "12:00", "Half-Time Report", ""),
            entry("12:00", "14:00", "Road to the Final", FootballImage),
            entry("14:00", "16:00", "Cricket Classics", CricketImage),
            entry("16:00", "18:30", "Live: Championship Football", FootballImage),
            entry("18:30", "19:00", "Final Whistle", ""),
            entry("19:00", "21:00", "Prime Basketball", BasketballImage),
            entry("21:00", "24:00", "Live: Stadium Night", FootballImage),
          ),
        ),
      ),
      // Deliberately empty so LazyFocusedStack's horizontal navigation can demonstrate skipping it.
      CalendarChannelData(
        channelId = "stream-local",
        title = "Stream Local",
        logoUrl = ForestImage,
        programs = emptyList(),
      ),
      CalendarChannelData(
        channelId = "stream-asia",
        title = "Stream Asia",
        logoUrl = FestivalImage,
        programs = programs(
          channelId = "asia",
          entries = listOf(
            entry("00:00", "02:30", "Tokyo After Dark", TokyoImage),
            entry("02:30", "04:00", "Living Heritage", FestivalImage),
            entry("04:00", "05:00", "Asia Today", TokyoImage),
            entry("05:00", "07:00", "Grace in Every Gesture", CeremonyImage),
            entry("07:00", "07:40", "Culture Minute", ""),
            entry("07:40", "10:00", "Colors of a Chinese Festival", FestivalImage),
            entry("10:00", "12:00", "Old Streets of Tokyo", TokyoImage),
            entry("12:00", "13:00", "Asia Today", FestivalImage),
            entry("13:00", "15:30", "The Silk Road", FestivalImage),
            entry("15:30", "17:00", "Japanese Craft", CeremonyImage),
            entry("17:00", "19:30", "Cities in Motion", TokyoImage),
            entry("19:30", "20:00", "Evening Update", ""),
            entry("20:00", "22:30", "Dynasties of China", FestivalImage),
            entry("22:30", "24:00", "Quiet Japan", CeremonyImage),
          ),
        ),
      ),
      CalendarChannelData(
        channelId = "stream-cinema",
        title = "Stream Cinema",
        logoUrl = TokyoImage,
        programs = programs(
          channelId = "cinema",
          entries = listOf(
            entry("00:00", "02:15", "Midnight Crossing", TokyoImage),
            entry("02:15", "04:00", "The Last Lantern", FestivalImage),
            entry("04:00", "06:00", "A Long Way Home", ForestImage),
            entry("06:00", "06:30", "Cinema Preview", ""),
            entry("06:30", "09:00", "Beyond the Horizon", OceanImage),
            entry("09:00", "11:00", "The Decisive Touch", FootballImage),
            entry("11:00", "13:15", "Autumn Letters", CeremonyImage),
            entry("13:15", "15:00", "City of Stories", TokyoImage),
            entry("15:00", "17:30", "Guardians of the Forest", TigerImage),
            entry("17:30", "18:00", "Coming Up", ""),
            entry("18:00", "20:15", "A Festival of Light", FestivalImage),
            entry("20:15", "22:30", "Blue Distance", OceanImage),
            entry("22:30", "24:00", "Late Night Cinema", TokyoImage),
          ),
        ),
      ),
      CalendarChannelData(
        channelId = "stream-kids",
        title = "Stream Kids",
        logoUrl = OceanImage,
        programs = programs(
          channelId = "kids",
          entries = listOf(
            entry("00:00", "05:00", "Dreamtime Stories", ForestImage),
            entry("05:00", "06:00", "Wake Up Club", FestivalImage),
            entry("06:00", "08:00", "Animal Adventures", TigerImage),
            entry("08:00", "08:25", "Mini Explorers", ""),
            entry("08:25", "10:00", "Ocean Friends", OceanImage),
            entry("10:00", "12:00", "Junior Champions", BasketballImage),
            entry("12:00", "14:00", "Festival Friends", FestivalImage),
            entry("14:00", "16:30", "Forest Detectives", ForestImage),
            entry("16:30", "18:00", "Tiger Tales", TigerImage),
            entry("18:00", "20:00", "Around the World", TokyoImage),
            entry("20:00", "21:00", "Bedtime Club", CeremonyImage),
            entry("21:00", "24:00", "Dreamtime Stories", ForestImage),
          ),
        ),
      ),
      CalendarChannelData(
        channelId = "stream-news",
        title = "Stream News",
        logoUrl = TokyoImage,
        programs = programs(
          channelId = "news",
          entries = buildList {
            repeat(24) { hour ->
              val start = String.format(Locale.ENGLISH, "%02d:00", hour)
              val stop = if (hour == 23) {
                "24:00"
              } else {
                String.format(Locale.ENGLISH, "%02d:00", hour + 1)
              }
              add(entry(start, stop, if (hour % 3 == 0) "World News" else "Newsroom Live", TokyoImage))
            }
          },
        ),
      ),
    ),
  )

  private fun programs(channelId: String, entries: List<ProgramEntry>): List<CalendarProgramData> =
    entries.mapIndexed { index, entry ->
      CalendarProgramData(
        programId = "$channelId-${index + 1}",
        startTime = "$ScheduleDate ${entry.start.toScheduleTime()}",
        stopTime = if (entry.stop == "24:00") {
          "$NextDate 00:00:00:000"
        } else {
          "$ScheduleDate ${entry.stop.toScheduleTime()}"
        },
        title = entry.title,
        description = "${entry.title} on StreamTV.",
        thumbnailUrl = entry.thumbnailUrl,
      )
    }

  private fun entry(start: String, stop: String, title: String, thumbnailUrl: String): ProgramEntry =
    ProgramEntry(start, stop, title, thumbnailUrl)

  private fun String.toScheduleTime(): String = "$this:00:000"

  private data class ProgramEntry(val start: String, val stop: String, val title: String, val thumbnailUrl: String)

  private companion object {
    const val ScheduleDate = "2026-09-02"
    const val NextDate = "2026-09-03"
    const val BasketballImage =
      "https://images.pexels.com/photos/9839903/pexels-photo-9839903.jpeg?auto=compress&cs=tinysrgb&w=1200"
    const val FootballImage =
      "https://images.pexels.com/photos/36958062/pexels-photo-36958062.jpeg?auto=compress&cs=tinysrgb&w=1200"
    const val CricketImage =
      "https://images.pexels.com/photos/11023865/pexels-photo-11023865.jpeg?auto=compress&cs=tinysrgb&w=1200"
    const val TigerImage =
      "https://images.pexels.com/photos/25785873/pexels-photo-25785873.jpeg?auto=compress&cs=tinysrgb&w=1200"
    const val ForestImage =
      "https://images.pexels.com/photos/1671325/pexels-photo-1671325.jpeg?auto=compress&cs=tinysrgb&w=1200"
    const val OceanImage =
      "https://images.pexels.com/photos/920163/pexels-photo-920163.jpeg?auto=compress&cs=tinysrgb&w=1200"
    const val FestivalImage =
      "https://images.pexels.com/photos/30765119/pexels-photo-30765119/free-photo-of-vibrant-traditional-chinese-cultural-festival.jpeg?auto=compress&cs=tinysrgb&w=1200"
    const val TokyoImage =
      "https://images.pexels.com/photos/12343886/pexels-photo-12343886.jpeg?auto=compress&cs=tinysrgb&w=1200"
    const val CeremonyImage =
      "https://images.pexels.com/photos/31370378/pexels-photo-31370378.jpeg?auto=compress&cs=tinysrgb&w=1200"
  }
}
