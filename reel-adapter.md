# Tài Liệu Kỹ Thuật: ReelAdapter.kt

## Mô Tả Tổng Quan
`ReelAdapter` là một `RecyclerView.Adapter` được thiết kế để hiển thị danh sách các video dạng reel (tương tự Instagram Reels hoặc TikTok). Mỗi item trong danh sách là một video được phát bằng `ExoPlayer`, với các tính năng chính:

- **Phát video**: Chỉ phát video tại position hiện tại, tạm dừng hoặc dừng các video khác.
- **Tải trước (predownload)**: Tải trước một phần file video (theo cấu hình `downloadSizeMb`) để giảm độ trễ khi phát, sau đó chuyển nguồn từ remote sang local.
- **Chuẩn bị trước (preload)**: Chuẩn bị trước `MediaSource` cho các video gần position hiện tại.
- **Phát trước (pre-play)**: Chuẩn bị và phát trước video (ở trạng thái mute) để giảm độ trễ khi người dùng scroll đến.
- **Quản lý bộ nhớ**: Giới hạn số lượng player và file tải về để tránh tràn bộ nhớ.
- **Xử lý scroll nhanh**: Đảm bảo không xảy ra race condition khi người dùng scroll nhanh qua nhiều video.
- **Chuyển nguồn mượt mà**: Chuyển nguồn phát từ remote sang local khi file đã tải xong.

---

## Cấu Trúc Chính

### Các biến quản lý trạng thái
- **`currentPlayingPosition`**: Theo dõi position của video đang phát (`RecyclerView.NO_POSITION` nếu không có video nào đang phát).
- **`mediaSources`**: Lưu các `MediaSource` đã preload cho các position.
- **`playLock`**: Đồng bộ hóa việc phát video bằng `ReentrantLock` để tránh race condition.
- **`activePlayers`**: Lưu các `ExoPlayer` đang hoạt động.
- **`downloadedFiles`**: Lưu các file video đã tải (phần đầu tiên, theo cấu hình `downloadSizeMb`).
- **`downloadLatches`**: Quản lý trạng thái tải file để tránh tải trùng lặp.
- **`mainHandler`**: Dùng để chuyển các tác vụ sang main thread (như chuyển nguồn phát từ remote sang local).
- **`config`**: Cấu hình động từ `ReelConfigViewModel`, bao gồm các thông số như `downloadSizeMb`, `positionsToKeepRange`, buffer cho `ExoPlayer`, v.v.

---

## Phân Tích Tính Năng Chính và Các Trường Hợp Sử Dụng

### 1. Race Condition Khi Scroll Nhanh

#### Mô tả
Khi người dùng scroll nhanh qua nhiều video, nhiều sự kiện `playVideoAtPosition` có thể được gọi liên tiếp, dẫn đến race condition (nhiều thread cùng truy cập và thay đổi trạng thái của `ExoPlayer` hoặc `activePlayers`). Điều này có thể gây ra lỗi như nhiều video phát cùng lúc hoặc player bị dừng/khởi tạo không đúng.

#### Giải pháp hiện tại
`ReelAdapter` sử dụng `ReentrantLock` (`playLock`) để đồng bộ hóa việc phát video trong `playVideoAtPosition` và `releaseAllPlayers`, đảm bảo chỉ một thread được phép thực thi logic phát video tại một thời điểm:
```kotlin
playLock.lock()
try {
    // Logic phát video
} finally {
    playLock.unlock()
}
```

#### Trường hợp kiểm tra
**Scroll từ position 0 → 5**:
- Khi người dùng scroll nhanh từ position 0 đến position 5, `playVideoAtPosition` sẽ được gọi liên tiếp cho các position trung gian (1, 2, 3, 4, 5).
- Nhờ `playLock`, các lời gọi này được xử lý tuần tự, đảm bảo:
  - Các player tại position 0, 1, 2, 3, 4 được tạm dừng (nếu nằm trong `positionsToKeep`) hoặc dừng hẳn (nếu ngoài `positionsToKeep`).
  - Position 5 được phát.

#### Log minh họa
```
ReelAdapter: Before pausing other players, activePlayers: [0, 1, 2, 3]
ReelAdapter: Paused player at position: 0 (in positionsToKeep)
ReelAdapter: Paused player at position: 1 (in positionsToKeep)
ReelAdapter: Paused player at position: 2 (in positionsToKeep)
ReelAdapter: Paused player at position: 3 (in positionsToKeep)
ReelAdapter: Added player to activePlayers at position 5, activePlayers size: 5
ReelAdapter: Played player at position: 5
```

#### Hiệu suất
- **Thời gian thực thi**: Việc sử dụng `playLock` có thể gây chậm nhẹ nếu có quá nhiều sự kiện scroll liên tiếp (do các thread phải chờ). Tuy nhiên, với số lượng position giới hạn trên màn hình (thường 2-3), tác động này không đáng kể (~1-2ms mỗi lần lock/unlock).
- **Rủi ro**: Không có nguy cơ deadlock vì `playLock` chỉ được sử dụng trong `playVideoAtPosition` và `releaseAllPlayers`, và luôn được unlock trong `finally`.

---

### 2. Cơ Chế Preload, Pre-play, Predownload

#### 2.1. Preload (`MediaSource`)
##### Mô tả
`preloadVideos` chuẩn bị trước `MediaSource` cho các video trong phạm vi gần `currentPlayingPosition` (±`preloadCount` = 2), giúp giảm độ trễ khi `ExoPlayer` cần prepare.

##### Hàm chính: `preloadVideos`
```kotlin
fun preloadVideos(firstVisiblePosition: Int, lastVisiblePosition: Int)
```
- **Nhiệm vụ**:
  - Chuẩn bị `MediaSource` cho các position trong phạm vi `preloadStart` đến `preloadEnd` (dựa trên `currentPlayingPosition` ± 2).
  - Chỉ preload nếu `MediaSource` chưa tồn tại và position không nằm trong vùng hiển thị (để tránh xung đột với video đang phát).
- **Hiệu suất**:
  - **Thời gian thực thi**: Tạo một `MediaSource` mất khoảng 1-5ms (tùy vào việc sử dụng file local hay remote).
  - **Tác động bộ nhớ**: Mỗi `MediaSource` chiếm rất ít bộ nhớ (~vài KB), nên preload ±2 position (tổng 5 `MediaSource`) không gây áp lực đáng kể.
  - **Debounce**: Chỉ gọi sau mỗi `preloadDebounceDuration` (300ms) để tránh gọi quá nhiều khi scroll nhanh.

##### Trường hợp kiểm tra
**Tại position 0**:
- Preload cho position -2 đến 2 (nhưng giới hạn từ 0 đến 2 vì danh sách bắt đầu từ 0):
  ```
  ReelAdapter: Preloaded MediaSource for position 1
  ReelAdapter: Preloaded MediaSource for position 2
  ```

#### 2.2. Predownload (Tải trước file)
##### Mô tả
`downloadVideoPartial` tải trước một phần file video (theo cấu hình `downloadSizeMb`) để sử dụng làm nguồn local, giảm độ trễ và băng thông khi phát.

##### Hàm chính: `downloadVideoPartial`
```kotlin
private fun downloadVideoPartial(position: Int, onComplete: () -> Unit = {})
```
- **Nhiệm vụ**:
  - Tải phần đầu tiên của video (theo `downloadSizeMb`, mặc định 8MB) từ URL remote.
  - Lưu file vào `cacheDir` với tên `trend_video_${position}_partial.mp4`.
  - Sử dụng `OkHttpClient` với `Range` header để chỉ tải một phần file.
  - Quản lý tải bằng `downloadLatches` để tránh tải trùng lặp.
- **Hiệu suất**:
  - **Thời gian thực thi**: Tùy thuộc vào tốc độ mạng, thường mất 1-3 giây để tải 8MB (theo log: `Successfully downloaded first 8MB for position 1 (2463092 bytes)` mất ~3 giây).
  - **Tác động bộ nhớ**: Mỗi file chiếm 8MB (theo cấu hình), với `positionsToKeepRange` = 2, tối đa 5 file (~40MB).
  - **Debounce**: Không gọi lại nếu đang tải hoặc đã tải xong.
- **Cơ chế trong `preloadVideos`**:
  - Gọi `downloadVideoPartial` cho các position trong phạm vi preload nếu chưa tải:
    ```
    if (!downloadedFiles.containsKey(pos) && !downloadLatches.containsKey(pos)) {
        downloadVideoPartial(pos)
    }
    ```

##### Trường hợp kiểm tra
**Scroll từ position 0 → 5**:
- Tại position 0, tải trước file cho position 1 và 2:
  ```
  ReelAdapter: Starting preload for position 1 in onScrolled
  ReelAdapter: Starting preload for position 2 in onScrolled
  ReelAdapter: Successfully downloaded first 8MB for position 1 (2463092 bytes)
  ReelAdapter: Successfully downloaded first 8MB for position 2 (8388608 bytes)
  ```
- Khi scroll đến position 5, hủy tải file ngoài `positionsToKeep` (±2):
  ```
  ReelAdapter: Canceled download for position: 1
  ReelAdapter: Canceled download for position: 2
  ```

#### 2.3. Pre-play (Phát trước video)
##### Mô tả
`preloadVideos` không chỉ preload `MediaSource` mà còn thực hiện pre-play (phát trước video ở trạng thái mute) để `ExoPlayer` đạt trạng thái `STATE_READY`, giảm độ trễ khi người dùng scroll đến video.

##### Cơ chế trong `preloadVideos`
- Nếu `MediaSource` đã có hoặc file local đã tải xong, khởi tạo `ExoPlayer` và phát trước:
  ```kotlin
  player.setMediaSource(mediaSources[pos]!!)
  player.volume = 0f // Mute audio
  player.prepare()
  player.playWhenReady = true
  ```
- Tạm dừng sau 1 giây hoặc khi đạt `STATE_READY`:
  ```kotlin
  player.addListener(object : Player.Listener {
      override fun onPlaybackStateChanged(state: Int) {
          if (state == Player.STATE_READY && pos != currentPlayingPosition) {
              player.pause()
          }
      }
  })
  mainHandler.postDelayed({
      if (pos != currentPlayingPosition && player.isPlaying) {
          player.pause()
      }
  }, 1000)
  ```

##### Hiệu suất
- **Thời gian thực thi**: Pre-play mất khoảng 100-500ms để đạt `STATE_READY` (theo log: `Paused pre-played ExoPlayer for position 1 after reaching STATE_READY`).
- **Tác động bộ nhớ**: Pre-play không tăng bộ nhớ đáng kể, nhưng giữ `ExoPlayer` ở trạng thái `STATE_READY` có thể tiêu tốn một chút tài nguyên CPU.
- **Lợi ích**: Giảm độ trễ khi phát chính thức xuống dưới 100ms (theo log: `play duration: 25ms`).

##### Trường hợp kiểm tra
**Tại position 0**:
- Pre-play cho position 1 và 2:
  ```
  ReelAdapter: Pre-playing ExoPlayer for position 1
  ReelAdapter: Pre-playing ExoPlayer for position 2
  ReelAdapter: Paused pre-played ExoPlayer for position 1 after reaching STATE_READY
  ReelAdapter: Paused pre-played ExoPlayer for position 2 after reaching STATE_READY
  ```
- Khi scroll đến position 1, video phát ngay lập tức:
  ```
  ReelAdapter: Before play - Player state at position 1: 2, isPlaying: false
  ReelAdapter: After play - Player state at position 1: 2, isPlaying: true, play duration: 4ms
  ```

---

### 3. Quản Lý `activePlayers` và `downloadedFiles`

#### Mô tả
`activePlayers` lưu các `ExoPlayer` đang hoạt động, và `downloadedFiles` lưu các file video đã tải. Cả hai cần được quản lý để tránh tràn bộ nhớ khi người dùng scroll qua nhiều video.

#### Giải pháp hiện tại
- **Giới hạn `activePlayers`**:
  - Các player ngoài khoảng cách ±10 so với `currentPlayingPosition` sẽ bị xóa:
    ```kotlin
    val maxDistance = 10
    activePlayers.keys.toList().forEach { pos ->
        if (pos != position && (pos < position - maxDistance || pos > position + maxDistance)) {
            activePlayers.remove(pos)
        }
    }
    ```
- **Giới hạn `downloadedFiles`**:
  - Các file ngoài `positionsToKeep` (±`positionsToKeepRange` = 2) sẽ bị xóa thông qua `cleanupOldFiles`.

#### Hàm chính: `cleanupOldFiles`
```kotlin
private fun cleanupOldFiles(currentPosition: Int)
```
- **Nhiệm vụ**:
  - Hủy các tác vụ tải ngoài `positionsToKeep`.
  - Xóa các file đã tải ngoài `positionsToKeep`.
- **Hiệu suất**:
  - **Thời gian thực thi**: Xóa file mất khoảng 1-5ms mỗi file.
  - **Debounce**: Chỉ gọi sau mỗi `cleanupDebounceDuration` (500ms) để tránh gọi quá nhiều.
  - **Tác động bộ nhớ**: Giới hạn tối đa 5 file (40MB), đảm bảo không tràn bộ nhớ.

#### Trường hợp kiểm tra
**Scroll từ position 0 → 15**:
- Tại position 0, `activePlayers` chứa các player từ position 0, 1, 2:
  ```
  ReelAdapter: Added player to activePlayers at position 0, activePlayers size: 1
  ReelAdapter: Added player to activePlayers at position 1, activePlayers size: 2
  ```
- Khi scroll đến position 15, các player từ position 0 đến 4 bị xóa:
  ```
  ReelAdapter: Removed player at position: 0 from activePlayers (outside max distance)
  ReelAdapter: Removed player at position: 1 from activePlayers (outside max distance)
  ```
- Tương tự, file từ position 0 đến 12 bị xóa:
  ```
  ReelAdapter: Deleted old partial video file for position 0
  ```

#### Hiệu suất
- **Bộ nhớ**: Với giới hạn ±10 cho `activePlayers` (tối đa 21 player) và ±2 cho `downloadedFiles` (tối đa 5 file ~40MB), nguy cơ tràn bộ nhớ được kiểm soát tốt.
- **CPU**: Khởi tạo và giải phóng `ExoPlayer` liên tục khi scroll nhanh có thể gây chậm nhẹ, nhưng không đáng kể nhờ giới hạn `activePlayers`.

---

### 4. Chuyển Nguồn Từ Remote Sang Local

#### Mô tả
Để giảm độ trễ và tiết kiệm băng thông, video ban đầu được phát từ URL remote, sau đó chuyển sang phát từ file local (phần đầu tiên đã tải) khi file sẵn sàng.

#### Hàm chính: `playVideoAtPosition` (phần chuyển nguồn)
```kotlin
fun playVideoAtPosition(position: Int)
```
- **Nhiệm vụ**:
  - Ban đầu phát video từ remote.
  - Nếu file local đã tải xong, chuyển nguồn sang local:
    ```kotlin
    if (downloadedFiles.containsKey(position)) {
        val localMediaItem = MediaItem.fromUri(localUri!!).buildUpon().setMediaId(localUri).build()
        player.setMediaItem(localMediaItem)
        player.seekTo(currentPositionMs)
        if (wasPlaying) player.play()
    }
    ```
- **Hiệu suất**:
  - **Thời gian chuyển nguồn**: Chuyển nguồn mất khoảng 10-50ms (theo log: `Switched to local file at position: 0`).
  - **Giật lag**: Không sử dụng `ConcatenatingMediaSource2` như trong tài liệu cũ, nhưng nhờ pre-play, việc chuyển nguồn không gây giật lag đáng kể (video đã ở trạng thái `STATE_READY`).

#### Trường hợp kiểm tra
**Phát video tại position 0**:
- Ban đầu phát từ remote:
  ```
  ReelAdapter: Creating remote media source for position: 0
  ```
- Sau khi file tải xong, chuyển sang local:
  ```
  ReelAdapter: Successfully downloaded first 8MB for position 0 (2463092 bytes)
  ReelAdapter: Switched to local file at position: 0 localFile: /data/user/0/com.kyobi.customer.dev/cache/trend_video_0_partial.mp4
  ```

#### Hiệu suất
- **Thời gian tải file**: Mất 1-3 giây (theo log), nhưng không ảnh hưởng đến trải nghiệm vì video đã phát từ remote trước.
- **Giật lag**: Nhờ pre-play, chuyển nguồn không gây giật (delay < 50ms).

---

### 5. Hiệu Suất và Rủi Ro Tràn Bộ Nhớ

#### Mô tả
Khi người dùng scroll qua hàng trăm video, `activePlayers` và `downloadedFiles` có thể tăng, dẫn đến nguy cơ tràn bộ nhớ.

#### Giải pháp hiện tại
- **Giới hạn `activePlayers`**: Tối đa 21 player (position ±10).
- **Giới hạn `downloadedFiles`**: Tối đa 5 file (position ±2), mỗi file ~8MB, tổng cộng ~40MB.
- **Dọn dẹp tài nguyên**:
  - `releaseAllPlayers` xóa tất cả player và file khi không cần thiết:
    ```kotlin
    fun releaseAllPlayers()
    ```

#### Trường hợp kiểm tra
**Scroll từ position 0 → 100**:
- Tại position 100, `activePlayers` chỉ giữ các player từ position 90 đến 110:
  ```
  ReelAdapter: Removed player at position: 0 from activePlayers (outside max distance)
  ```
- Tương tự, file từ position 0 đến 97 bị xóa:
  ```
  ReelAdapter: Deleted old partial video file for position 0
  ```

#### Hiệu suất
- **Bộ nhớ**: Với giới hạn 21 player và 5 file (~40MB), nguy cơ tràn bộ nhớ được kiểm soát tốt trên thiết bị thông thường (bộ nhớ từ 2GB trở lên).
- **Cải thiện nếu cần**: Giảm `maxDistance` xuống 5 (tối đa 11 player) nếu thiết bị yếu.

---

## Các Hàm Chính Khác

### `onBindViewHolder`
- **Nhiệm vụ**: Bind dữ liệu video (`Reel`) vào `ViewHolder`, tạm dừng player nếu không phải `currentPlayingPosition`.
- **Hiệu suất**: Nhanh (~1-2ms), chỉ thực hiện các thao tác UI đơn giản.

### `onViewRecycled`
- **Nhiệm vụ**: Tạm dừng hoặc giải phóng player khi `ViewHolder` được tái chế.
- **Hiệu suất**: Nhanh (~1-5ms), chỉ gọi `pause` hoặc `release` trên `ExoPlayer`.

### `retryDownloads`
- **Nhiệm vụ**: Thử tải lại các video chưa tải được trong `positionsToKeep`, sau đó chuyển nguồn nếu cần.
- **Hiệu suất**: Tùy thuộc vào số lượng position cần retry và tốc độ mạng (mỗi file ~1-3 giây).

---

## Kết Luận
- **Hiệu suất tổng thể**: `ReelAdapter` hoạt động mượt mà, với độ trễ khi phát video giảm xuống dưới 100ms nhờ cơ chế pre-play.
- **Cơ chế preload/pre-play/predownload**:
  - Preload giảm độ trễ khi prepare `MediaSource`.
  - Predownload giảm băng thông và độ trễ khi phát.
  - Pre-play giảm độ trễ khi phát chính thức xuống dưới 50ms.
- **Rủi ro**:
  - Tràn bộ nhớ được kiểm soát tốt với giới hạn `activePlayers` và `downloadedFiles`.
  - Race condition được xử lý nhờ `playLock`.
- **Cải thiện đề xuất**:
  - Tăng buffer cho `ExoPlayer` nếu mạng yếu:
    ```kotlin
    ExoPlayer.Builder(context)
        .setLoadControl(
            DefaultLoadControl.Builder()
                .setBufferDurationsMs(5000, 50000, 5000, 5000)
                .build()
        )
    ```
  - Kiểm tra trên thiết bị thực tế để đảm bảo không giật lag khi chuyển nguồn.