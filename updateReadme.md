# Cách cập nhật ảnh cho README.md

Tài liệu vận hành cho việc chụp lại ảnh/GIF trong [`README.md`](README.md). Đọc file này **trước
khi** đụng vào ảnh trong [`docs/images/`](docs/images/).

Câu hỏi file này trả lời:

- Đổi UI ở file `X.kt` thì phải chụp lại **những capture nào**?
- Chỗ này nên là **ảnh tĩnh hay GIF**?
- Vào màn đó bằng **đường phím nào**, và dùng **item nội dung nào**?
- Làm màn hình mới thì thêm capture kiểu gì?

> **Đây là tài liệu sống.** Thêm màn hình, thêm section, đổi thứ tự dummy data, hay đổi đường phím
> vào một màn — đều phải cập nhật file này trong **cùng một change**. Một bảng ánh xạ sai còn tệ hơn
> không có bảng, vì nó khiến người sau tin là mình đã chụp đủ.

---

## 1. Chuẩn bị

```bash
adb devices          # phải thấy ĐÚNG MỘT device
ffmpeg -version      # cần cho cả WebP lẫn GIF
./gradlew :app:installDebug
```

Công cụ chụp là [`tools/capture_media.py`](tools/capture_media.py). Nó tự force-stop rồi mở lại app
trước mỗi capture, nên không capture nào thừa hưởng focus của capture trước.

```bash
python3 tools/capture_media.py list              # xem toàn bộ capture và loại của nó
python3 tools/capture_media.py shot <tên>        # chụp một ảnh tĩnh
python3 tools/capture_media.py gif <tên>         # quay một GIF
python3 tools/capture_media.py all               # chụp lại toàn bộ (~12 phút)
```

Đầu ra luôn là `docs/images/<tên>.webp` hoặc `docs/images/<tên>.gif` — **tên capture chính là tên
file**, không cần đổi gì trong README nếu giữ nguyên tên.

**Không chạy `all` khi chỉ sửa một màn.** Chạy `all` mất ~12 phút và tạo diff rác trên những ảnh
không liên quan (video đang phát ở frame khác nhau). Chỉ chạy `all` khi đổi theme/token dùng chung.

---

## 2. Ảnh tĩnh hay GIF?

Quy tắc duy nhất: **thứ cần chứng minh có nằm ở sự thay đổi giữa các frame không?**

| Dùng | Khi điều cần nói là | Ví dụ trong README |
|---|---|---|
| **Ảnh tĩnh** (`shot`) | Bố cục, thứ bậc thị giác, màu, hoặc **một trạng thái cuối** | `player-controller`, `setting`, `calendar` |
| **GIF** (`gif`) | Focus di chuyển, animation, hand-off, hoặc **quan hệ nhân quả giữa hai trạng thái** | `player-focus-restore`, `topbar-focus`, `home-banner-trailer` |

GIF đắt hơn nhiều: 1–3 MB mỗi cái so với 20–160 KB cho ảnh tĩnh. Chỉ dùng GIF khi một ảnh tĩnh thực
sự **không thể** nói được điều đó. Ba trường hợp hiện đang xứng đáng dùng GIF:

- **Restore focus** — điểm mấu chốt là "xuống lại đúng nút cũ", tức là so sánh frame đầu và frame cuối.
- **Hand-off** — thumbnail → trailer, không có frame nào đứng một mình diễn tả được.
- **Item mở rộng khi nhận focus** — TopBar giãn ngang lộ nhãn.

Ngược lại: "section mở ra ở mép phải" là **trạng thái**, không phải chuyển động → ảnh tĩnh.

---

## 3. Dùng item nội dung nào

### Quy ước chung

Mọi demo player dùng **stream Big Buck Bunny**, để hai orientation so sánh được với nhau và để người
đọc nhận ra ngay đây là cùng một nội dung.

| Demo | Item | Stream | Đường tới |
|---|---|---|---|
| Player ngang | `video-tokyo-culture` — *Tokyo: Tradition in motion* | `BigBuckBunnyAbr` | Banner, `RIGHT` ×2 |
| Player dọc | `short-festival-colors` — *Festival colors* | `BigBuckBunnyAbr` | Rail *Fresh shorts*, item đầu |
| Trailer banner | `video-chinese-festival` — *Colors of a Chinese festival* | trailer = `BigBuckBunnyAbr` | Banner, `RIGHT` ×3 |

### Ba cái bẫy về dummy data

1. **`trailerUrl` xoay lệch một bậc so với `videoUrl`.** Item có *video* là Big Buck Bunny **không**
   phải item có *trailer* là Big Buck Bunny. Demo trailer vì thế dùng banner item thứ 4, còn demo
   player dùng banner item thứ 3. Lần đầu chụp sai chỗ này ra một cái đồng hồ test-pattern của Apple,
   trông y như bug.
2. **`short-cricket-focus` dùng `JwPlayerBigBuckBunny` và stream này trả 404 từ device.** Đừng chọn
   nó cho demo dù tên có chữ BigBuckBunny.
3. **Rail *Fresh shorts* là `discoveryShorts.reversed()`**, nên item đầu của nó là item **cuối** trong
   danh sách nguồn. Đổi thứ tự `discoveryShorts` là đổi luôn item mà player dọc mở ra.

Nguồn: [`HomeDummyDataSource.kt`](app/src/main/java/com/congnguyencn/stream_tv/feature/home/data/source/HomeDummyDataSource.kt).

### Thứ tự điều hướng (dùng để tính số phím)

**Section trong Home, từ trên xuống** — số phím `DPAD_DOWN` từ Banner:

| # | Section | viewType |
|---|---|---|
| 0 | Featured (hero) | `Banner` |
| 1 | Videos for you | `Videos` |
| 2 | Popular videos | `VideosPopular` |
| 3 | Documentary series | `ListSeries` |
| 4 | Live channels | `Channels` |
| 5 | Portrait discoveries | `VerticalBanner` |
| 6 | Fresh shorts | `Shorts` |
| 7 | Popular shorts | `ShortPopular` |

**TopBar, từ trái sang phải**: Search, Home, Calendar, Setting, Profile. Home được chọn sẵn lúc mở
app, nên từ Home: `LEFT` ×1 tới Search, `RIGHT` ×1 tới Calendar, ×2 tới Setting, ×3 tới Profile.

**Control row của player ngang**, focus vào là ở play/pause:

| Phím từ play/pause | Tới |
|---|---|
| `LEFT` ×1 | Rewind |
| `LEFT` ×2 | Pill `Description` |
| `RIGHT` ×1 | Forward |
| `RIGHT` ×2 | Like |
| `RIGHT` ×3 | Save |
| `RIGHT` ×4 | Comment |
| `RIGHT` ×5 | Settings |
| `UP` | Seek bar |

**Interaction panel của player dọc**: `RIGHT` từ stage vào thẳng **action đầu tiên** (Like), *không*
phải title block. Title block ở **một bước `UP`** từ đó.

---

## 4. Danh sách capture hiện có

| Tên capture | Loại | Nội dung | Đường vào (sau khi mở app) |
|---|---|---|---|
| `home-overview` | shot | Hero banner + rail đầu | — |
| `home-banner-trailer` | **gif** | Thumbnail → trailer fade in | `RIGHT` ×3, chờ 5s |
| `home-rows` | shot | Rail *Popular videos* xếp hạng | `DOWN` ×2 |
| `home-series` | shot | Rail *Documentary series* | `DOWN` ×3 |
| `home-channels` | shot | Rail *Live channels* | `DOWN` ×4 |
| `home-vertical-banner` | shot | Portrait carousel | `DOWN` ×5 |
| `home-shorts` | shot | Rail *Fresh shorts* | `DOWN` ×6 |
| `home-row-navigation` | **gif** | Xuống rail rồi sang phải trong rail | `DOWN`, `RIGHT` ×2, `DOWN`, `RIGHT` ×2 |
| `topbar-focus` | **gif** | Item TopBar giãn ngang lộ nhãn | `UP`, `LEFT` ×3, `RIGHT` ×2 |
| `search` | shot | Search | `UP`, `LEFT`, `CENTER`, `DOWN` |
| `calendar` | shot | EPG lưới | `UP`, `RIGHT`, `CENTER`, `DOWN` |
| `setting` | shot | Setting hai pane | `UP`, `RIGHT` ×2, `CENTER`, `DOWN` |
| `profile` | shot | Profile / QR sign-in | `UP`, `RIGHT` ×3, `CENTER`, `DOWN` |
| `player-surface` | shot | Player ngang, không chrome | `RIGHT` ×2, `CENTER`, chờ 14s |
| `player-controller` | shot | Controller hiện, focus play/pause | ↑ rồi `UP` |
| `player-focus-restore` | **gif** | Save → seek bar → về lại Save | ↑ rồi `RIGHT` ×3, `UP`, `DOWN` |
| `player-metadata-section` | shot | Section Metadata | ↑ rồi `LEFT` ×2, `CENTER` |
| `player-comments-section` | shot | Section Comments | ↑ rồi `RIGHT` ×4, `CENTER` |
| `player-settings-section` | shot | Settings → Quality | ↑ rồi `RIGHT` ×5, `CENTER`, `CENTER` |
| `vertical-player` | shot | Stage 9:16 + interaction panel | `DOWN` ×6, `CENTER`, chờ 18s |
| `vertical-player-metadata` | shot | Section trong suốt trên nền ambient | ↑ rồi `RIGHT`, `UP`, `CENTER` |
| `vertical-player-panel` | **gif** | Stage → panel → dịch trong action row | ↑ rồi `RIGHT`, `UP`, `DOWN`, `RIGHT` ×2, `LEFT` |

Định nghĩa đầy đủ (kể cả `settle`, `step_delay`, `duration`) nằm trong dict `CAPTURES` của
[`tools/capture_media.py`](tools/capture_media.py).

---

## 5. Sửa file nào thì chụp lại capture nào

Đây là bảng tra chính. Cột trái là thứ vừa sửa, cột phải là **toàn bộ** capture cần chạy lại.

### Player

| Sửa | Chụp lại |
|---|---|
| `PlayerController.kt`, `PlayerControlRow.kt`, `PlayerIconButton.kt` | `player-controller`, `player-focus-restore` |
| `PlayerSeekBar.kt`, `PlayerSeekPreviewCard.kt` | `player-controller`, `player-focus-restore` |
| `PlayerScreen.kt`, `PlayerFocusableGroup.kt`, `PlayerOverlay.kt` | `player-surface`, `player-controller`, `player-focus-restore`, cả 3 section shot |
| `VerticalPlayerScreen.kt`, `VerticalPlayerFocusableSurface.kt` | `vertical-player`, `vertical-player-panel`, `vertical-player-metadata` |
| `section/PlayerMetadataSection.kt` | `player-metadata-section`, `vertical-player-metadata` |
| `section/PlayerCommentSections.kt`, `PlayerCommentScrollableItem.kt` | `player-comments-section` |
| `section/PlayerSettingSections.kt` | `player-settings-section` |
| `section/PlayerSideSection.kt`, `AnimatedPlayerSection.kt` | cả 4 section shot (hai orientation) |

Ví dụ cụ thể — **sửa UI của `PlayerController.kt`**:

```bash
./gradlew :app:installDebug
python3 tools/capture_media.py shot player-controller
python3 tools/capture_media.py gif player-focus-restore
```

Rồi mở [`README.md`](README.md) mục *4. Player ngang*, đọc lại caption dưới hai ảnh đó xem còn đúng
không. Tên file không đổi nên không phải sửa đường dẫn.

### Home

| Sửa | Chụp lại |
|---|---|
| `HomeBannerSection.kt`, `HomeBannerInfo.kt`, `HomeBannerGradients.kt`, `HomeBannerDotsIndicator.kt` | `home-overview` |
| `HomeBannerTrailer.kt`, `HomeBannerTrailerViewModel.kt` | `home-banner-trailer` |
| `HomeVerticalBannerSection.kt` | `home-vertical-banner` |
| `HomeContentRowSection.kt`, card của rail | `home-rows`, `home-series`, `home-channels`, `home-shorts` |
| `core/designsystem/component/contentrow/**` | `home-row-navigation` + 4 rail shot ở trên |

### Shell và các destination khác

| Sửa | Chụp lại |
|---|---|
| `MainTopBarItems.kt`, `MainRoute.kt` | `topbar-focus` **và mọi shot có TopBar** — tức là toàn bộ mục 1 và 3 của README |
| `SearchContent.kt`, `SearchContentRow.kt` | `search` |
| `CalendarScreen.kt` | `calendar` |
| `SettingContent.kt` | `setting` |
| `ProfileContent.kt` | `profile` |

### Thay đổi lan rộng

| Sửa | Chụp lại |
|---|---|
| `core/designsystem/theme/**`, `tokens/**` | `all` |
| `HomeDummyDataSource.kt` — đổi thứ tự section hoặc item | `all`, **và** kiểm tra lại mọi đường phím ở mục 3 và 4 của file này |

---

## 6. Làm một màn hình mới

Bốn bước, làm hết trong cùng một change:

1. **Thêm entry vào `CAPTURES`** trong [`tools/capture_media.py`](tools/capture_media.py). Đặt tên
   kebab-case theo dạng `<màn>-<thứ-cần-nói>`, ví dụ `library-grid`, `library-focus-restore`. Tách
   `setup` (đường phím để tới nơi, không quay) khỏi `steps` (chính phần biểu diễn) — GIF chỉ nên
   chứa phần biểu diễn.
2. **Nếu là GIF thì thêm tên vào `GIF_CAPTURES`.** Không thêm thì nó bị chụp thành ảnh tĩnh.
3. **Chạy thử và xem lại kết quả bằng mắt.** Đừng tin là nó đúng chỉ vì lệnh chạy xong không lỗi —
   đường phím sai vẫn cho ra một tấm ảnh hợp lệ của màn hình sai.
4. **Nhúng vào [`README.md`](README.md)** kèm một caption *in nghiêng* nói điều mà ảnh chứng minh,
   không phải mô tả lại thứ nhìn thấy được. Rồi **cập nhật mục 4 và 5 của file này**.

Song song đó, [`AGENTS.md`](AGENTS.md) vẫn yêu cầu mỗi màn mới có một `spec/<màn>.md`.

---

## 7. Checklist trước khi commit ảnh

- [ ] Đã **mở từng ảnh mới ra xem**. Đường phím sai vẫn tạo file thành công.
- [ ] Không có ảnh nào bị bắt **giữa animation** — panel section phải đứng yên hẳn.
- [ ] Không có ảnh player nào còn **buffering** hoặc đang ở frame đen đầu stream.
- [ ] Ảnh destination **không bị lớp dim của TopBar** phủ lên.
- [ ] Player dùng đúng nội dung Big Buck Bunny.
- [ ] `python3 tools/capture_media.py list` khớp với bảng ở mục 4 của file này.
- [ ] Mọi đường dẫn ảnh trong README đều tồn tại, và mọi file trong `docs/images/` đều được dùng:

```bash
python3 - <<'PY'
import re, io, os
s = io.open('README.md', encoding='utf-8').read()
refs = set(re.findall(r'!\[[^\]]*\]\(([^)]+)\)', s)) | set(re.findall(r'<img src="([^"]+)"', s))
print("THIẾU FILE:", [r for r in sorted(refs) if not os.path.exists(r)] or "không")
print("ẢNH THỪA:", sorted({f for f in os.listdir('docs/images')} - {os.path.basename(r) for r in refs}) or "không")
PY
```

---

## 8. Những lỗi đã gặp, đừng gặp lại

| Triệu chứng | Nguyên nhân | Cách xử lý |
|---|---|---|
| Section bị bắt giữa lúc trượt vào | Khoảng nghỉ sau `steps` chỉ 0.6s, đủ cho một bước focus chứ không đủ cho animation panel | Thêm `"sleep:2"` vào cuối `steps` |
| Ảnh destination bị mờ xám | Chọn destination xong focus vẫn ở TopBar, và TopBar phủ dim lên nội dung | Kết thúc `steps` bằng `DPAD_DOWN` |
| Player ra frame đen hoặc đang buffer | Chưa chờ stream render xong | `sleep:14` cho player ngang, `sleep:18` cho player dọc |
| Trailer ra đồng hồ test-pattern | Chọn nhầm item — `trailerUrl` xoay lệch một bậc | Xem mục 3 |
| Vào nhầm màn | Đếm sai `DPAD_DOWN`, hoặc thứ tự section/TopBar đã đổi | Đối chiếu hai bảng thứ tự ở mục 3 |
| Panel Settings chỉ có một dòng | Đúng như thiết kế — stream này không có phụ đề/audio thay thế, và Settings không hiện category rỗng | Bấm `CENTER` thêm một lần để vào danh sách Quality |
| GIF quá nặng (>3 MB) | `duration` dài, hoặc nền là video đang chạy nên mọi frame đều khác nhau | Rút ngắn `duration`, bớt `steps` |
| Emulator không tải được stream dù ping được | TLS của emulator hỏng sau khi chạy lâu | `adb reboot`, chờ boot xong rồi chụp lại |
| `am start` báo lỗi, không capture nào chạy được | `connectedDebugAndroidTest` gỡ app sau khi chạy xong | `./gradlew :app:installDebug` rồi chụp lại |
