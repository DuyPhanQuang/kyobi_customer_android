Các trường hợp onNewToken(token: String) được gọi
Hàm onNewToken(token: String) được gọi khi Firebase Cloud Messaging (FCM) tạo ra một FCM token mới (registration token) cho thiết bị. Đây là các trường hợp phổ biến:

Khi ứng dụng được cài đặt lần đầu tiên:
Lần đầu tiên ứng dụng được cài đặt trên thiết bị, FCM sẽ tạo một token mới và gọi onNewToken() ngay sau khi ứng dụng khởi động và FCM được khởi tạo.
Khi ứng dụng được mở lại sau khi bị xóa dữ liệu (clear data):
Nếu người dùng xóa dữ liệu của ứng dụng (Settings > Apps > Clear Data), token cũ sẽ bị xóa. Khi ứng dụng mở lại, FCM sẽ tạo token mới và gọi onNewToken().
Khi token cũ hết hạn hoặc bị làm mới (refresh):
FCM token có thể bị làm mới định kỳ bởi hệ thống (thường sau vài tháng, nhưng không có thời gian cố định). Điều này xảy ra khi:
Google Play Services trên thiết bị cập nhật hoặc thay đổi cách quản lý token.
Token bị thu hồi (revoked) do các vấn đề bảo mật hoặc chính sách của FCM.
Thiết bị thay đổi trạng thái (ví dụ: khôi phục cài đặt gốc, thay đổi tài khoản Google chính trên thiết bị).
Khi ứng dụng được khôi phục từ backup (restore):
Nếu thiết bị được khôi phục từ bản sao lưu (backup), token cũ có thể không còn hợp lệ. FCM sẽ tạo token mới và gọi onNewToken().
Khi gọi thủ công FirebaseMessaging.getInstance().deleteToken():
Nếu gọi FirebaseMessaging.getInstance().deleteToken() (ví dụ: khi user logout), token cũ sẽ bị xóa. Lần tiếp theo FCM tạo token mới (thường ngay sau đó), onNewToken() sẽ được gọi.
Khi ứng dụng được cập nhật (update):
Trong một số trường hợp hiếm, khi ứng dụng được cập nhật (update) qua Play Store, FCM có thể làm mới token để đảm bảo tính bảo mật, và onNewToken() sẽ được gọi.
Trường hợp khi mới mở app thì sao?
Không phải lúc nào mở app cũng gọi onNewToken():
Khi mở app, nếu token hiện tại vẫn hợp lệ (đã được tạo trước đó và chưa bị thu hồi), thì onNewToken() sẽ không được gọi.
Tuy nhiên, nếu đây là lần đầu mở app sau khi cài đặt, hoặc token cũ bị xóa (do clear data, logout, v.v.), thì onNewToken() sẽ được gọi ngay sau khi FCM khởi tạo và tạo token mới.

Hướng dẫn người dùng trên thiết bị "độc lạ":
Anh có thể thêm một màn hình hướng dẫn trong app, yêu cầu người dùng:
Bật quyền thông báo: Vào Settings > Apps > [App Name] > Notifications > Allow.
Cho phép chạy nền: Vào Settings > Apps > [App Name] > Battery > Allow background activity.
Bật âm thanh thông báo: Vào Settings > Notifications > [Channel Name] > Sound > Enable.