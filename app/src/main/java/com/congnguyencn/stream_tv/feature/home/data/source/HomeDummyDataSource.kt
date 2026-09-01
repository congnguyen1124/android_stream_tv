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
    // Free public HLS test streams. Grouped by what each surface needs to exercise: VOD for the
    // seekable players, live for the channel rows (no seek bar, resume jumps to the live edge).
    //
    // `trailerUrl` draws from the same VOD pool, rotated so no item's trailer is its own stream: the
    // banner's thumbnail-to-trailer hand-off is only visible when the two are different videos. Live
    // manifests stay out of the rotation because a trailer has to end for the loop-back to run.
    private object StreamUrls {
      private const val AppleBase = "https://devstreaming-cdn.apple.com/videos/streaming/examples/"
      private const val MuxBase = "https://test-streams.mux.dev/"
      private const val UnifiedBase =
        "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/"

      const val AppleBipBopTs = AppleBase + "img_bipbop_adv_example_ts/master.m3u8"
      const val AppleBipBopFmp4 = AppleBase + "img_bipbop_adv_example_fmp4/master.m3u8"
      const val AppleHevc = AppleBase + "bipbop_adv_example_hevc/master.m3u8"

      const val TearsOfSteelTs = UnifiedBase + "tears-of-steel.ism/.m3u8"
      const val TearsOfSteelFmp4 = UnifiedBase + "tears-of-steel.mp4/.m3u8"

      const val BigBuckBunnyAbr = MuxBase + "x36xhzz/x36xhzz.m3u8"
      const val BigBuckBunnyFixed = MuxBase + "x36xhzz/url_6/193039199_mp4_h264_aac_hq_7.m3u8"
      const val MuxPtsShift = MuxBase + "pts_shift/master.m3u8"
      const val MuxDaiDiscontinuity = MuxBase + "dai-discontinuity-deltatre/manifest.m3u8"
      const val MuxImscCaptions = MuxBase + "tos_ismc/main.m3u8"
      const val MuxTest001 = MuxBase + "test_001/stream.m3u8"
      const val MuxIssue666 = MuxBase + "issue666/playlists/cisq0gim60007xzvi505emlxx.m3u8"
      const val MuxSampleAes = MuxBase + "bbbAES/playlists/sample_aes/index.m3u8"

      const val JwPlayerBigBuckBunny = "https://cdn.jwplayer.com/manifests/pZxWPRg4.m3u8"
      const val LongtailBipBop = "https://playertest.longtailvideo.com/adaptive/bipbop/gear4/prog_index.m3u8"
      const val ShakaAngelOne = "https://storage.googleapis.com/shaka-demo-assets/angel-one-hls/hls.m3u8"
      const val BitmovinSintel = "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8"

      const val AkamaiLive = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8"
      const val AkamaiEightLive = "https://moctobpltc-i.akamaihd.net/hls/live/571329/eight/playlist.m3u8"
      const val ShakaLive = "https://storage.googleapis.com/shaka-live-assets/player-source.m3u8"
    }
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
      "https://images.pexels.com/photos/30765119/pexels-photo-30765119/" +
        "free-photo-of-vibrant-traditional-chinese-cultural-festival.jpeg?auto=compress&cs=tinysrgb&w=1600"
    const val ChineseNewYearImage =
      "https://images.pexels.com/photos/36603900/pexels-photo-36603900.jpeg?auto=compress&cs=tinysrgb&w=1200"
    const val TokyoStreetImage =
      "https://images.pexels.com/photos/12343886/pexels-photo-12343886.jpeg?auto=compress&cs=tinysrgb&w=1600"
    const val JapaneseCeremonyImage =
      "https://images.pexels.com/photos/31370378/pexels-photo-31370378.jpeg?auto=compress&cs=tinysrgb&w=1200"

    val featuredVideos = listOf(
      VideoData(
        id = "video-basketball-energy",
        videoUrl = StreamUrls.AppleBipBopTs,
        trailerUrl = StreamUrls.AppleBipBopFmp4,
        thumbnailUrl = BasketballImage,
        title = "Pulse of the court",
        description = "Follow two athletes through a basketball game charged with speed, focus, and emotion.",
        ageRestriction = "P",
      ),
      VideoData(
        id = "video-wild-tiger",
        videoUrl = StreamUrls.TearsOfSteelTs,
        trailerUrl = StreamUrls.AppleHevc,
        thumbnailUrl = TigerForestImage,
        title = "Realm of the Bengal tiger",
        description = "A quiet journey through Ranthambore and the hidden world of one of Asia's great predators.",
        ageRestriction = "T13",
      ),
      VideoData(
        id = "video-tokyo-culture",
        videoUrl = StreamUrls.BigBuckBunnyAbr,
        trailerUrl = StreamUrls.TearsOfSteelTs,
        thumbnailUrl = TokyoStreetImage,
        title = "Tokyo: Tradition in motion",
        description = "Explore Asakusa, where kimonos, ancient temples, and modern city life meet.",
        ageRestriction = "P",
      ),
      VideoData(
        id = "video-chinese-festival",
        videoUrl = StreamUrls.TearsOfSteelFmp4,
        trailerUrl = StreamUrls.BigBuckBunnyAbr,
        thumbnailUrl = ChineseFestivalImage,
        title = "Colors of a Chinese festival",
        description = "Vivid costumes, music, and community rituals bring a traditional celebration to life.",
        ageRestriction = "P",
      ),
      VideoData(
        id = "video-football-decision",
        videoUrl = StreamUrls.AppleBipBopFmp4,
        trailerUrl = StreamUrls.ShakaAngelOne,
        thumbnailUrl = FootballImage,
        title = "The decisive touch",
        description = "A football match turns on one perfectly timed run and a fearless finish.",
        ageRestriction = "P",
      ),
      VideoData(
        id = "video-cricket-pressure",
        videoUrl = StreamUrls.AppleHevc,
        trailerUrl = StreamUrls.BitmovinSintel,
        thumbnailUrl = CricketImage,
        title = "Under pressure at the crease",
        description = "A batter prepares for the delivery that could decide the entire match.",
        ageRestriction = "P",
      ),
      VideoData(
        id = "video-lunar-new-year",
        videoUrl = StreamUrls.ShakaAngelOne,
        trailerUrl = StreamUrls.AppleBipBopTs,
        thumbnailUrl = ChineseNewYearImage,
        title = "Welcoming the new spring",
        description = "Red, gold, and generations of tradition fill a joyful Lunar New Year celebration.",
        ageRestriction = "P",
      ),
      VideoData(
        id = "video-japanese-ceremony",
        videoUrl = StreamUrls.BitmovinSintel,
        trailerUrl = StreamUrls.AppleBipBopFmp4,
        thumbnailUrl = JapaneseCeremonyImage,
        title = "Grace in every gesture",
        description = "A close look at the details, discipline, and meaning of a Japanese ceremony.",
        ageRestriction = "P",
      ),
    )

    val discoveryShorts = listOf(
      ShortData(
        id = "short-cricket-focus",
        videoUrl = StreamUrls.JwPlayerBigBuckBunny,
        trailerUrl = StreamUrls.AppleHevc,
        thumbnailUrl = CricketImage,
        title = "Before the strike",
        description = "A cricket player finds complete focus just before the game begins.",
        ageRestriction = "P",
      ),
      ShortData(
        id = "short-lunar-new-year",
        videoUrl = StreamUrls.LongtailBipBop,
        trailerUrl = StreamUrls.TearsOfSteelTs,
        thumbnailUrl = ChineseNewYearImage,
        title = "A spring in red and gold",
        description = "Lunar New Year comes alive among lanterns and traditional dress.",
        ageRestriction = "P",
      ),
      ShortData(
        id = "short-japanese-ceremony",
        videoUrl = StreamUrls.MuxTest001,
        trailerUrl = StreamUrls.TearsOfSteelFmp4,
        thumbnailUrl = JapaneseCeremonyImage,
        title = "A Japanese ceremony",
        description = "Intricate clothing and timeless gestures shape a traditional ceremony.",
        ageRestriction = "P",
      ),
      ShortData(
        id = "short-tiger-portrait",
        videoUrl = StreamUrls.BigBuckBunnyFixed,
        trailerUrl = StreamUrls.BigBuckBunnyAbr,
        thumbnailUrl = TigerPortraitImage,
        title = "The wild gaze",
        description = "A close portrait captures a tiger's quiet power among autumn leaves.",
        ageRestriction = "T13",
      ),
      ShortData(
        id = "short-football-motion",
        videoUrl = StreamUrls.MuxPtsShift,
        trailerUrl = StreamUrls.ShakaAngelOne,
        thumbnailUrl = FootballImage,
        title = "Motion on the pitch",
        description = "One decisive touch in a football match played at full speed.",
        ageRestriction = "P",
      ),
      ShortData(
        id = "short-basketball-reach",
        videoUrl = StreamUrls.MuxSampleAes,
        trailerUrl = StreamUrls.BitmovinSintel,
        thumbnailUrl = BasketballImage,
        title = "Above the rim",
        description = "Two players rise for a split-second contest above the basket.",
        ageRestriction = "P",
      ),
      ShortData(
        id = "short-tokyo-walk",
        videoUrl = StreamUrls.MuxIssue666,
        trailerUrl = StreamUrls.AppleBipBopTs,
        thumbnailUrl = TokyoStreetImage,
        title = "A minute in old Tokyo",
        description = "A quick walk through Asakusa where every corner holds a story.",
        ageRestriction = "P",
      ),
      ShortData(
        id = "short-festival-colors",
        videoUrl = StreamUrls.BigBuckBunnyAbr,
        trailerUrl = StreamUrls.AppleBipBopFmp4,
        thumbnailUrl = ChineseFestivalImage,
        title = "Festival colors",
        description = "Traditional costumes sweep past the camera in a burst of color.",
        ageRestriction = "P",
      ),
    )

    val documentarySeries = listOf(
      SeriesData(
        id = "series-wild-asia",
        videoUrl = StreamUrls.AppleBipBopTs,
        trailerUrl = StreamUrls.AppleHevc,
        thumbnailUrl = TigerForestImage,
        title = "Wild Asia",
        description = "A documentary series about Asia's landscapes and remarkable wildlife.",
        ageRestriction = "T13",
        episodes = listOf(
          VideoData(
            id = "episode-wild-asia-1",
            videoUrl = StreamUrls.AppleBipBopTs,
            trailerUrl = StreamUrls.TearsOfSteelTs,
            thumbnailUrl = TigerForestImage,
            title = "Episode 1: Predator of the forest",
            description = "Track a Bengal tiger through its natural habitat.",
            ageRestriction = "T13",
          ),
          VideoData(
            id = "episode-wild-asia-2",
            videoUrl = StreamUrls.TearsOfSteelTs,
            trailerUrl = StreamUrls.TearsOfSteelFmp4,
            thumbnailUrl = TigerPortraitImage,
            title = "Episode 2: Built to survive",
            description = "The adaptations that help large animals endure a demanding wilderness.",
            ageRestriction = "T13",
          ),
        ),
      ),
      SeriesData(
        id = "series-east-asia-culture",
        videoUrl = StreamUrls.TearsOfSteelTs,
        trailerUrl = StreamUrls.BigBuckBunnyAbr,
        thumbnailUrl = TokyoStreetImage,
        title = "Living heritage of East Asia",
        description = "Meet the people and living traditions of China and Japan.",
        ageRestriction = "P",
        episodes = listOf(
          VideoData(
            id = "episode-east-asia-1",
            videoUrl = StreamUrls.BigBuckBunnyAbr,
            trailerUrl = StreamUrls.ShakaAngelOne,
            thumbnailUrl = ChineseFestivalImage,
            title = "Episode 1: Colors of China",
            description = "A day inside a vibrant traditional festival.",
            ageRestriction = "P",
          ),
          VideoData(
            id = "episode-east-asia-2",
            videoUrl = StreamUrls.TearsOfSteelFmp4,
            trailerUrl = StreamUrls.BitmovinSintel,
            thumbnailUrl = TokyoStreetImage,
            title = "Episode 2: Old Tokyo",
            description = "Asakusa through the eyes of the people who live there.",
            ageRestriction = "P",
          ),
        ),
      ),
      SeriesData(
        id = "series-human-performance",
        videoUrl = StreamUrls.BigBuckBunnyAbr,
        trailerUrl = StreamUrls.AppleBipBopTs,
        thumbnailUrl = BasketballImage,
        title = "The edge of performance",
        description = "Athletes reveal how preparation becomes instinct when the pressure rises.",
        ageRestriction = "P",
        episodes = listOf(
          VideoData(
            id = "episode-performance-1",
            videoUrl = StreamUrls.AppleBipBopFmp4,
            trailerUrl = StreamUrls.AppleHevc,
            thumbnailUrl = BasketballImage,
            title = "Episode 1: Reading the court",
            description = "Basketball players make complex decisions in fractions of a second.",
            ageRestriction = "P",
          ),
          VideoData(
            id = "episode-performance-2",
            videoUrl = StreamUrls.AppleHevc,
            trailerUrl = StreamUrls.TearsOfSteelTs,
            thumbnailUrl = FootballImage,
            title = "Episode 2: Space and timing",
            description = "A football attack is built from movement before the ball arrives.",
            ageRestriction = "P",
          ),
        ),
      ),
      SeriesData(
        id = "series-rituals-of-asia",
        videoUrl = StreamUrls.TearsOfSteelFmp4,
        trailerUrl = StreamUrls.BigBuckBunnyAbr,
        thumbnailUrl = JapaneseCeremonyImage,
        title = "Rituals of Asia",
        description = "A respectful journey through ceremonies that connect past and present.",
        ageRestriction = "P",
        episodes = listOf(
          VideoData(
            id = "episode-rituals-1",
            videoUrl = StreamUrls.ShakaAngelOne,
            trailerUrl = StreamUrls.BitmovinSintel,
            thumbnailUrl = JapaneseCeremonyImage,
            title = "Episode 1: A language of gestures",
            description = "Every movement carries meaning in a traditional Japanese ceremony.",
            ageRestriction = "P",
          ),
          VideoData(
            id = "episode-rituals-2",
            videoUrl = StreamUrls.BitmovinSintel,
            trailerUrl = StreamUrls.AppleBipBopTs,
            thumbnailUrl = ChineseNewYearImage,
            title = "Episode 2: The color of renewal",
            description = "Families welcome a new year through symbols of luck and renewal.",
            ageRestriction = "P",
          ),
          VideoData(
            id = "episode-rituals-3",
            videoUrl = StreamUrls.MuxImscCaptions,
            trailerUrl = StreamUrls.AppleBipBopFmp4,
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
        videoUrl = StreamUrls.AkamaiLive,
        trailerUrl = StreamUrls.AppleHevc,
        thumbnailUrl = BasketballImage,
        title = "StreamTV Sport",
        description = "The day's biggest sporting moments, live every day.",
        ageRestriction = "P",
      ),
      ChannelData(
        id = "channel-nature-live",
        videoUrl = StreamUrls.ShakaLive,
        trailerUrl = StreamUrls.TearsOfSteelTs,
        thumbnailUrl = TigerForestImage,
        title = "StreamTV Nature",
        description = "An uninterrupted window into the wild, 24/7.",
        ageRestriction = "P",
      ),
      ChannelData(
        id = "channel-football-live",
        videoUrl = StreamUrls.AkamaiEightLive,
        trailerUrl = StreamUrls.TearsOfSteelFmp4,
        thumbnailUrl = FootballImage,
        title = "StreamTV Football",
        description = "Live matches, tactical analysis, and the stories behind the final score.",
        ageRestriction = "P",
      ),
      ChannelData(
        id = "channel-cricket-live",
        videoUrl = StreamUrls.AkamaiLive,
        trailerUrl = StreamUrls.BigBuckBunnyAbr,
        thumbnailUrl = CricketImage,
        title = "StreamTV Cricket",
        description = "International cricket and classic matches throughout the day.",
        ageRestriction = "P",
      ),
      ChannelData(
        id = "channel-culture-live",
        videoUrl = StreamUrls.ShakaLive,
        trailerUrl = StreamUrls.ShakaAngelOne,
        thumbnailUrl = ChineseFestivalImage,
        title = "StreamTV Culture",
        description = "Festivals, art, food, and living traditions from around the world.",
        ageRestriction = "P",
      ),
      ChannelData(
        id = "channel-city-live",
        videoUrl = StreamUrls.AkamaiEightLive,
        trailerUrl = StreamUrls.BitmovinSintel,
        thumbnailUrl = TokyoStreetImage,
        title = "StreamTV Cities",
        description = "A continuous window into the streets and rhythms of remarkable cities.",
        ageRestriction = "P",
      ),
    )
  }
}
