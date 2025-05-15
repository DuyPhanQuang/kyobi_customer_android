produceState<Session?>(initialValue = null, lifecycleOwner) { ... }
Ý nghĩa:

produceState là một API của Jetpack Compose dùng để biến một nguồn dữ liệu bất đồng bộ (như Flow, LiveData, hoặc các nguồn dữ liệu khác) thành một State mà Compose có thể sử dụng để tự động cập nhật UI.
Ở đây, produceState<Session?> tạo ra một State<Session?> (được lưu trong sessionState) để chứa giá trị của session từ sessionEvents.
initialValue = null: Giá trị ban đầu của sessionState là null, nghĩa là khi composable được tạo lần đầu, sessionState sẽ có giá trị là null cho đến khi sessionEvents emit giá trị đầu tiên.
lifecycleOwner: Đây là tham số để produceState biết cách tích hợp với lifecycle của composable (thông qua LocalLifecycleOwner). Điều này đảm bảo rằng việc thu thập dữ liệu từ sessionEvents sẽ được quản lý theo lifecycle của composable.
Về mặt kỹ thuật:

produceState hoạt động giống như một cầu nối giữa thế giới bất đồng bộ (như Flow) và thế giới đồng bộ của Compose.
Khi produceState được gọi, nó sẽ chạy khối lambda { ... } bên trong để thu thập dữ liệu từ nguồn bất đồng bộ (ở đây là sessionEvents).
Mỗi khi value trong khối lambda được cập nhật (bằng value = session), sessionState sẽ được cập nhật và Compose sẽ tự động recompose (tái render UI) nếu sessionState được sử dụng trong UI.


Ví dụ tương tự:
Nếu không dùng produceState, anh sẽ phải tự viết một State và tự thu thập Flow trong một LaunchedEffect, như thế này:

val sessionState = remember { mutableStateOf<Session?>(null) }
LaunchedEffect(Unit) {
sessionEvents.collect { session ->
sessionState.value = session
}
}
produceState giúp làm việc này một cách ngắn gọn và an toàn hơn, đồng thời tích hợp với lifecycle.

.flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.RESUMED)
Ý nghĩa:
flowWithLifecycle là một extension function của Kotlin Flow trong Jetpack Lifecycle, được sử dụng để kiểm soát việc thu thập dữ liệu từ Flow dựa trên lifecycle của một thành phần (ở đây là composable).
lifecycleOwner.lifecycle là lifecycle của composable, được lấy từ LocalLifecycleOwner.current.
Lifecycle.State.RESUMED là trạng thái lifecycle mà ta muốn thu thập dữ liệu:
RESUMED nghĩa là composable đang hiển thị trên màn hình (tương ứng với activity/fragment đang ở foreground).
Nếu composable không ở trạng thái RESUMED (ví dụ: activity bị pause hoặc composable không còn hiển thị), việc thu thập dữ liệu sẽ tự động bị tạm dừng.
Về mặt kỹ thuật:
flowWithLifecycle biến sessionEvents thành một Flow mới, trong đó:
Dữ liệu từ sessionEvents chỉ được thu thập khi lifecycle đạt ít nhất trạng thái RESUMED.
Khi lifecycle rơi xuống dưới RESUMED (ví dụ: activity bị pause → trạng thái STARTED), việc thu thập dữ liệu sẽ bị tạm dừng.
Khi lifecycle trở lại RESUMED (activity được resume), việc thu thập dữ liệu sẽ tự động tiếp tục.
Điều này giúp tránh rò rỉ bộ nhớ (memory leak) và tiết kiệm tài nguyên, vì ta không thu thập dữ liệu khi composable không hiển thị.
Tại sao cần flowWithLifecycle?
Nếu không dùng flowWithLifecycle, việc thu thập sessionEvents sẽ tiếp tục chạy ngay cả khi composable không còn hiển thị, dẫn đến:
Tiêu tốn tài nguyên không cần thiết.
Có thể gây lỗi nếu composable bị hủy nhưng vẫn cố gắng cập nhật trạng thái.
flowWithLifecycle đảm bảo rằng việc thu thập dữ liệu chỉ diễn ra trong phạm vi lifecycle phù hợp.

.collectLatest { session -> ... }
Ý nghĩa:
collectLatest là một operator của Kotlin Flow, dùng để thu thập dữ liệu từ Flow và xử lý giá trị mới nhất được emit.
{ session -> ... } là khối lambda được gọi mỗi khi sessionEvents emit một giá trị mới (session).
collectLatest có đặc điểm:
Nếu có nhiều giá trị được emit liên tiếp trong thời gian ngắn, nó sẽ hủy xử lý giá trị cũ và chỉ xử lý giá trị mới nhất.
Điều này hữu ích trong trường hợp sessionEvents emit nhiều giá trị nhanh chóng, nhưng ta chỉ quan tâm đến giá trị mới nhất.
Về mặt kỹ thuật:
collectLatest là một suspending function, nghĩa là nó phải được gọi trong một coroutine scope.
produceState tự động quản lý coroutine scope cho ta, nên ta không cần tự tạo scope (như LaunchedEffect).
Trong khối lambda:
session là giá trị mới nhất được emit từ sessionEvents.
value = session: Cập nhật giá trị của sessionState (do produceState cung cấp). Khi value thay đổi, Compose sẽ tự động recompose nếu sessionState được sử dụng trong UI.
Phần logic còn lại kiểm tra điều kiện và gọi onAppForeground() nếu cần.
Tại sao dùng collectLatest thay vì collect?
collect sẽ xử lý tuần tự tất cả giá trị được emit, kể cả khi có giá trị mới đến trước khi xử lý giá trị cũ hoàn tất.
collectLatest bỏ qua giá trị cũ và chỉ xử lý giá trị mới nhất, giúp tránh xử lý không cần thiết nếu sessionEvents emit nhiều giá trị liên tiếp.
Ví dụ minh họa:
sessionEvents emit Session(userId="1") → Khối lambda chạy, value = Session(userId="1").
Ngay sau đó, sessionEvents emit null (trước khi logic trong lambda hoàn tất) → collectLatest hủy xử lý Session(userId="1") và chạy lại lambda với value = null.