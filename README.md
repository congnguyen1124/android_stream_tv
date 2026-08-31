# StreamTV

StreamTV là bộ khung Android TV dùng Jetpack Compose for TV, được tổ chức theo Clean Architecture trong **một app module**. Ứng dụng hiện có bốn màn hình Home, Search, Setting, Profile cùng TopBar và luồng focus bằng D-pad.

## Trạng thái hiện tại

- Home là start destination; Search, Setting và Profile là các top-level destination độc lập.
- Mỗi màn hình có một nút hành động nằm giữa và tự nhận focus sau khi điều hướng.
- Nhấn nút cập nhật `UiState` thông qua ViewModel riêng của feature.
- TopBar điều hướng đầy đủ giữa Search, Home, Setting và Profile.
- Nhóm item được neo bên phải. Search, Home và Setting chỉ hiện icon khi không có focus, sau đó mở rộng để hiện tên khi nhận focus; Profile luôn giữ dạng icon tròn.
- Bộ VectorDrawable navigation được thiết kế riêng cho StreamTV: Search có sparkle, Home tích hợp biểu tượng play, Setting dùng thanh tinh chỉnh và Profile có điểm nhấn nhận diện.
- Font Roboto được dùng lại từ UI của dự án tham chiếu.
- Không có `FocusRequesterModifiers` hoặc helper focus-restorer cũ.

## Cấu trúc thư mục

```text
app/src/main/java/com/congnguyencn/stream_tv/
├── MainActivity.kt                         # Android entry point
├── app/
│   ├── StreamTvApp.kt                      # App shell và focus giữa TopBar/nội dung
│   └── navigation/
│       ├── StreamTvNavHost.kt
│       └── StreamTvTopBarItems.kt
├── core/designsystem/
│   ├── component/
│   │   ├── StreamTvAppBar.kt
│   │   ├── StreamTvActionScreen.kt
│   │   ├── StreamTvButton.kt
│   │   ├── StreamTvSurface.kt
│   │   ├── StreamTvTopBar.kt
│   │   └── StreamTvTopBarItem.kt
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   └── tokens/StreamTvDimensions.kt
└── feature/
    ├── home/presentation/
    ├── search/presentation/
    ├── setting/presentation/
    └── profile/presentation/
        ├── <Feature>Route.kt
        ├── <Feature>Screen.kt
        ├── <Feature>UiState.kt
        ├── <Feature>ViewModel.kt
        └── navigation/<Feature>Navigation.kt
```

## Quy tắc Clean Architecture

Dự án không tách Gradle module, nhưng vẫn giữ seam giữa các package:

- `app`: ghép các feature, quản lý navigation và app-level focus.
- `core/designsystem`: theme, token và composable base; không chứa nghiệp vụ của feature.
- `feature/<name>/presentation`: route, screen, `UiState`, ViewModel và navigation entry của feature.
- `feature/<name>/domain`: thêm khi feature bắt đầu có entity/use case nghiệp vụ thuần Kotlin.
- `feature/<name>/data`: thêm khi có repository adapter, local source hoặc remote source.

Không tạo sẵn repository/use case rỗng vì các màn hình hiện chưa có nguồn dữ liệu hay nghiệp vụ cần một seam riêng. Khi thêm dữ liệu, interface repository đặt ở `domain`, adapter đặt ở `data`, và ViewModel chỉ gọi use case.

## Focus trên Android TV

Focus được khai báo trực tiếp tại nơi sở hữu hành vi:

- `StreamTvActionScreen` gắn `FocusRequester` vào nút và gọi `requestFocus()` trong `LaunchedEffect` cho mọi feature.
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
