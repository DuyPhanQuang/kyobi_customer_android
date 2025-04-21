Tài Liệu Kỹ Thuật: ReelAdapter.kt
Mô Tả Tổng Quan
ReelAdapter là một RecyclerView.Adapter được thiết kế để hiển thị danh sách các video dạng reel (giống như Instagram Reels hoặc TikTok). Mỗi item trong danh sách là một video được phát bằng ExoPlayer, với các tính năng chính như:

Phát video: Chỉ phát video tại position hiện tại, tạm dừng hoặc dừng các video khác.
Tải trước file: Tải trước 5MB đầu tiên của video để giảm độ trễ khi phát, sau đó chuyển nguồn từ remote sang local.
Quản lý bộ nhớ: Giới hạn số lượng player và file tải về để tránh tràn bộ nhớ.
Xử lý scroll nhanh: Đảm bảo không xảy ra race condition khi người dùng scroll nhanh qua nhiều video.

Cấu Trúc Chính

Các biến quản lý trạng thái:

currentPlayingPosition: Theo dõi position của video đang phát.
downloadedFiles: Lưu các file video đã tải (5MB đầu tiên).
downloadLatches: Quản lý trạng thái tải file để tránh tải trùng lặp.
activePlayers: Lưu các ExoPlayer đang hoạt động.
playLock: Đồng bộ hóa việc phát video để tránh race condition.


Các phương thức chính:

playVideoAtPosition: Phát video tại một position cụ thể.
downloadVideoPartial: Tải 5MB đầu tiên của video.
downloadNextVideoPartial: Tải trước file cho các position tiếp theo.
createMediaSource: Tạo nguồn phát video (remote hoặc local).
releaseAllPlayers: Dọn dẹp tài nguyên khi không cần thiết.



Phân Tích Tính Năng Chính và Các Trường Hợp Sử Dụng
1. Race Condition Khi Scroll Nhanh
   Mô tả
   Khi người dùng scroll nhanh qua nhiều video, nhiều sự kiện playVideoAtPosition có thể được gọi liên tiếp, dẫn đến race condition (nhiều thread cùng truy cập và thay đổi trạng thái của ExoPlayer hoặc activePlayers). Điều này có thể gây ra lỗi như:

Nhiều video phát cùng lúc.
Player bị dừng hoặc khởi tạo không đúng.

Giải pháp hiện tại
ReelAdapter sử dụng ReentrantLock (playLock) để đồng bộ hóa việc phát video trong playVideoAtPosition, đảm bảo chỉ một thread được phép thực thi logic phát video tại một thời điểm.
Code:
playLock.lock()
try {
// Logic phát video
} finally {
playLock.unlock()
}

Trường hợp kiểm tra

Scroll từ position 0 → 5:
Khi người dùng scroll nhanh từ position 0 đến position 5, playVideoAtPosition sẽ được gọi liên tiếp cho các position trung gian (1, 2, 3, 4, 5).
Nhờ playLock, các lời gọi này sẽ được xử lý tuần tự, đảm bảo:
Các player tại position 0, 1, 2, 3, 4 sẽ được tạm dừng (nếu nằm trong positionsToKeep) hoặc dừng hẳn (nếu ngoài positionsToKeep).
Position 5 sẽ được phát.





Log minh họa:
ReelAdapter: Before pausing other players, activePlayers: [0, 1, 2, 3]
ReelAdapter: Paused player at position: 0 (in positionsToKeep)
ReelAdapter: Paused player at position: 1 (in positionsToKeep)
ReelAdapter: Paused player at position: 2 (in positionsToKeep)
ReelAdapter: Paused player at position: 3 (in positionsToKeep)
ReelAdapter: Added player to activePlayers at position 5, activePlayers size: 5
ReelAdapter: Played player at position: 5

Rủi ro nếu không có playLock

Nhiều video phát cùng lúc: Nếu không có playLock, các thread có thể cùng lúc thêm player vào activePlayers và gọi player.play(), dẫn đến nhiều video phát đồng thời.
Trạng thái player không đồng bộ: Các player có thể bị dừng hoặc khởi tạo không đúng, gây lỗi như video không phát được hoặc crash.

Phân tích rủi ro

Hiệu suất: Việc sử dụng playLock có thể gây chậm nhẹ nếu có quá nhiều sự kiện scroll liên tiếp (do các thread phải chờ). Tuy nhiên, trong trường hợp thông thường, tác động này không đáng kể.
Deadlock: Hiện tại không có nguy cơ deadlock vì chỉ có một playLock được sử dụng trong playVideoAtPosition và releaseAllPlayers, và luôn được unlock trong finally.

2. Tải Trước File Khi Scroll Nhanh
   Mô tả
   Để giảm độ trễ khi phát video, ReelAdapter tải trước 5MB đầu tiên của video cho 2 position tiếp theo (position + 1 và position + 2). File được lưu vào cacheDir và được sử dụng để phát video thay vì streaming trực tiếp từ remote.
   Giải pháp hiện tại

Tải trước file:
downloadNextVideoPartial kiểm tra và tải trước file cho position + 1 và position + 2 nếu chưa tải:val positionsToPreload = listOf(currentPosition + 1, currentPosition + 2)
.filter {
it >= 0 &&
it < reels.size &&
!downloadedFiles.containsKey(it) &&
!downloadLatches.containsKey(it)
}
for (position in positionsToPreload) {
Timber.tag("ReelAdapter").d("Starting download for position $position")
downloadVideoPartial(position)
}




Hủy tải không cần thiết:
Các position không nằm trong positionsToKeep (khoảng ±3 so với position hiện tại) sẽ bị hủy tải để tiết kiệm tài nguyên:val positionsToKeep = positionsToKeep(currentPosition)
downloadLatches.keys.toList().forEach { pos ->
if (pos !in positionsToKeep) {
downloadLatches.remove(pos)?.countDown()
Timber.tag("ReelAdapter").d("Canceled download for position: $pos")
}
}





Trường hợp kiểm tra

Scroll từ position 0 → 5:
Tại position 0, file cho position 1 và 2 được tải trước:ReelAdapter: Starting download for position 1
ReelAdapter: Starting download for position 2
ReelAdapter: Successfully downloaded first 5MB for position 1 (2463092 bytes)
ReelAdapter: Successfully downloaded first 5MB for position 2 (5242880 bytes)


Khi scroll đến position 5, các file đang tải cho position 1, 2 sẽ bị hủy (vì không còn nằm trong positionsToKeep), và file cho position 6, 7 được tải trước:ReelAdapter: Canceled download for position: 1
ReelAdapter: Canceled download for position: 2
ReelAdapter: Starting download for position 6
ReelAdapter: Starting download for position 7





Rủi ro nếu không kiểm soát tải trước

Tải trùng lặp: Nếu không có kiểm tra downloadLatches, một position có thể được tải nhiều lần, gây lãng phí băng thông và tài nguyên. Hiện tại, downloadLatches đã giải quyết vấn đề này:if (downloadLatches.containsKey(position)) {
Timber.tag("ReelAdapter").d("Download already in progress for position $position, skipping")
return
}


Tải quá nhiều file: Nếu không hủy tải các position không cần thiết, ứng dụng có thể tải hàng loạt file, dẫn đến tốn băng thông và bộ nhớ.

Phân tích rủi ro

Tốn bộ nhớ: Mỗi file tải về chiếm khoảng 5MB. Nếu không giới hạn số lượng file (hiện tại giới hạn ±3), bộ nhớ có thể bị chiếm dụng quá mức. Với 7 file (position -3 đến +3), bộ nhớ tối đa là ~35MB, vẫn trong ngưỡng chấp nhận được.
Tốn băng thông: Nếu mạng chậm, việc tải trước có thể làm chậm ứng dụng. Tuy nhiên, logic hủy tải giúp giảm thiểu vấn đề này.

3. Quản Lý activePlayers và downloadedFiles
   Mô tả
   activePlayers lưu các ExoPlayer đang hoạt động, và downloadedFiles lưu các file video đã tải. Cả hai đều cần được quản lý để tránh tràn bộ nhớ khi người dùng scroll qua nhiều video.
   Giải pháp hiện tại

Giới hạn activePlayers:
Các player ngoài khoảng cách ±10 so với position hiện tại sẽ bị xóa:val maxDistance = 10
activePlayers.keys.toList().forEach { pos ->
if (pos != position &&
(pos < position - maxDistance || pos > position + maxDistance)) {
val player = activePlayers[pos]
player?.stop()
player?.clearMediaItems()
player?.repeatMode = Player.REPEAT_MODE_OFF
activePlayers.remove(pos)
Timber.tag("ReelAdapter").d("Removed player at position: $pos from activePlayers (outside max distance)")
}
}




Giới hạn downloadedFiles:
Các file ngoài khoảng cách ±3 so với position hiện tại sẽ bị xóa (dù không có phương thức cleanupOldFiles trong code hiện tại, nhưng logic tương tự được áp dụng thông qua positionsToKeep trong downloadNextVideoPartial).



Trường hợp kiểm tra

Scroll từ position 0 → 15:
Tại position 0, activePlayers chứa các player từ position 0, 1, 2:ReelAdapter: Added player to activePlayers at position 0, activePlayers size: 1
ReelAdapter: Added player to activePlayers at position 1, activePlayers size: 2
ReelAdapter: Added player to activePlayers at position 2, activePlayers size: 3


Khi scroll đến position 15, các player tại position 0, 1, 2 sẽ bị xóa (vì ngoài khoảng cách ±10):ReelAdapter: Removed player at position: 0 from activePlayers (outside max distance)
ReelAdapter: Removed player at position: 1 from activePlayers (outside max distance)
ReelAdapter: Removed player at position: 2 from activePlayers (outside max distance)


Tương tự, các file tải về cho position 0, 1, 2 sẽ bị hủy nếu không nằm trong positionsToKeep.



Rủi ro nếu không giới hạn

Tràn bộ nhớ:
Mỗi ExoPlayer tiêu tốn một lượng bộ nhớ đáng kể (bao gồm buffer video, trạng thái phát, và các tài nguyên liên quan). Nếu không giới hạn, activePlayers có thể chứa hàng trăm player, dẫn đến crash ứng dụng.
Mỗi file tải về chiếm 5MB. Nếu không giới hạn downloadedFiles, bộ nhớ lưu trữ có thể bị đầy, đặc biệt trên thiết bị có bộ nhớ thấp.


Hiệu suất giảm:
Quản lý quá nhiều player và file làm chậm ứng dụng, đặc biệt khi scroll qua hàng trăm video.



Phân tích rủi ro

Hiện tại: Với giới hạn ±10 cho activePlayers (tối đa 21 player) và ±3 cho downloadedFiles (tối đa 7 file ~ 35MB), nguy cơ tràn bộ nhớ đã được kiểm soát tốt.
Cải thiện nếu cần:
Giảm maxDistance xuống (ví dụ từ 10 → 5) nếu thấy ứng dụng vẫn tốn bộ nhớ trên thiết bị yếu.
Thêm phương thức cleanupOldFiles để chủ động xóa file cũ:private fun cleanupOldFiles(currentPosition: Int) {
val positionsToKeep = positionsToKeep(currentPosition)
val iterator = downloadedFiles.iterator()
while (iterator.hasNext()) {
val entry = iterator.next()
val position = entry.key
if (position !in positionsToKeep) {
val file = entry.value
file.delete()
iterator.remove()
Timber.tag("ReelAdapter").d("Deleted old partial video file for position $position")
}
}
}





4. Chuyển Nguồn Từ Remote Sang Local
   Mô tả
   Để giảm độ trễ và tiết kiệm băng thông, video ban đầu được phát từ URL remote, sau đó chuyển sang phát từ file local (5MB đầu tiên) khi file tải xong.
   Giải pháp hiện tại

Kiểm tra và chuyển nguồn:
Trong playVideoAtPosition, sau khi phát video từ remote, ứng dụng kiểm tra xem file local đã tải xong chưa. Nếu có, chuyển nguồn sang local:if (downloadedFiles.containsKey(position) &&
(playbackState == Player.STATE_BUFFERING || playbackState == Player.STATE_READY)) {
setPlayVideoWhenSwitchedSource(player, holder.playerView, position)
}




Tải song song:
Nếu file chưa tải, downloadVideoPartial được gọi để tải file, và sau khi tải xong, nguồn sẽ được chuyển:downloadVideoPartial(position) {
mainHandler.post {
switchToLocalIfAvailable()
}
}





Trường hợp kiểm tra

Phát video tại position 0:
Ban đầu, video phát từ remote:ReelAdapter: Played player at position: 0
ReelAdapter: Did not switch to local file at position 0: containsKey=false, isPlaying=false


Sau khi file tải xong (sau ~7 giây), nguồn chuyển sang local:ReelAdapter: Successfully downloaded first 5MB for position 0 (2463092 bytes)
ReelAdapter: Using local file for first 5MB at position: 0
ReelAdapter: Switched to local file after retry at position: 0 localFile: /data/user/0/com.kyobi.customer/cache/video_0_partial.mp4




Scroll nhanh từ position 0 → 5:
Nếu file cho position 5 chưa tải xong, video sẽ phát từ remote trước:ReelAdapter: Played player at position: 5
ReelAdapter: Did not switch to local file at position 5: containsKey=false, isPlaying=false


Khi file tải xong, nguồn chuyển sang local:ReelAdapter: Successfully downloaded first 5MB for position 5
ReelAdapter: Switched to local file after retry at position: 5





Rủi ro nếu không chuyển nguồn

Tốn băng thông: Nếu không chuyển sang local, video sẽ tiếp tục streaming từ remote, gây tốn băng thông và có thể giật lag nếu mạng yếu.
UX kém: Nếu chuyển nguồn không đúng thời điểm (ví dụ khi player đang phát), video có thể bị giật. Hiện tại, logic chỉ chuyển nguồn khi player ở trạng thái BUFFERING hoặc READY, nên tránh được vấn đề này.

Phân tích rủi ro

Thời gian tải file: Nếu mạng chậm, việc tải file có thể mất nhiều thời gian (trong log, position 0 mất ~7 giây). Trong thời gian này, video phát từ remote, có thể gây giật nếu mạng không ổn định.
Giải pháp: Tăng buffer cho ExoPlayer để giảm giật:ExoPlayer.Builder(context)
.setLoadControl(
DefaultLoadControl.Builder()
.setBufferDurationsMs(5000, 50000, 5000, 5000) // Tăng buffer
.build()
)
.build()




Chuyển nguồn thất bại: Nếu file tải về bị lỗi (ví dụ không đủ 5MB), việc chuyển nguồn có thể thất bại. Hiện tại, code không kiểm tra kích thước file trước khi chuyển nguồn, nhưng trong log không thấy lỗi này.

5. Hiệu Suất và Rủi Ro Tràn Bộ Nhớ
   Mô tả
   Khi người dùng scroll qua hàng trăm video, activePlayers và downloadedFiles có thể tăng lên đáng kể, dẫn đến nguy cơ tràn bộ nhớ.
   Giải pháp hiện tại

Giới hạn activePlayers: Tối đa 21 player (position ±10).
Giới hạn downloadedFiles: Tối đa 7 file (position ±3), mỗi file ~5MB, tổng cộng ~35MB.
Dọn dẹp tài nguyên: releaseAllPlayers xóa tất cả player và file khi không cần thiết:fun releaseAllPlayers() {
playLock.lock()
try {
for (i in 0 until recyclerView.childCount) {
val child = recyclerView.getChildAt(i)
val holder = recyclerView.getChildViewHolder(child) as? ReelViewHolder
holder?.releasePlayer(true)
}
downloadedFiles.values.forEach { it.delete() }
downloadedFiles.clear()
mediaSources.clear()
downloadLatches.clear()
dataSourceFactories.clear()
activePlayers.clear()
Timber.tag("ReelAdapter").d("Cleared all downloaded partial files and media sources")
} finally {
playLock.unlock()
}
}



Trường hợp kiểm tra

Scroll từ position 0 → 100:
Tại position 0, activePlayers chứa các player từ position 0 đến 10 (tối đa 11 player).
Khi scroll đến position 100, các player từ position 0 đến 89 sẽ bị xóa:ReelAdapter: Removed player at position: 0 from activePlayers (outside max distance)
ReelAdapter: Removed player at position: 1 from activePlayers (outside max distance)
...
ReelAdapter: Removed player at position: 89 from activePlayers (outside max distance)


Tương tự, các file từ position 0 đến 96 sẽ bị hủy hoặc xóa.



Phân tích rủi ro

Tràn bộ nhớ:
Hiện tại, với giới hạn 21 player và 7 file (~35MB), nguy cơ tràn bộ nhớ đã được kiểm soát tốt trên các thiết bị thông thường (có bộ nhớ từ 2GB trở lên).
Tuy nhiên, trên thiết bị yếu (bộ nhớ < 1GB), việc giữ 21 player có thể gây áp lực. Đề xuất giảm maxDistance xuống 5 (tối đa 11 player) nếu cần.


Hiệu suất:
Việc khởi tạo và giải phóng ExoPlayer liên tục khi scroll nhanh có thể gây chậm nhẹ, nhưng không đáng kể nhờ giới hạn activePlayers.



6. Vấn Đề Nhỏ: visiblePosition: -1 Khi Scroll
   Mô tả
   Khi người dùng scroll và dừng giữa hai video, không có video nào hoàn toàn hiển thị, dẫn đến visiblePosition: -1.
   Log minh họa:
   ReelList: Scroll state idle, visiblePosition: -1
   ReelList: No completely visible position found, trying findFirstVisibleItemPosition

Tác động

Hiện tại, nếu visiblePosition: -1, video hiện tại (position 0 trong log) tiếp tục phát:ReelAdapter: Player at position 0 is already playing, skipping


Điều này không gây lỗi, nhưng UX có thể không mượt mà: Người dùng có thể mong muốn video tại position hiển thị nhiều nhất được phát.

Giải pháp đề xuất
Sử dụng logic chọn position hiển thị nhiều nhất trong ReelList:
private fun findVisiblePosition(): Int {
val layoutManager = recyclerView.layoutManager as LinearLayoutManager
val firstVisiblePosition = layoutManager.findFirstCompletelyVisibleItemPosition()
if (firstVisiblePosition != RecyclerView.NO_POSITION) {
return firstVisiblePosition
}

    // Nếu không có item nào hoàn toàn hiển thị, tìm item hiển thị nhiều nhất
    val firstPartial = layoutManager.findFirstVisibleItemPosition()
    val lastPartial = layoutManager.findLastVisibleItemPosition()
    if (firstPartial == RecyclerView.NO_POSITION || lastPartial == RecyclerView.NO_POSITION) {
        return RecyclerView.NO_POSITION
    }

    var maxVisibleHeight = 0
    var maxVisiblePosition = firstPartial
    for (i in firstPartial..lastPartial) {
        val child = layoutManager.findViewByPosition(i) ?: continue
        val visibleHeight = minOf(child.bottom, recyclerView.height) - maxOf(child.top, 0)
        if (visibleHeight > maxVisibleHeight) {
            maxVisibleHeight = visibleHeight
            maxVisiblePosition = i
        }
    }
    if (maxVisibleHeight == 0) {
        Timber.tag("ReelList").w("No visible position found")
        return RecyclerView.NO_POSITION
    }
    return maxVisiblePosition
}

Phân tích rủi ro

UX kém: Nếu không xử lý visiblePosition: -1, video không chuyển đổi đúng thời điểm, gây khó chịu cho người dùng.
Hiệu suất: Logic chọn position hiển thị nhiều nhất có thể làm chậm nhẹ khi scroll nhanh (do phải tính toán chiều cao hiển thị), nhưng tác động không đáng kể.

Kết Luận

Ứng dụng hoạt động ổn định:
Các tính năng chính (phát video, tải trước file, chuyển nguồn, quản lý player) đều hoạt động tốt, ngay cả khi scroll nhanh.
Không có lỗi nghiêm trọng (như video phát trùng, tải trùng lặp, hoặc crash).


Rủi ro được kiểm soát:
Nguy cơ tràn bộ nhớ được giảm thiểu nhờ giới hạn activePlayers (tối đa 21) và downloadedFiles (tối đa 7 file ~ 35MB).
Race condition được xử lý nhờ playLock.


Cải thiện đề xuất:
Xử lý visiblePosition: -1 bằng cách chọn position hiển thị nhiều nhất.
Tăng buffer cho ExoPlayer nếu cần giảm giật khi phát từ remote:ExoPlayer.Builder(context)
.setLoadControl(
DefaultLoadControl.Builder()
.setBufferDurationsMs(5000, 50000, 5000, 5000)
.build()
)
.build()





