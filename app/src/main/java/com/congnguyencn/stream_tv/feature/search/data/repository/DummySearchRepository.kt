package com.congnguyencn.stream_tv.feature.search.data.repository

import com.congnguyencn.stream_tv.feature.home.domain.model.HomeSectionViewType
import com.congnguyencn.stream_tv.feature.home.domain.model.Short
import com.congnguyencn.stream_tv.feature.home.domain.model.Video
import com.congnguyencn.stream_tv.feature.home.domain.repository.HomeRepository
import com.congnguyencn.stream_tv.feature.search.domain.model.SearchContent
import com.congnguyencn.stream_tv.feature.search.domain.model.SearchContentType
import com.congnguyencn.stream_tv.feature.search.domain.model.SearchSection
import com.congnguyencn.stream_tv.feature.search.domain.repository.SearchRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Search catalog backed by the app's dummy Home feed.
 *
 * The repository owns filtering and fallback ordering so the presentation layer only handles query
 * editing and immutable UI state. The catalog is cached after the first read because keyboard input
 * can request suggestions frequently.
 */
internal class DummySearchRepository(private val homeRepository: HomeRepository) : SearchRepository {
  private val catalogMutex = Mutex()

  @Volatile
  private var cachedCatalog: SearchCatalog? = null

  override suspend fun getRecommendations(): List<SearchSection> = catalog().toSections()

  override suspend fun search(query: String): List<SearchSection> {
    val source = catalog()
    val tokens = query.normalizedTokens()
    if (tokens.isEmpty()) return source.toSections()

    val matchingVideos = source.videos.filter { content -> content.matches(tokens) }
    val matchingShorts = source.shorts.filter { content -> content.matches(tokens) }

    // Dummy content should still demonstrate the final layout for arbitrary keyboard input. When
    // no title matches, rotate the catalog deterministically instead of presenting an empty screen.
    val seed = query.lowercase().hashCode()
    return SearchCatalog(
      videos = matchingVideos.ifEmpty { source.videos.rotatedBy(seed) },
      shorts = matchingShorts.ifEmpty { source.shorts.rotatedBy(seed) },
    ).toSections()
  }

  override suspend fun getSuggestions(query: String): List<String> {
    val normalizedQuery = query.trim().lowercase()
    val catalogTitles = catalog().all.map(SearchContent::title)
    val candidates = RecentSearches + CuratedSearches + catalogTitles

    return candidates
      .distinct()
      .filter { suggestion ->
        normalizedQuery.isBlank() || suggestion.lowercase().contains(normalizedQuery)
      }
      .take(MaxSuggestions)
  }

  private suspend fun catalog(): SearchCatalog {
    cachedCatalog?.let { return it }
    return catalogMutex.withLock {
      cachedCatalog ?: loadCatalog().also { cachedCatalog = it }
    }
  }

  private suspend fun loadCatalog(): SearchCatalog {
    val sections = homeRepository.getHomeSections()
    val videos = sections
      .filter { section -> section.viewType in VideoSectionTypes }
      .flatMap { section -> section.items }
      .filterIsInstance<Video>()
      .distinctBy(Video::id)
      .map(Video::toSearchContent)
    val shorts = sections
      .filter { section -> section.viewType in ShortSectionTypes }
      .flatMap { section -> section.items }
      .filterIsInstance<Short>()
      .distinctBy(Short::id)
      .map(Short::toSearchContent)

    return SearchCatalog(videos = videos, shorts = shorts)
  }

  private companion object {
    const val MaxSuggestions = 6

    val VideoSectionTypes = setOf(
      HomeSectionViewType.Banner,
      HomeSectionViewType.Videos,
      HomeSectionViewType.VideosPopular,
    )
    val ShortSectionTypes = setOf(
      HomeSectionViewType.VerticalBanner,
      HomeSectionViewType.Shorts,
      HomeSectionViewType.ShortPopular,
    )
    val RecentSearches = listOf(
      "Wildlife documentaries",
      "Live sports",
      "Japanese culture",
    )
    val CuratedSearches = listOf(
      "Chinese festivals",
      "Football highlights",
      "Basketball stories",
      "Nature shorts",
      "Tokyo travel",
    )
  }
}

private data class SearchCatalog(val videos: List<SearchContent>, val shorts: List<SearchContent>) {
  val all: List<SearchContent>
    get() = videos + shorts

  fun toSections(): List<SearchSection> = buildList {
    if (videos.isNotEmpty()) {
      add(
        SearchSection(
          id = "search-videos",
          type = SearchContentType.Video,
          items = videos,
        ),
      )
    }
    if (shorts.isNotEmpty()) {
      add(
        SearchSection(
          id = "search-shorts",
          type = SearchContentType.Short,
          items = shorts,
        ),
      )
    }
  }
}

private fun SearchContent.matches(tokens: List<String>): Boolean {
  val searchableText = "$title $description".lowercase()
  return tokens.all(searchableText::contains)
}

private fun String.normalizedTokens(): List<String> = lowercase()
  .trim()
  .split(Regex("\\s+"))
  .filter(String::isNotBlank)

private fun <T> List<T>.rotatedBy(seed: Int): List<T> {
  if (size < 2) return this
  val distance = Math.floorMod(seed, size)
  return drop(distance) + take(distance)
}

private fun Video.toSearchContent() = SearchContent(
  id = id,
  videoUrl = videoUrl,
  thumbnailUrl = thumbnailUrl,
  title = title,
  description = description,
  ageRestriction = ageRestriction,
  type = SearchContentType.Video,
)

private fun Short.toSearchContent() = SearchContent(
  id = id,
  videoUrl = videoUrl,
  thumbnailUrl = thumbnailUrl,
  title = title,
  description = description,
  ageRestriction = ageRestriction,
  type = SearchContentType.Short,
)
