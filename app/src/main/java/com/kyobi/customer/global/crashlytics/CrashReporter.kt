package com.kyobi.customer.global.crashlytics

import android.os.Process.killProcess
import android.os.Process.myPid
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kyobi.customer.BuildConfig
import timber.log.Timber
import kotlin.system.exitProcess

/**
 * CrashReporter - Wrapper cho FirebaseCrashlytics
 * Dùng để log crash, sự kiện, người dùng... từ mọi module
 * e-commerce + reel + community
 */
object CrashReporter {
    private const val TAG = "CrashReporter"
    private val crashlytics: FirebaseCrashlytics = FirebaseCrashlytics.getInstance()

    /** global crash handler - runZonedGuarded
     */
    fun initGlobalHandler() {
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            crashlytics.recordException(throwable)
            crashlytics.log("UncaughtException in thread ${thread.name}")
            // Production mới exit, Dev để dễ debug
            if (!BuildConfig.DEBUG) {
                killProcess(myPid())
                exitProcess(1)
            }
        }
    }

    /**
     * Ghi log custom
     */
    fun log(message: String) {
        crashlytics.log(message)
        Timber.tag(TAG).d("CrashReporter $message")
    }

    /**
     * Gán ID người dùng (hữu ích khi debug user gặp lỗi)
     */
    fun setUserId(userId: String) {
        crashlytics.setUserId(userId)
        Timber.tag(TAG).d("Set userId = $userId")
    }

    /**
     * Gán custom key-value (VD: màn hiện tại, hành vi user...)
     */
    private fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
        Timber.tag(TAG).d("CustomKey: $key = $value")
    }

    /**
     * Báo lỗi không fatal (không crash app nhưng cần theo dõi)
     */
    fun logException(e: Throwable) {
        crashlytics.recordException(e)
        Timber.tag(TAG).d("Logged exception $e")
    }

    /**
     * Báo sự kiện custom để tracking logic app (VD: checkout thất bại)
     */
    fun logEvent(eventName: String, data: Map<String, String>? = null) {
        crashlytics.log("Event: $eventName")
        data?.forEach { (k, v) -> setCustomKey(k, v) }
    }

    /**
     * Log sự kiện video (reel)
     */
    fun logVideoEvent(event: String, videoId: String, extra: Map<String, String>? = null) {
        crashlytics.log("[Video][$event] videoId=$videoId")
        crashlytics.setCustomKey("video_id", videoId)
        extra?.forEach { (k, v) -> setCustomKey(k, v) }
    }

    fun logCommunityEvent(event: String, userId: String, postId: String? = null) {
        crashlytics.log("[Community][$event] userId=$userId postId=${postId ?: "N/A"}")
        setCustomKey("community_event", event)
        setCustomKey("community_user_id", userId)
        postId?.let { setCustomKey("community_post_id", it) }
    }
}
