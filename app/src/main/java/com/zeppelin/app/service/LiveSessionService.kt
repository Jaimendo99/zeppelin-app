package com.zeppelin.app.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.core.app.NotificationCompat
import com.zeppelin.app.MainActivity
import com.zeppelin.app.R
import com.zeppelin.app.screens._common.data.WebSocketClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject // Import the inject delegate


class LiveSessionService : Service() {

    private val wsClient: WebSocketClient by inject()

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val courseId = intent?.getIntExtra("courseId", -1) ?: -1

        when (intent?.action) {
            SessionState.CONNECTED.name -> {
                val notification = notificationBuilder("Session started...")
                startForeground(1, notification.build())
                start(courseId)
            }

            SessionState.DISCONNECTED.name -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start(courseId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            wsClient.connect(courseId)
            wsClient.incomingMessages.collect { message ->
                val notification = notificationBuilder(message)
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(1, notification.build())
            }
        }
    }

    private fun stop() {
        Log.d("LiveSessionService", "Stopping service...")
        CoroutineScope(Dispatchers.IO).launch {
            wsClient.disconnect()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            Log.d("LiveSessionService", "Service stopped.")
        }
    }

    private fun notificationBuilder(data: String): NotificationCompat.Builder {
        val courseId = wsClient.lastCourseId.value

        val contentIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("courseId", courseId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Adjust flags as needed
        }

        val pendingIntentFlags =
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val contentPendingIntent: PendingIntent = PendingIntent.getActivity(
            this,
            0,
            contentIntent,
            pendingIntentFlags
        )
        return NotificationCompat.Builder(this, "live_session_channel")
            .setOngoing(true)
            .setContentTitle("Live Session for Course ID: $courseId")
            .setContentText(data)
            .setSmallIcon(R.drawable.ic_fg_dark)
            .setContentIntent(contentPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setLargeIcon( BitmapFactory.decodeResource( resources, R.drawable.logo ) )
            .setStyle(
                NotificationCompat.BigTextStyle() // Keep BigTextStyle if 'data' can be long
                    .bigText(data)
                    .setBigContentTitle("Live Session for Course ID: $courseId")
                    .setSummaryText("Tap to view details")
            )

    }

    enum class SessionState {
        CONNECTED,
        DISCONNECTED
    }
}