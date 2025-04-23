package com.zeppelin.app.service

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
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
                val notification = notificationBuilder(courseId, "Session started...")
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
                val notification = notificationBuilder(courseId, message)
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

    private fun notificationBuilder(courseId: Int?, data: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, "live_session_channel")
            .setOngoing(true)
            .setContentTitle("Live Session for Course ID: ${wsClient.lastCourseId.value ?: "N/A"}")
            .setContentText(data)
            .setSmallIcon(R.drawable.logo)
            .setPriority(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
    }

    enum class SessionState {
        CONNECTED,
        DISCONNECTED
    }
}