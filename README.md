# StreamTV

StreamTV là ứng dụng Android TV dùng Jetpack Compose for TV, được tổ chức theo Clean Architecture trong **một app module**. Toàn bộ nội dung hiển thị trong ứng dụng dùng tiếng Anh.

## Chức năng hiện tại

- TopBar điều hướng giữa Search, Home, Setting và Profile bằng D-pad. Khi navigation nhận focus, app phủ một lớp `surface` bán trong suốt lên toàn bộ screen và giữ TopBar nổi phía trên.
- TopBar còn có overlay riêng: gradient dọc từ `surface` xuống trong suốt, vẽ phía sau các item. Overlay do màn đang hiện bật/tắt — Home bật khi focus rời section đầu tiên, vì Banner full-bleed đã có scrim riêng.
- Home nhận một danh sách section dọc; mỗi section sở hữu `title`, `viewType` và danh sách content ngang.
- `Banner` full-width cao 600dp nằm phía sau TopBar overlay, dùng hero scrim nhiều lớp, CTA, edge pages, indicator và lifecycle-aware auto-scroll khi không focus.
- `Banner` phát trailer của item đang focus: thumbnail giữ 5 giây, sau đó video fade in đè lên thumbnail, chạy loop, không có controller. Rời focus, đổi item, mở player hoặc app xuống background đều dừng và unload player. Trailer lỗi hoặc thiếu `trailerUrl` thì banner vẫn là ảnh tĩnh — thumbnail không bao giờ bị bỏ đi. Chi tiết: `docs/home-banner-trailer/`.
- `VerticalBanner` hiển thị `Short` theo tỷ lệ 2:3, loop trên virtual pager dài khi có ít nhất 5 item, preload 5 page quanh viewport, scale item và đổi nền theo palette trích từ thumbnail active.
- Focus đầu tiên thuộc về Banner; nhấn Up quay lại TopBar. Trái/phải chuyển item ngay trong carousel.
- `Videos`, `ListSeries`, `Channels` và `Shorts` dùng `ContentRow`: horizontal lazy layout với selector cố định tại leading content edge và luôn đo thêm item ngoài hai biên để chuyển động không lộ khoảng trống.
- Thumbnail không bị làm tối theo selection; title chưa selected dùng màu sáng vừa phải để vẫn giữ hierarchy.
- Ảnh online được tải bằng Coil 3; dummy `videoUrl` và `trailerUrl` dùng HLS test stream (`trailerUrl` lấy từ cùng pool VOD nhưng xoay lệch một bậc nên không trùng `videoUrl` của chính item đó) và `logoUrl` đang để trống.
- Dependency injection dùng Hilt; graph được kiểm tra và tạo code tại compile time bằng KSP.

## Cấu trúc Home feature

```text
feature/home/
├── data/
│   ├── model/                  # DTO đa hình và viewType từ nguồn dữ liệu
│   ├── source/                 # HomeDummyDataSource
│   ├── mapper/                 # DTO -> domain
│   └── repository/             # DummyHomeRepository adapter
├── domain/
│   ├── model/                  # Content, Video, Series, Channel, Short, HomeSection
│   └── repository/             # Suspend HomeRepository contract
└── presentation/
    ├── component/              # HomeContent, Banner, BannerTrailer, VerticalBanner, ContentRow section, card
    ├── mapper/                 # Domain -> UI model
    ├── model/                  # UI item và UI viewType
    ├── HomeRoute.kt            # HomeScreen: bind ViewModel, cấp trailer slot cho banner
    ├── HomeUiState.kt
    ├── HomeViewModel.kt
    ├── HomeBannerTrailerUiState.kt   # UiState + pure fold quyết định khi nào hiện video
    └── HomeBannerTrailerViewModel.kt # sở hữu một player cho trailer của banner
```

App composition root nằm tại `app/di/HomeModule.kt`. Presentation không tự khởi tạo data source hoặc repository.

## Navigation

Navigation chia làm hai graph. `MainScreen` là shell duyệt nội dung: sở hữu `StreamTvTopBar` và một
`MainNavHost` lồng bên trong. Hai màn player là anh em của `MainScreen` ở graph ngoài, nên chiếm trọn
màn hình mà không ai phải ẩn TopBar.

```
StreamTvNavHost (ngoài)
├── MainRoute ─────────► MainScreen
│                        ├── StreamTvTopBar
│                        └── MainNavHost (trong)
│                            ├── HomeRoute
│                            ├── SearchRoute
│                            ├── SettingRoute
│                            └── ProfileRoute
├── PlayerRoute
└── VerticalPlayerRoute
```

- Một destination có TopBar khi và chỉ khi nó được đăng ký trong `MainNavHost`. Không còn predicate
  `isPlayerRoute` so khớp prefix route để quyết định ẩn bar.
- Back không cần code thêm: NavHost trong xử lý trước, hết stack thì rơi xuống NavHost ngoài.
- `MainNavHost` không thấy controller ngoài, nên mở player đi qua `onOpenPlayer` /
  `onOpenVerticalPlayer`. Chọn player nào vẫn do `HomeContentUiItem.playerTarget()` quyết định.
- Mỗi feature duyệt nội dung chỉ còn **một** composable `XxxScreen`, đặt trong `XxxRoute.kt`. Home
  giữ phần UI stateless tách riêng là `HomeContent` vì nó lớn và là thứ Compose test chạy vào.

Chi tiết quyết định, phương án thay thế và hệ quả: `docs/adr/2026-09-01-nested-main-navigation.md`.

## Dependency injection với Hilt

- `StreamTvApplication` dùng `@HiltAndroidApp` để tạo application-level container.
- `MainActivity` dùng `@AndroidEntryPoint` để kết nối Android entry point với Hilt graph.
- `HomeModule` được cài vào `SingletonComponent`; module cung cấp `HomeDummyDataSource`, `HomeRepository` và `HomeUiMapper`.
- `PlayerModule` cung cấp `StreamTvPlayerFactory`, `PlayerDummyDataSource` và
  `PlayerDetailsRepository`; UI không khởi tạo data source hoặc ExoPlayer trực tiếp.
- Toàn bộ `HomeViewModel`, `SearchViewModel`, `SettingViewModel`, `ProfileViewModel` dùng `@HiltViewModel` và constructor injection.
- Mọi feature Route lấy ViewModel bằng `hiltViewModel()`; không còn factory hoặc dependency container thủ công trong production code.

Domain giữ nguyên thuần Kotlin. Hilt wiring chỉ nằm tại app composition root và presentation entry point, nhờ đó có thể thay dummy repository bằng remote repository mà không sửa ViewModel hoặc UI.

## Luồng dữ liệu

```text
HomeDummyDataSource
    -> DummyHomeRepository
    -> HomeDataMapper
    -> HomeViewModel
    -> HomeUiMapper
    -> HomeUiState
    -> HomeScreen
    -> HomeContent
```

`HomeViewModel` gọi suspend repository trực tiếp trong `viewModelScope`, hủy request cũ khi reload và không nuốt `CancellationException`. `PlayerViewModel` gọi `PlayerDetailsRepository` trực tiếp (không có use case trung gian), rồi `combine` playback state với content/action state thành immutable `PlayerUiState` bằng `stateIn(SharingStarted.Eagerly)`.

`Content` là sealed hierarchy gồm:

- `Video`: một video đơn lẻ, thumbnail 16:9.
- `Series`: content có thêm `episodes: List<Video>`.
- `Channel`: content phát live.
- `Short`: video dọc, thumbnail 2:3.

Mọi content có `id`, `videoUrl`, `trailerUrl`, `thumbnailUrl`, `vastUrl`, `title`, `description`, `ageRestriction`, `logoUrl`. `id` được thêm để cung cấp stable key cho Compose; hậu tố `Url` của `logoUrl` làm rõ kiểu dữ liệu.

Quan hệ hợp lệ được kiểm tra tại constructor của `HomeSection` và `HomeSectionUiItem`:

| `viewType` | Loại item bắt buộc |
|---|---|
| `Banner` | `Video` |
| `VerticalBanner` | `Short` |
| `Videos` | `Video` |
| `ListSeries` | `Series` |
| `Channels` | `Channel` |
| `Shorts` | `Short` |

Section sai kiểu bị từ chối ngay tại boundary thay vì bị lọc âm thầm trong Compose.

## Ảnh dummy

Dummy thumbnail dùng ảnh từ Pexels cho các chủ đề sport, animal, Chinese culture và Japanese culture. Các trang ảnh gốc:

Dummy sections cố ý bao gồm cả hai boundary của `ContentRow`: Videos có 8 item, Channels có 6 item và Shorts có 8 item để chạy loop; Documentary Series có 4 item để giữ finite.

- [Basketball](https://www.pexels.com/photo/men-playing-basketball-9839903/)
- [Football](https://www.pexels.com/photo/soccer-player-on-field-during-match-36958062/)
- [Cricket](https://www.pexels.com/photo/a-man-holding-a-wooden-paddle-11023865/)
- [Bengal tiger](https://www.pexels.com/photo/tiger-in-a-forest-25785873/)
- [Tiger portrait](https://www.pexels.com/photo/photo-of-a-tiger-12167844/)
- [Chinese festival](https://www.pexels.com/photo/vibrant-traditional-chinese-cultural-festival-30765119/)
- [Chinese New Year](https://www.pexels.com/photo/young-woman-celebrating-lunar-new-year-outdoors-36603900/)
- [Tokyo street](https://www.pexels.com/photo/people-walking-in-city-in-japan-12343886/)
- [Japanese ceremony](https://www.pexels.com/photo/traditional-japanese-ceremony-with-participants-31370378/)

## Focus trên Android TV

StreamTV không dùng `FocusRequesterModifiers` của dự án tham chiếu. Hành vi focus được khai báo tại composable sở hữu nó:

- `HomeBannerSection` gắn `contentFocusRequester` và khai báo `up = topBarFocusRequester`.
- Banner và VerticalBanner tự xử lý D-pad trái/phải bằng `onPreviewKeyEvent`.
- Auto-scroll dừng khi carousel nhận focus.
- Trailer chỉ chạy khi carousel đang giữ focus và screen đang RESUMED. Một `LaunchedEffect` key theo `(item, isBannerFocused, isScreenResumed)` lo cả delay 5 giây lẫn điểm dừng duy nhất trong `finally`, nên mọi đường ra — mất focus, đổi item, dispose, navigate — đều dừng player.
- Item TopBar khai báo hướng Down về content focus requester.
- `HomeContent` theo dõi section nào đang giữ focus qua `onFocusChanged` và bật overlay TopBar khi index lớn hơn 0. `MainScreen` tự hạ overlay mỗi lần đổi destination.

### ContentRow

Base component nằm tại `core/designsystem/component/contentrow` và cung cấp DSL gần với `LazyRow`:

```kotlin
val state = rememberContentRowState()

ContentRow(state = state) {
    items(
        items = videos,
        key = VideoUiItem::id,
    ) { video ->
        VideoCard(video)
    }
}
```

- `ContentRow` được xây trên `LazyLayout`; chỉ item trong vùng nhìn thấy và vùng đệm sát viewport được compose/measure.
- Toàn bộ row chỉ có một focus target là `SelectedItem` trong suốt có border, cố định tại leading content edge. Card bên dưới không được gắn `focusable`.
- Border selector rộng hơn content 2dp ở mỗi cạnh, tạo khoảng thở mà không thay đổi kích thước card.
- D-pad Left/Right dịch chuyển danh sách bên dưới selector; Center/Enter gọi callback của real selected index.
- Khi di chuyển sang phải, item trước trượt ra ngoài leading edge nhưng vẫn để lại một phần nhỏ ở mép màn hình trong lúc row đang ở các index tiếp theo.
- Ở item `0`, không có item giả phía trái và D-pad Left trả quyền xử lý về `FocusRequester.Default`.
- Khi có hơn 5 item, provider nối thêm một cycle đầy đủ của collection. Vì vậy ở gần cuối row vẫn luôn thấy các item `0, 1, 2...` phía sau; sau animation qua cuối, state rebase về cycle đầu mà không tạo khoảng trống hoặc nhảy hình.
- Collection có tối đa 5 item giữ finite: D-pad Right tại item cuối không reset về item `0`.
- `ContentRowState.scrollToItem(index)` wrap index đối với row loop và clamp index đối với row finite.

## Player

Player là destination full-screen ở graph ngoài và có hai cách trình bày dùng chung một
`PlayerViewModel`:

- `PlayerScreen` phát nội dung ngang bằng surface 16:9. Controller là overlay trong `Box`, tự ẩn sau
  5 giây khi video đang chạy và chỉ có title, Like, Save, Comment, Settings cùng duration/progress; không có
  related/episodes và không dùng `LazyColumn` cho content.
- `VerticalPlayerScreen` giữ stage 9:16 ở giữa lệch trái, nền ambient tối và interaction section ở
  bên phải. D-pad Right đi từ player sang action đầu tiên; D-pad Left quay về player.
- Cả hai orientation dùng chung retained section tree trong `component/section`: Metadata,
  Comments → Replies → Reply detail và Settings → Quality/Subtitles/Audio. Parent vẫn được compose
  phía dưới child để giữ list state và item đã chọn.
- Khi section bắt đầu enter hoặc child bắt đầu exit, focus được chuyển vào một pending target luôn
  tồn tại. Sau animation, section mới nhận focus; điều này ngăn Compose tự nhảy focus về player hoặc
  một control khác trong lúc node đang biến mất.
- Player ngang nhớ chính xác control đã mở root section: Back từ Metadata về Title, từ Comments về
  Comment và từ Settings về Settings. Back khi controller đang hiện chỉ ẩn controller; Back tiếp theo
  mới thoát player.
- Player dọc dùng Left hoặc Back để pop từng level. Child trở về đúng row đã mở nó; root trở về portrait
  player, sau đó Right mới đưa focus sang interaction section.
- Track snapshot từ `stream_player` được map một lần tại `presentation/mapper`. Chọn option dispatch
  trực tiếp `selectVideoTrack`, `selectTextTrack` hoặc `selectAudioTrack` qua `PlayerViewModel`.
- Settings không hiển thị category rỗng; Subtitles có option Off và Quality có Auto khi manifest có
  nhiều rendition.
- Metadata và comment dummy lấy từ `PlayerDummyDataSource`, qua
  `DummyPlayerDetailsRepository -> domain model -> PlayerDetailsUiMapper -> PlayerUiState`; toàn bộ copy
  hiển thị là tiếng Anh.

## Thêm một ContentRow section

1. Map view type về một `HomeContentRowStyle` trong `HomeScreen.kt`.
2. Truyền typed items qua `requireItemsOfType()`; mapper và model đã bảo vệ đúng loại item.
3. Render card trong DSL của `ContentRow`, cung cấp stable `key` và `contentType`.
4. Không gắn `focusable` vào card; focus, loop và D-pad đã được encapsulate trong base component.

## Build và test

Yêu cầu JDK 17 trở lên và Android SDK 37.

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebugAndroidTest
./gradlew :app:lintDebug
```

APK debug được tạo tại `app/build/outputs/apk/debug/app-debug.apk`.
