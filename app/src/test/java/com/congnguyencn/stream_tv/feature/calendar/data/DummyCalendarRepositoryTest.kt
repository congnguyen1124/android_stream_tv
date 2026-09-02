package com.congnguyencn.stream_tv.feature.calendar.data

import com.congnguyencn.stream_tv.feature.calendar.data.mapper.CalendarDataMapper
import com.congnguyencn.stream_tv.feature.calendar.data.repository.DummyCalendarRepository
import com.congnguyencn.stream_tv.feature.calendar.data.source.CalendarDummyDataSource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DummyCalendarRepositoryTest {
  private val repository = DummyCalendarRepository(
    dataSource = CalendarDummyDataSource(),
    mapper = CalendarDataMapper(),
  )

  @Test
  fun `dummy schedule maps json-shaped timestamps into one bounded day`() = runTest {
    val schedule = repository.getCalendarSchedule()

    assertEquals(7, schedule.channels.size)
    assertTrue(schedule.channels.any { channel -> channel.programs.isEmpty() })
    assertTrue(schedule.channels.flatMap { channel -> channel.programs }.all { program ->
      program.startMinute in 0 until 24 * 60 &&
        program.endMinute in 1..24 * 60 &&
        program.endMinute > program.startMinute
    })
  }

  @Test
  fun `dummy schedule exercises compact and artwork program durations`() = runTest {
    val programs = repository.getCalendarSchedule().channels.flatMap { channel -> channel.programs }

    assertTrue(programs.any { program -> program.durationMinutes < 60 })
    assertTrue(programs.any { program -> program.durationMinutes >= 60 })
  }
}
