package com.congnguyencn.stream_tv.feature.home.data.source

import com.congnguyencn.stream_tv.feature.home.data.model.ChannelData
import com.congnguyencn.stream_tv.feature.home.data.model.HomeSectionData
import com.congnguyencn.stream_tv.feature.home.data.model.HomeSectionViewTypeData
import com.congnguyencn.stream_tv.feature.home.data.model.SeriesData
import com.congnguyencn.stream_tv.feature.home.data.model.ShortData
import com.congnguyencn.stream_tv.feature.home.data.model.VideoData

internal class HomeDummyDataSource {
    fun getHomeSections(): List<HomeSectionData> = listOf(
        HomeSectionData(
            id = "featured-stories",
            title = "Featured today",
            viewType = HomeSectionViewTypeData.Banner,
            items = featuredVideos,
        ),
        HomeSectionData(
            id = "videos-for-you",
            title = "Videos for you",
            viewType = HomeSectionViewTypeData.Videos,
            items = featuredVideos.reversed(),
        ),
        HomeSectionData(
            id = "documentary-series",
            title = "Documentary series",
            viewType = HomeSectionViewTypeData.ListSeries,
            items = documentarySeries,
        ),
        HomeSectionData(
            id = "live-channels",
            title = "Live channels",
            viewType = HomeSectionViewTypeData.Channels,
            items = liveChannels,
        ),
        HomeSectionData(
            id = "portrait-discovery",
            title = "Portrait discoveries",
            viewType = HomeSectionViewTypeData.VerticalBanner,
            items = discoveryShorts,
        ),
        HomeSectionData(
            id = "shorts-feed",
            title = "Fresh shorts",
            viewType = HomeSectionViewTypeData.Shorts,
            items = discoveryShorts.reversed(),
        ),
    )

    private companion object {
        const val BasketballImage =
            "https://images.pexels.com/photos/9839903/pexels-photo-9839903.jpeg?auto=compress&cs=tinysrgb&w=1600"
        const val FootballImage =
            "https://images.pexels.com/photos/36958062/pexels-photo-36958062.jpeg?auto=compress&cs=tinysrgb&w=1600"
        const val CricketImage =
            "https://images.pexels.com/photos/11023865/pexels-photo-11023865.jpeg?auto=compress&cs=tinysrgb&w=1200"
        const val TigerForestImage =
            "https://images.pexels.com/photos/25785873/pexels-photo-25785873.jpeg?auto=compress&cs=tinysrgb&w=1600"
        const val TigerPortraitImage =
            "https://images.pexels.com/photos/12167844/pexels-photo-12167844.jpeg?auto=compress&cs=tinysrgb&w=1200"
        const val ChineseFestivalImage =
            "https://images.pexels.com/photos/30765119/pexels-photo-30765119/free-photo-of-vibrant-traditional-chinese-cultural-festival.jpeg?auto=compress&cs=tinysrgb&w=1600"
        const val ChineseNewYearImage =
            "https://images.pexels.com/photos/36603900/pexels-photo-36603900.jpeg?auto=compress&cs=tinysrgb&w=1200"
        const val TokyoStreetImage =
            "https://images.pexels.com/photos/12343886/pexels-photo-12343886.jpeg?auto=compress&cs=tinysrgb&w=1600"
        const val JapaneseCeremonyImage =
            "https://images.pexels.com/photos/31370378/pexels-photo-31370378.jpeg?auto=compress&cs=tinysrgb&w=1200"

        val featuredVideos = listOf(
            VideoData(
                id = "video-basketball-energy",
                thumbnailUrl = BasketballImage,
                title = "Pulse of the court",
                description = "Follow two athletes through a basketball game charged with speed, focus, and emotion.",
                ageRestriction = "P",
            ),
            VideoData(
                id = "video-wild-tiger",
                thumbnailUrl = TigerForestImage,
                title = "Realm of the Bengal tiger",
                description = "A quiet journey through Ranthambore and the hidden world of one of Asia's great predators.",
                ageRestriction = "T13",
            ),
            VideoData(
                id = "video-tokyo-culture",
                thumbnailUrl = TokyoStreetImage,
                title = "Tokyo: Tradition in motion",
                description = "Explore Asakusa, where kimonos, ancient temples, and modern city life meet.",
                ageRestriction = "P",
            ),
            VideoData(
                id = "video-chinese-festival",
                thumbnailUrl = ChineseFestivalImage,
                title = "Colors of a Chinese festival",
                description = "Vivid costumes, music, and community rituals bring a traditional celebration to life.",
                ageRestriction = "P",
            ),
            VideoData(
                id = "video-football-decision",
                thumbnailUrl = FootballImage,
                title = "The decisive touch",
                description = "A football match turns on one perfectly timed run and a fearless finish.",
                ageRestriction = "P",
            ),
            VideoData(
                id = "video-cricket-pressure",
                thumbnailUrl = CricketImage,
                title = "Under pressure at the crease",
                description = "A batter prepares for the delivery that could decide the entire match.",
                ageRestriction = "P",
            ),
            VideoData(
                id = "video-lunar-new-year",
                thumbnailUrl = ChineseNewYearImage,
                title = "Welcoming the new spring",
                description = "Red, gold, and generations of tradition fill a joyful Lunar New Year celebration.",
                ageRestriction = "P",
            ),
            VideoData(
                id = "video-japanese-ceremony",
                thumbnailUrl = JapaneseCeremonyImage,
                title = "Grace in every gesture",
                description = "A close look at the details, discipline, and meaning of a Japanese ceremony.",
                ageRestriction = "P",
            ),
        )

        val discoveryShorts = listOf(
            ShortData(
                id = "short-cricket-focus",
                thumbnailUrl = CricketImage,
                title = "Before the strike",
                description = "A cricket player finds complete focus just before the game begins.",
                ageRestriction = "P",
            ),
            ShortData(
                id = "short-lunar-new-year",
                thumbnailUrl = ChineseNewYearImage,
                title = "A spring in red and gold",
                description = "Lunar New Year comes alive among lanterns and traditional dress.",
                ageRestriction = "P",
            ),
            ShortData(
                id = "short-japanese-ceremony",
                thumbnailUrl = JapaneseCeremonyImage,
                title = "A Japanese ceremony",
                description = "Intricate clothing and timeless gestures shape a traditional ceremony.",
                ageRestriction = "P",
            ),
            ShortData(
                id = "short-tiger-portrait",
                thumbnailUrl = TigerPortraitImage,
                title = "The wild gaze",
                description = "A close portrait captures a tiger's quiet power among autumn leaves.",
                ageRestriction = "T13",
            ),
            ShortData(
                id = "short-football-motion",
                thumbnailUrl = FootballImage,
                title = "Motion on the pitch",
                description = "One decisive touch in a football match played at full speed.",
                ageRestriction = "P",
            ),
            ShortData(
                id = "short-basketball-reach",
                thumbnailUrl = BasketballImage,
                title = "Above the rim",
                description = "Two players rise for a split-second contest above the basket.",
                ageRestriction = "P",
            ),
            ShortData(
                id = "short-tokyo-walk",
                thumbnailUrl = TokyoStreetImage,
                title = "A minute in old Tokyo",
                description = "A quick walk through Asakusa where every corner holds a story.",
                ageRestriction = "P",
            ),
            ShortData(
                id = "short-festival-colors",
                thumbnailUrl = ChineseFestivalImage,
                title = "Festival colors",
                description = "Traditional costumes sweep past the camera in a burst of color.",
                ageRestriction = "P",
            ),
        )

        val documentarySeries = listOf(
            SeriesData(
                id = "series-wild-asia",
                thumbnailUrl = TigerForestImage,
                title = "Wild Asia",
                description = "A documentary series about Asia's landscapes and remarkable wildlife.",
                ageRestriction = "T13",
                episodes = listOf(
                    VideoData(
                        id = "episode-wild-asia-1",
                        thumbnailUrl = TigerForestImage,
                        title = "Episode 1: Predator of the forest",
                        description = "Track a Bengal tiger through its natural habitat.",
                        ageRestriction = "T13",
                    ),
                    VideoData(
                        id = "episode-wild-asia-2",
                        thumbnailUrl = TigerPortraitImage,
                        title = "Episode 2: Built to survive",
                        description = "The adaptations that help large animals endure a demanding wilderness.",
                        ageRestriction = "T13",
                    ),
                ),
            ),
            SeriesData(
                id = "series-east-asia-culture",
                thumbnailUrl = TokyoStreetImage,
                title = "Living heritage of East Asia",
                description = "Meet the people and living traditions of China and Japan.",
                ageRestriction = "P",
                episodes = listOf(
                    VideoData(
                        id = "episode-east-asia-1",
                        thumbnailUrl = ChineseFestivalImage,
                        title = "Episode 1: Colors of China",
                        description = "A day inside a vibrant traditional festival.",
                        ageRestriction = "P",
                    ),
                    VideoData(
                        id = "episode-east-asia-2",
                        thumbnailUrl = TokyoStreetImage,
                        title = "Episode 2: Old Tokyo",
                        description = "Asakusa through the eyes of the people who live there.",
                        ageRestriction = "P",
                    ),
                ),
            ),
            SeriesData(
                id = "series-human-performance",
                thumbnailUrl = BasketballImage,
                title = "The edge of performance",
                description = "Athletes reveal how preparation becomes instinct when the pressure rises.",
                ageRestriction = "P",
                episodes = listOf(
                    VideoData(
                        id = "episode-performance-1",
                        thumbnailUrl = BasketballImage,
                        title = "Episode 1: Reading the court",
                        description = "Basketball players make complex decisions in fractions of a second.",
                        ageRestriction = "P",
                    ),
                    VideoData(
                        id = "episode-performance-2",
                        thumbnailUrl = FootballImage,
                        title = "Episode 2: Space and timing",
                        description = "A football attack is built from movement before the ball arrives.",
                        ageRestriction = "P",
                    ),
                ),
            ),
            SeriesData(
                id = "series-rituals-of-asia",
                thumbnailUrl = JapaneseCeremonyImage,
                title = "Rituals of Asia",
                description = "A respectful journey through ceremonies that connect past and present.",
                ageRestriction = "P",
                episodes = listOf(
                    VideoData(
                        id = "episode-rituals-1",
                        thumbnailUrl = JapaneseCeremonyImage,
                        title = "Episode 1: A language of gestures",
                        description = "Every movement carries meaning in a traditional Japanese ceremony.",
                        ageRestriction = "P",
                    ),
                    VideoData(
                        id = "episode-rituals-2",
                        thumbnailUrl = ChineseNewYearImage,
                        title = "Episode 2: The color of renewal",
                        description = "Families welcome a new year through symbols of luck and renewal.",
                        ageRestriction = "P",
                    ),
                    VideoData(
                        id = "episode-rituals-3",
                        thumbnailUrl = ChineseFestivalImage,
                        title = "Episode 3: A community in celebration",
                        description = "Music and costume transform a gathering into shared memory.",
                        ageRestriction = "P",
                    ),
                ),
            ),
        )

        val liveChannels = listOf(
            ChannelData(
                id = "channel-sport-live",
                thumbnailUrl = BasketballImage,
                title = "StreamTV Sport",
                description = "The day's biggest sporting moments, live every day.",
                ageRestriction = "P",
            ),
            ChannelData(
                id = "channel-nature-live",
                thumbnailUrl = TigerForestImage,
                title = "StreamTV Nature",
                description = "An uninterrupted window into the wild, 24/7.",
                ageRestriction = "P",
            ),
            ChannelData(
                id = "channel-football-live",
                thumbnailUrl = FootballImage,
                title = "StreamTV Football",
                description = "Live matches, tactical analysis, and the stories behind the final score.",
                ageRestriction = "P",
            ),
            ChannelData(
                id = "channel-cricket-live",
                thumbnailUrl = CricketImage,
                title = "StreamTV Cricket",
                description = "International cricket and classic matches throughout the day.",
                ageRestriction = "P",
            ),
            ChannelData(
                id = "channel-culture-live",
                thumbnailUrl = ChineseFestivalImage,
                title = "StreamTV Culture",
                description = "Festivals, art, food, and living traditions from around the world.",
                ageRestriction = "P",
            ),
            ChannelData(
                id = "channel-city-live",
                thumbnailUrl = TokyoStreetImage,
                title = "StreamTV Cities",
                description = "A continuous window into the streets and rhythms of remarkable cities.",
                ageRestriction = "P",
            ),
        )
    }
}
