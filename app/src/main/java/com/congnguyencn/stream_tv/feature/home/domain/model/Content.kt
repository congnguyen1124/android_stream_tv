package com.congnguyencn.stream_tv.feature.home.domain.model

sealed interface Content {
    val id: String
    val videoUrl: String
    val thumbnailUrl: String
    val vastUrl: String
    val title: String
    val description: String
    val ageRestriction: String?
    val logoUrl: String
}

data class Video(
    override val id: String,
    override val videoUrl: String,
    override val thumbnailUrl: String,
    override val vastUrl: String,
    override val title: String,
    override val description: String,
    override val ageRestriction: String?,
    override val logoUrl: String,
) : Content

data class Series(
    override val id: String,
    override val videoUrl: String,
    override val thumbnailUrl: String,
    override val vastUrl: String,
    override val title: String,
    override val description: String,
    override val ageRestriction: String?,
    override val logoUrl: String,
    val episodes: List<Video>,
) : Content {
    init {
        require(episodes.isNotEmpty()) { "Series $id must contain at least one episode" }
    }
}

data class Channel(
    override val id: String,
    override val videoUrl: String,
    override val thumbnailUrl: String,
    override val vastUrl: String,
    override val title: String,
    override val description: String,
    override val ageRestriction: String?,
    override val logoUrl: String,
) : Content

data class Short(
    override val id: String,
    override val videoUrl: String,
    override val thumbnailUrl: String,
    override val vastUrl: String,
    override val title: String,
    override val description: String,
    override val ageRestriction: String?,
    override val logoUrl: String,
) : Content
