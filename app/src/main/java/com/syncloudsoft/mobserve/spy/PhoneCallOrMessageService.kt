package com.syncloudsoft.mobserve.spy

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.syncloudsoft.mobserve.R
import com.syncloudsoft.mobserve.MainActivity
import com.syncloudsoft.mobserve.MainApplication
import com.syncloudsoft.mobserve.data.ForwardingRuleDatabase
import timber.log.Timber

class PhoneCallOrMessageService : Service() {

    private var observer1: PhoneCallContentObserver? = null
    private var observer2: TextMessageContentObserver? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        releaseObservers()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.v("Starting the service with start ID %d", startId)
        startForeground(NOTIFICATION_ID, createNotification())
        setUpObservers()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun releaseObservers() {
        if (observer1 != null) contentResolver.unregisterContentObserver(observer1!!)
        if (observer2 != null) contentResolver.unregisterContentObserver(observer2!!)
    }

    private fun setUpObservers() {
        releaseObservers()
        val forwardingRules = MainApplication.CONTAINER[ForwardingRuleDatabase::class.java]
            .forwardingRuleDao()
            .getAll()
        observer1 = PhoneCallContentObserver(
            this,
            forwardingRules.toTypedArray(),
            Handler(Looper.getMainLooper()!!))
        observer2 = TextMessageContentObserver(
            this,
            forwardingRules.toTypedArray(),
            Handler(Looper.getMainLooper()!!))
        contentResolver.registerContentObserver(
            PhoneCallContentObserver.URI, true, observer1!!)
        contentResolver.registerContentObserver(
            TextMessageContentObserver.URI, true, observer2!!)
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannelGroup(
                NotificationChannelGroup(NOTIFICATION_CHANNEL_GROUP_ID, getString(R.string.notification_channel_label))
            )
            val notificationChannel =
                NotificationChannel(NOTIFICATION_CHANNEL_ID, getString(R.string.notification_channel_label),
                    NotificationManager.IMPORTANCE_MIN)
            notificationChannel.enableLights(false)
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_SECRET
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE)
                } else {
                    PendingIntent.getActivity(this, 0, notificationIntent, 0)
                }
            }

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getText(R.string.notification_service_title))
            .setContentText(getText(R.string.notification_service_content))
            .setSmallIcon(R.drawable.ic_baseline_compare_arrows_24)
            .setContentIntent(pendingIntent)
            .setTicker(getText(R.string.notification_service_content))
            .build()
    }

    companion object {

        private const val NOTIFICATION_CHANNEL_GROUP_ID = "default_group"
        private const val NOTIFICATION_CHANNEL_ID = "default_channel"
        private const val NOTIFICATION_ID = 60600

        fun startSelf(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(Intent(context, PhoneCallOrMessageService::class.java))
            } else {
                context.startService(Intent(context, PhoneCallOrMessageService::class.java))
            }
        }
    }
}
