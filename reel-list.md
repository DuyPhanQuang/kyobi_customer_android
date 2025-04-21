Tài liệu kỹ thuật: Triển khai tính năng Reel
Tài liệu này cung cấp cái nhìn chi tiết về việc triển khai tính năng Reel trong ứng dụng Kyobi Trend, tập trung vào 3 file chính: ReelAdapter.kt, ReelList.kt và MediaCache.kt. Tài liệu sẽ mô tả các chức năng đã triển khai, lý do tại sao cần áp dụng chúng, và các vấn đề liên quan đến video mà các chức năng này giải quyết. Tài liệu này được viết để làm tài liệu tham khảo cho việc bảo trì và hiểu code sau này.

Tổng quan về tính năng Reel
Tính năng Reel cho phép người dùng xem một danh sách video ngắn, cuộn dọc, tương tự như trải nghiệm trên TikTok. Mỗi video (Reel) được hiển thị toàn màn hình, tự động phát khi hiển thị, và hỗ trợ cuộn mượt mà với hiệu suất tối ưu. Việc triển khai sử dụng Jetpack Compose cho giao diện (ReelList.kt), một adapter tùy chỉnh cho RecyclerView (ReelAdapter.kt) để quản lý phát video, và cơ chế lưu trữ đệm (MediaCache.kt) để xử lý media hiệu quả.

File 1: ReelAdapter.kt
Các chức năng đã triển khai
Phát video với ExoPlayer:

Mỗi Reel được phát bằng một instance của ExoPlayer, được quản lý trong ReelViewHolder.
Phát video được điều khiển qua hàm playVideoAtPosition, đảm bảo chỉ một video phát tại một thời điểm bằng cách tạm dừng các player khác.
PlayerView được cấu hình với RESIZE_MODE_FILL để đảm bảo video lấp đầy toàn màn hình.
Tải một phần video (5MB đầu tiên):

Tải 5MB đầu tiên của mỗi video và lưu vào bộ nhớ đệm để giảm thời gian buffering khi phát từ URL từ xa.
Sử dụng một thread nền để tải file video một phần, lưu trong downloadedFiles.
Chuyển đổi giữa nguồn từ xa và nguồn cục bộ:

Ban đầu phát video từ URL từ xa.
Khi 5MB đầu tiên được tải xong, chuyển sang file cục bộ bằng CacheDataSource để giảm độ trễ.
Thử lại việc chuyển nguồn nếu player chưa sẵn sàng.
Tải trước video tiếp theo:

Tải trước 2 video tiếp theo (currentPosition + 1 và currentPosition + 2) để đảm bảo phát mượt mà khi cuộn.
Quản lý tải trước với một phạm vi các vị trí cần giữ (positionsToKeep), hủy tải các vị trí không cần thiết ngoài phạm vi này.
Quản lý tài nguyên và dọn dẹp:

Sử dụng activePlayers để theo dõi các instance ExoPlayer đang phát và tạm dừng chúng khi chuyển vị trí.
Dọn dẹp các file đã tải cũ và các nguồn media ngoài phạm vi positionsToKeep để tiết kiệm bộ nhớ.
Giải phóng tất cả player và xóa tài nguyên trong releaseAllPlayers khi RecyclerView bị hủy.
Xử lý lỗi và cơ chế thử lại:

Triển khai cơ chế thử lại (tối đa 3 lần) cho các lỗi phát video bằng Player.Listener.onPlayerError.
Hiển thị thông báo Toast cho người dùng nếu phát video thất bại sau các lần thử lại.
Đảm bảo an toàn luồng với khóa:

Sử dụng ReentrantLock (playLock) để ngăn chặn race condition trong playVideoAtPosition khi cuộn nhanh.
Đảm bảo an toàn luồng trong quá trình phát video và chuyển nguồn.
Lý do áp dụng các chức năng này
Phát video với ExoPlayer:
Mục đích: Cung cấp trải nghiệm phát video mạnh mẽ và tùy chỉnh được.
Vấn đề giải quyết: Đảm bảo video phát mượt mà với thời gian buffering tối thiểu, hỗ trợ các tính năng như lặp lại (REPEAT_MODE_ONE) và điều chỉnh âm lượng.
Tải một phần video:
Mục đích: Giảm thời gian buffering ban đầu bằng cách lưu trữ 5MB đầu tiên của video cục bộ.
Vấn đề giải quyết: Xử lý vấn đề mạng chậm, đảm bảo video khởi động nhanh hơn và trải nghiệm người dùng mượt mà hơn.
Chuyển đổi giữa nguồn từ xa và nguồn cục bộ:
Mục đích: Tối ưu phát video bằng cách dùng file cục bộ khi có, nhưng vẫn bắt đầu với URL từ xa để phát ngay lập tức.
Vấn đề giải quyết: Giảm sự phụ thuộc vào mạng, cải thiện hiệu suất phát sau khi tải ban đầu.
Tải trước video tiếp theo:
Mục đích: Tải trước video sắp tới để cuộn mượt mà.
Vấn đề giải quyết: Ngăn chặn độ trễ buffering khi người dùng cuộn đến video tiếp theo, nâng cao tính liền mạch của trải nghiệm Reel.
Quản lý tài nguyên và dọn dẹp:
Mục đích: Quản lý bộ nhớ và lưu trữ hiệu quả.
Vấn đề giải quyết: Ngăn chặn rò rỉ bộ nhớ và sử dụng lưu trữ quá mức bằng cách giải phóng các instance ExoPlayer không dùng và xóa file video cũ.
Xử lý lỗi và cơ chế thử lại:
Mục đích: Xử lý các lỗi phát video một cách nhẹ nhàng.
Vấn đề giải quyết: Đảm bảo ứng dụng ổn định ngay cả khi video không phát được, với cơ chế thử lại để khắc phục lỗi mạng tạm thời.
Đảm bảo an toàn luồng với khóa:
Mục đích: Ngăn chặn race condition khi cuộn nhanh.
Vấn đề giải quyết: Tránh crash hoặc trạng thái phát không nhất quán khi nhiều lệnh playVideoAtPosition được gọi đồng thời.
Các vấn đề liên quan đến video đã giải quyết
Độ trễ buffering: Tải một phần video và tải trước giảm thời gian buffering, đảm bảo video bắt đầu nhanh.
Gián đoạn phát: Đảm bảo an toàn luồng và tạm dừng các player khác ngăn nhiều video phát cùng lúc.
Sử dụng tài nguyên quá mức: Cơ chế dọn dẹp đảm bảo ứng dụng không tiêu tốn quá nhiều bộ nhớ hoặc lưu trữ.
Khôi phục lỗi: Cơ chế thử lại đảm bảo ứng dụng không crash khi gặp lỗi phát, duy trì trải nghiệm mượt mà.
File 2: ReelList.kt
Các chức năng đã triển khai
Cuộn dọc với RecyclerView:

Sử dụng RecyclerView với LinearLayoutManager (hướng dọc) để hiển thị danh sách các Reel.
Triển khai PagerSnapHelper để snap mỗi Reel vào màn hình, đảm bảo trải nghiệm toàn màn hình.
Tự động phát khi cuộn:

Thêm một scroll listener để phát hiện khi cuộn dừng (SCROLL_STATE_IDLE).
Phát video tại vị trí hiển thị hoàn toàn (findFirstCompletelyVisibleItemPosition) hoặc dùng vị trí hiển thị đầu tiên nếu không có item nào hiển thị hoàn toàn.
Tối ưu hiệu suất:

Đặt setHasFixedSize(true) để cải thiện hiệu suất RecyclerView bằng cách giả định kích thước item cố định.
Giới hạn bộ nhớ đệm view (setItemViewCacheSize(3)) và pool view tái chế (setMaxRecycledViews(0, 5)) để giảm sử dụng bộ nhớ.
Xử lý Window Insets:

Cố gắng xử lý WindowInsets bằng ViewCompat.setOnApplyWindowInsetsListener để áp dụng padding cho status bar và navigation bar.
Đặt fitsSystemWindows = true để đảm bảo RecyclerView tuân theo insets của hệ thống.
Quản lý vòng đời:

Giải phóng tất cả instance ExoPlayer và xóa adapter trong callback onRelease của AndroidView.
Lưu tham chiếu đến RecyclerView bằng recyclerViewRef để điều khiển từ bên ngoài (ví dụ: trong TrendTab).
Lý do áp dụng các chức năng này
Cuộn dọc với RecyclerView:
Mục đích: Tạo trải nghiệm cuộn dọc giống TikTok.
Vấn đề giải quyết: Cho phép người dùng cuộn qua các Reel mượt mà, với PagerSnapHelper đảm bảo mỗi video snap vào màn hình.
Tự động phát khi cuộn:
Mục đích: Tự động phát video hiện đang hiển thị.
Vấn đề giải quyết: Nâng cao trải nghiệm người dùng bằng cách tự động phát video mà không cần thao tác thủ công, giống các ứng dụng video ngắn phổ biến.
Tối ưu hiệu suất:
Mục đích: Cải thiện hiệu suất cuộn và giảm sử dụng bộ nhớ.
Vấn đề giải quyết: Ngăn chặn lag khi cuộn và đảm bảo ứng dụng phản hồi nhanh, đặc biệt trên thiết bị yếu.
Xử lý Window Insets:
Mục đích: Điều chỉnh padding của RecyclerView để tính đến status bar và navigation bar.
Vấn đề giải quyết: Nhằm ngăn video bị che bởi các thanh hệ thống, nhưng triển khai này đang gặp vấn đề (xem bên dưới).
Quản lý vòng đời:
Mục đích: Quản lý tài nguyên khi RecyclerView bị hủy.
Vấn đề giải quyết: Ngăn chặn rò rỉ bộ nhớ bằng cách giải phóng các instance ExoPlayer và xóa tham chiếu khi view không còn cần thiết.
Các vấn đề liên quan đến video đã giải quyết
Cuộn liền mạch: PagerSnapHelper đảm bảo video snap vào màn hình, mang lại trải nghiệm người dùng mượt mà.
Tự động phát: Tự động phát video hiển thị giảm thao tác người dùng và tăng sự tương tác.
Hiệu suất: Tối ưu hóa đảm bảo cuộn và phát video mượt mà, ngay cả với nhiều video.
Vấn đề Window Insets (Chưa giải quyết): Triển khai hiện tại cố gắng xử lý WindowInsets để tránh video bị che bởi thanh hệ thống. Tuy nhiên, log Timber.tag("ReelList").d("Top inset: $topInset, Bottom inset: $bottomInset") không được in ra, cho thấy WindowInsets không được truyền đến RecyclerView. Điều này gây ra khoảng trắng ở phía trên video đầu tiên vì padding cho status bar không được áp dụng. Vấn đề vẫn tồn tại vì Scaffold cha có thể đã tiêu thụ WindowInsets, ngăn không cho RecyclerView nhận được.
File 3: MediaCache.kt
Các chức năng đã triển khai
SimpleCache cho Media:

Triển khai SimpleCache với LeastRecentlyUsedCacheEvictor giới hạn 50MB.
Sử dụng StandaloneDatabaseProvider để lưu trữ đệm lâu dài.
Tiêm phụ thuộc (Dependency Injection):

Sử dụng Dagger Hilt với @Singleton để đảm bảo chỉ có một instance MediaCache trong toàn ứng dụng.
Tiêm Context ứng dụng bằng @ApplicationContext.
Giải phóng bộ nhớ đệm:

Cung cấp phương thức release để giải phóng tài nguyên bộ nhớ đệm khi không cần thiết.
Lý do áp dụng các chức năng này
SimpleCache cho Media:
Mục đích: Lưu trữ đệm các file media (như video tải một phần) để phát nhanh hơn.
Vấn đề giải quyết: Giảm sự phụ thuộc vào mạng bằng cách lưu trữ video, cải thiện hiệu suất phát.
Tiêm phụ thuộc:
Mục đích: Cung cấp một instance bộ nhớ đệm duy nhất cho toàn ứng dụng.
Vấn đề giải quyết: Đảm bảo hành vi lưu trữ đệm nhất quán và tránh tạo nhiều instance dư thừa, tiết kiệm bộ nhớ.
Giải phóng bộ nhớ đệm:
Mục đích: Dọn dẹp tài nguyên bộ nhớ đệm khi ứng dụng không cần nữa.
Vấn đề giải quyết: Ngăn chặn rò rỉ bộ nhớ và đảm bảo sử dụng tài nguyên hiệu quả.
Các vấn đề liên quan đến video đã giải quyết
Độ trễ buffering: Lưu trữ đệm file video một phần giảm thời gian buffering, đặc biệt khi chuyển sang nguồn cục bộ.
Quản lý tài nguyên: Giới hạn bộ nhớ đệm 50MB và chính sách LRU ngăn sử dụng lưu trữ quá mức.
Tính nhất quán: Tiêm singleton đảm bảo tất cả các thành phần dùng cùng bộ nhớ đệm, tránh xung đột.
Tóm tắt các vấn đề liên quan đến video và giải pháp
Các vấn đề đã giải quyết
Độ trễ buffering:
Giải pháp: Tải một phần video (ReelAdapter.kt) và lưu trữ đệm (MediaCache.kt) giảm thời gian buffering bằng cách tải trước 5MB của video.
Tác động: Video bắt đầu phát nhanh hơn, cải thiện trải nghiệm người dùng.
Phát liền mạch khi cuộn:
Giải pháp: Tự động phát khi cuộn (ReelList.kt) và tải trước video tiếp theo (ReelAdapter.kt) đảm bảo chuyển đổi mượt mà giữa các video.
Tác động: Người dùng có thể cuộn qua các Reel mà không bị gián đoạn.
Sử dụng tài nguyên quá mức:
Giải pháp: Cơ chế dọn dẹp trong ReelAdapter.kt (như cleanupOldFiles, releaseAllPlayers) và MediaCache.kt (giới hạn bộ nhớ đệm 50MB) quản lý bộ nhớ và lưu trữ.
Tác động: Ngăn chặn crash do rò rỉ bộ nhớ và sử dụng lưu trữ quá mức.
Lỗi phát video:
Giải pháp: Cơ chế thử lại trong ReelAdapter.kt xử lý lỗi phát một cách nhẹ nhàng.
Tác động: Đảm bảo ứng dụng ổn định ngay cả khi gặp vấn đề mạng.
Vấn đề chưa giải quyết
Khoảng trắng ở phía trên video đầu tiên:
Vấn đề: Video đầu tiên hiển thị với khoảng trắng ở phía trên khi mở tab Trend. Cuộn xuống nhẹ làm khoảng trắng biến mất khi video lấp đầy màn hình.
Nguyên nhân: Listener WindowInsets trong ReelList.kt không được kích hoạt, nên RecyclerView không áp dụng padding cho status bar. Nguyên nhân có thể là Scaffold cha đã tiêu thụ WindowInsets, ngăn không cho RecyclerView nhận được.
Giải pháp đã thử: Thêm ViewCompat.setOnApplyWindowInsetsListener để áp dụng padding topInset và bottomInset, nhưng listener không được gọi.
Bước tiếp theo:
Kiểm tra tại sao Scaffold tiêu thụ WindowInsets. Cân nhắc bỏ contentWindowInsets trong Scaffold hoặc dùng cách khác (như Modifier.windowInsetsPadding trong Compose).
Đảm bảo PlayerView trong item_reel.xml lấp đầy màn hình ngay từ đầu, có thể điều chỉnh resize_mode hoặc constraints.