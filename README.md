# StreamTV

StreamTV là bộ khung Android TV dùng Jetpack Compose for TV, được tổ chức theo Clean Architecture trong **một app module**. Phiên bản đầu tiên cung cấp màn Home, TopBar, design system và luồng focus bằng D-pad.

## Trạng thái hiện tại

- Màn Home là start destination.
- Một nút hành động nằm giữa màn hình và tự nhận focus khi mở app.
- Nhấn nút sẽ cập nhật `HomeUiState` thông qua `HomeViewModel`.
- TopBar có bốn item Search, Home, Setting và Profile; hiện chỉ Home là destination có nội dung.
- Nhóm item được neo bên phải. Search, Home và Setting chỉ hiện icon khi không có focus, sau đó mở rộng để hiện tên khi nhận focus; Profile luôn giữ dạng icon tròn.
- Vector icon và font Roboto được dùng lại từ UI của dự án tham chiếu.
- Không có `FocusRequesterModifiers` hoặc helper focus-restorer cũ.

## Cấu trúc thư mục

```text
app/src/main/java/com/congnguyencn/stream_tv/
├── MainActivity.kt                         # Android entry point
├── app/
│   ├── StreamTvApp.kt                      # App shell và focus giữa TopBar/Home
│   └── navigation/
│       ├── StreamTvNavHost.kt
│       └── StreamTvTopBarItems.kt
├── core/designsystem/
│   ├── component/
│   │   ├── StreamTvAppBar.kt
│   │   ├── StreamTvButton.kt
│   │   ├── StreamTvSurface.kt
│   │   ├── StreamTvTopBar.kt
│   │   └── StreamTvTopBarItem.kt
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   └── tokens/StreamTvDimensions.kt
└── feature/home/presentation/
    ├── HomeRoute.kt
    ├── HomeScreen.kt
    ├── HomeUiState.kt
    ├── HomeViewModel.kt
    └── navigation/HomeNavigation.kt
```

## Quy tắc Clean Architecture

Dự án không tách Gradle module, nhưng vẫn giữ seam giữa các package:

- `app`: ghép các feature, quản lý navigation và app-level focus.
- `core/designsystem`: theme, token và composable base; không chứa nghiệp vụ của feature.
- `feature/<name>/presentation`: route, screen, `UiState`, ViewModel và navigation entry của feature.
- `feature/<name>/domain`: thêm khi feature bắt đầu có entity/use case nghiệp vụ thuần Kotlin.
- `feature/<name>/data`: thêm khi có repository adapter, local source hoặc remote source.

Không tạo sẵn repository/use case rỗng cho Home vì UI hiện chưa có nguồn dữ liệu hay nghiệp vụ cần một seam riêng. Khi thêm dữ liệu, interface repository đặt ở `domain`, adapter đặt ở `data`, và ViewModel chỉ gọi use case.

## Focus trên Android TV

Focus được khai báo trực tiếp tại nơi sở hữu hành vi:

- `HomeScreen` gắn `FocusRequester` vào nút và gọi `requestFocus()` trong `LaunchedEffect`.
- Nút khai báo `up = topBarFocusRequester`.
- Item trên TopBar khai báo `down = contentFocusRequester`.
- Container TopBar dùng `focusProperties.onEnter` để trả focus về destination đang chọn.

Cách này giúp luồng focus đọc được ngay tại screen/composable và không phụ thuộc vào wrapper modifier dùng chung.

## Thêm feature mới

1. Tạo `feature/<feature-name>/presentation`.
2. Chỉ thêm `domain` và `data` khi có nghiệp vụ hoặc nguồn dữ liệu thật.
3. Khai báo route trong file `navigation/<Feature>Navigation.kt`.
4. Đăng ký route trong `StreamTvNavHost.kt`.
5. Thêm `StreamTvTopBarItem` và xử lý điều hướng trong `StreamTvApp.kt`.
6. Dùng component, màu, typography và dimensions từ `core/designsystem`.

## Build và test

Yêu cầu JDK 17 trở lên và Android SDK 37.

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebugAndroidTest
```

APK debug được tạo tại `app/build/outputs/apk/debug/app-debug.apk`.
