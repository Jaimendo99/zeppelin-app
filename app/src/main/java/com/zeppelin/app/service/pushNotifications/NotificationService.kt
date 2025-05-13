package com.zeppelin.app.service.pushNotifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.zeppelin.app.R
import com.zeppelin.app.screens.auth.domain.NetworkResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class NotificationService : FirebaseMessagingService() {

    private val TAG = "NotificationService"

    private val parentJob = SupervisorJob()
    private val serviceScope = CoroutineScope(parentJob + Dispatchers.IO)

    private val fcmRepository: IFcmRepository by inject()

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        serviceScope.launch {
            val userToken = UserFcmToken(
                firebaseToken = token,
                deviceInfo = "${android.os.Build.MODEL} ${android.os.Build.VERSION.SDK_INT}",
            )
            when (val result = fcmRepository.saveToken(userToken)) {
                is NetworkResult.Success -> { Log.d(TAG, "Token saved successfully") }
                is NetworkResult.Error -> { Log.e(TAG, "Error saving token: ${result.errorMessage}") }
                NetworkResult.Idle, NetworkResult.Loading -> {Log.d(TAG, "Idle or loading")}
            }
            Log.d(TAG, "Token: $token")
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        remoteMessage.data.isNotEmpty().let {
            showNotification(remoteMessage)
        }

        Log.d(
            "NotificationService", "Message: ${remoteMessage.data}," +
                    " ${remoteMessage.notification?.clickAction}" +
                    " ${remoteMessage.notification?.title}" +
                    " ${remoteMessage.notification?.body}"
        )
    }

    private fun showNotification(message: RemoteMessage) {
        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, "defrente")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(message.notification?.title)
            .setContentText(message.notification?.body)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .build()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(2, notification)


    }

    private fun createNotificationChannel() {
        // Notification channels are required for Android O and above
        val channelId = "defrente"
        val channelName = "Defrente"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, channelName, importance).apply {
            description = "Defrente notifications"
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }


}
