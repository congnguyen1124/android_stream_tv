# StreamTV

StreamTV là ứng dụng Android TV dùng Jetpack Compose for TV, được tổ chức theo Clean Architecture trong **một app module**. Toàn bộ nội dung hiển thị trong ứng dụng dùng tiếng Anh.

## Chức năng hiện tại

- TopBar điều hướng giữa Search, Home, Setting và Profile bằng D-pad.
- Home nhận một danh sách section dọc; mỗi section sở hữu `title`, `viewType` và danh sách content ngang.
- `Banner` hiển thị `Video` theo tỷ lệ 16:9, có thông tin nội dung, age restriction, indicator và tự chuyển trang khi không focus.
- `VerticalBanner` hiển thị `Short` theo tỷ lệ 2:3, đặt item active ở giữa, scale item và đổi nền theo thumbnail active.
- Focus đầu tiên thuộc về Banner; nhấn Up quay lại TopBar. Trái/phải chuyển item ngay trong carousel.
- Các model và dummy data cho `Videos`, `ListSeries`, `Channels`, `Shorts` đã có sẵn; UI của bốn view type này sẽ được bổ sung sau.
- Ảnh online được tải bằng Coil 3; `videoUrl` và `logoUrl` đang để trống theo yêu cầu.
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
│   ├── repository/             # HomeRepository contract
│   └── usecase/                # GetHomeSectionsUseCase
└── presentation/
    ├── component/              # Banner, VerticalBanner, section header
    ├── mapper/                 # Domain -> UI model
    ├── model/                  # UI item và UI viewType
    ├── HomeRoute.kt
    ├── HomeScreen.kt
    ├── HomeUiState.kt
    └── HomeViewModel.kt
```

App composition root nằm tại `app/di/HomeModule.kt`. Presentation không tự khởi tạo data source hoặc repository.

## Dependency injection với Hilt

- `StreamTvApplication` dùng `@HiltAndroidApp` để tạo application-level container.
- `MainActivity` dùng `@AndroidEntryPoint` để kết nối Android entry point với Hilt graph.
- `HomeModule` được cài vào `SingletonComponent`; module cung cấp `HomeDummyDataSource`, `HomeRepository`, `GetHomeSectionsUseCase` và `HomeUiMapper`.
- Toàn bộ `HomeViewModel`, `SearchViewModel`, `SettingViewModel`, `ProfileViewModel` dùng `@HiltViewModel` và constructor injection.
- Mọi feature Route lấy ViewModel bằng `hiltViewModel()`; không còn factory hoặc dependency container thủ công trong production code.

Domain giữ nguyên thuần Kotlin. Hilt wiring chỉ nằm tại app composition root và presentation entry point, nhờ đó có thể thay dummy repository bằng remote repository mà không sửa ViewModel hoặc UI.

## Luồng dữ liệu

```text
HomeDummyDataSource
    -> DummyHomeRepository
    -> HomeDataMapper
    -> GetHomeSectionsUseCase
    -> HomeViewModel
    -> HomeUiMapper
    -> HomeUiState
    -> HomeScreen
```

`Content` là sealed hierarchy gồm:

- `Video`: một video đơn lẻ, thumbnail 16:9.
- `Series`: content có thêm `episodes: List<Video>`.
- `Channel`: content phát live.
- `Short`: video dọc, thumbnail 2:3.

Mọi content có `id`, `videoUrl`, `thumbnailUrl`, `vastUrl`, `title`, `description`, `ageRestriction`, `logoUrl`. `id` được thêm để cung cấp stable key cho Compose; hậu tố `Url` của `logoUrl` làm rõ kiểu dữ liệu.

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
- Item TopBar khai báo hướng Down về content focus requester.

## Thêm UI cho view type còn lại

1. Tạo composable trong `feature/home/presentation/component`.
2. Thêm nhánh tương ứng trong `when (section.viewType)` của `HomeScreen.kt`.
3. Lấy typed item bằng `requireItemsOfType()`; mapper và model đã bảo vệ đúng loại item.
4. Đặt logic focus tại composable mới, không tạo modifier focus dùng chung toàn app.

## Build và test

Yêu cầu JDK 17 trở lên và Android SDK 37.

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebugAndroidTest
./gradlew :app:lintDebug
```

APK debug được tạo tại `app/build/outputs/apk/debug/app-debug.apk`.
