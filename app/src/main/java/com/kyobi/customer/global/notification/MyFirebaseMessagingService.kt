package com.kyobi.customer.global.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.kyobi.customer.MainActivity
import com.kyobi.customer.R
import com.kyobi.customer.global.firebase.MyFirebaseService
import com.kyobi.featurecommon.auth.session.Session
import com.kyobi.featurecommon.auth.session.SessionEventBus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val CHANNEL_ID = "kyobi_notifications"
        private const val CHANNEL_NAME = "Kyobi Notifications"
        private const val NOTIFICATION_ID = 1001
        private const val tag = "FCMCore"
    }

    @Inject
    lateinit var firebaseService: MyFirebaseService

    @Inject
    lateinit var sessionEventBus: SessionEventBus

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var currentSession: Session? = null
    private var isCollecting = false

    override fun onCreate() {
        super.onCreate()
        Timber.tag(tag).d("MyFirebaseMessagingService onCreate called")
        if (coroutineScope.isActive && !isCollecting) {
            Timber.tag(tag).d("CoroutineScope is active, starting to collect sessionEvents")
            coroutineScope.launch {
                Timber.tag(tag).d("Starting to collect sessionEvents")
                isCollecting = true
                sessionEventBus.sessionFlow.collectLatest { session ->
                    Timber.tag(tag).d("***sessionEventBus*** subscribed - Received new session: $session")
                    currentSession = session
                    handleTokenUpdate()
                }
            }
        } else {
            Timber.tag(tag).e("CoroutineScope is not active, cannot collect sessionEvents")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Hủy CoroutineScope khi Service bị hủy
        serviceScope.cancel()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Timber.tag(tag).d("Received message from: ${remoteMessage.from}")

        // Message Notification (FCM tự động hiển thị nếu app ở background)
        remoteMessage.notification?.let { notification ->
            Timber.tag(tag).d("Message Notification Body: ${notification.body}")
            // Hiển thị thông báo nếu app ở foreground
            if (isAppInForeground()) {
                showNotification(
                    title = notification.title ?: "Kyobi Notification",
                    body = notification.body ?: "You have a new notification",
                    data = remoteMessage.data
                )
            }
        }

        // Data Message (luôn cần tự xử lý)
        if (remoteMessage.data.isNotEmpty()) {
            Timber.tag(tag).d("Data Payload: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }
    }

    override fun onNewToken(token: String) {
        // Không cần gọi refreshAndUploadToken ở đây nữa nhưng cứ override func này
        // Logic đã được chuyển vào handleTokenUpdate() trong collectLatest
        Timber.tag(tag).d("onNewToken: $token")
    }

    private fun handleTokenUpdate() {
        val userId = currentSession?.userId
        if (userId != null) {
            Timber.tag(tag).d("Session available, refreshing and uploading token for user: $userId")
            // Chạy bất đồng bộ trong serviceScope
            serviceScope.launch {
                try {
                    // Gọi tuần tự với suspend function
                    firebaseService.removeCurrentToken(userId)
                    firebaseService.refreshAndUploadToken(userId)
                    Timber.tag(tag).d("Token update completed for user: $userId")
                } catch (e: Exception) {
                    Timber.tag(tag).e(e, "Error updating token for user: $userId")
                }
            }
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        when (data["type"]) {
            "chat_message" -> {
                val chatId = data["chat_id"]
                val message = data["message"]
                // Hiển thị custom UI / gửi event qua ViewModel, v.v...
            }
            "promotion" -> {
                // Xử lý khuyến mãi
            }
        }
    }

    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Tạo Notification Channel
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Kyobi app notifications"
            enableLights(true) // Bật đèn LED (nếu thiết bị hỗ trợ)
            lightColor = android.graphics.Color.RED // Màu LED
            enableVibration(true) // Bật rung
            vibrationPattern = longArrayOf(0, 500, 200, 500)
        }
        notificationManager.createNotificationChannel(channel)

        // Tạo PendingIntent để mở MainActivity khi người dùng click vào thông báo
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("notification_type", data["type"])
            putExtra("chat_id", data["chat_id"])
            putExtra("message", data["message"])
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Tạo thông báo
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        // Hiển thị thông báo
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun isAppInForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val runningProcesses = activityManager.runningAppProcesses ?: return false
        val packageName = applicationContext.packageName
        return runningProcesses.any { it.processName == packageName && it.importance == android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND }
    }
}