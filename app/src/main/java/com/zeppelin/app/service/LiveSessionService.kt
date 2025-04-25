package com.zeppelin.app.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.zeppelin.app.MainActivity
import com.zeppelin.app.R
import com.zeppelin.app.screens._common.data.ClientHelloMessage
import com.zeppelin.app.screens._common.data.PomodoroExtendMessage
import com.zeppelin.app.screens._common.data.PomodoroPhaseEndMessage
import com.zeppelin.app.screens._common.data.PomodoroSessionEndMessage
import com.zeppelin.app.screens._common.data.PomodoroStartMessage
import com.zeppelin.app.screens._common.data.StatusUpdateMessage
import com.zeppelin.app.screens._common.data.UnknownEvent
import com.zeppelin.app.screens._common.data.WebSocketClient
import com.zeppelin.app.screens._common.data.WebSocketState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class LiveSessionService : Service() {

    private val TAG = "LiveSessionService"
    private val wsClient: WebSocketClient by inject()

    private lateinit var notificationManager: NotificationManager
    private val serviceScope =
        CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var currentCourseId: Int = -1

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "live_session_channel"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        observeWebSocketState()
        observeIncomingMessages()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        when (intent?.action) {
            Action.START.name -> {
                val courseId = intent.getIntExtra("courseId", -1)
                val retry = intent.getBooleanExtra("retry", false)
                startForeground(
                    NOTIFICATION_ID,
                    buildNotification("Starting session...", currentCourseId).build()
                )
                serviceScope.launch { wsClient.connect(courseId, retry) }
            }

            Action.STOP.name -> {
                Log.d(TAG, "Stopping service...")
                serviceScope.launch {
                    wsClient.disconnect()
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun observeWebSocketState() {
        val title = "Live Session for Course ID"
        serviceScope.launch {
            wsClient.state.collect { state ->
                val (notiTitle, text) = when (state) {
                    WebSocketState.Idle -> "Live Session" to "Idle"
                    WebSocketState.Connecting -> "Live Session" to "Connecting..."
                    is WebSocketState.Connected -> {
                        currentCourseId = state.lastCourseId
                        "$title $currentCourseId" to
                                "Connected"
                    }
                    WebSocketState.Disconnected -> "$title $currentCourseId" to "Disconnected"
                    is WebSocketState.Error -> "$title $currentCourseId" to "Error: ${state.message}"
                }

                notificationManager.notify(
                    NOTIFICATION_ID,
                    buildNotification(text, currentCourseId, notiTitle).build()
                )
            }
        }
    }

    private fun observeIncomingMessages() {
        serviceScope.launch {
            wsClient.incomingMessages.collect { msg ->
                if (wsClient.state.value is WebSocketState.Connected) {
                    notificationManager.notify(
                        NOTIFICATION_ID,
                        buildNotification(
                            msg,
                            currentCourseId,
                            "Live Session for Course ID: $currentCourseId"
                        ).build()
                    )
                }
            }
        }
    }

    private fun observeInconmingEvents(){
        serviceScope.launch {
            wsClient.wsEvents.collect { event ->
                if (wsClient.state.value is WebSocketState.Connected) {
                    when (event) {
                        is ClientHelloMessage -> TODO()
                        is PomodoroExtendMessage -> TODO()
                        is PomodoroPhaseEndMessage -> TODO()
                        is PomodoroSessionEndMessage -> TODO()
                        is PomodoroStartMessage -> TODO()
                        is StatusUpdateMessage -> TODO()
                        is UnknownEvent -> TODO()
                    }

                }
            }
        }
    }

    private fun buildNotification(
        contentText: String,
        courseId: Int,
        contentTitle: String = "Live Session for Course ID: $courseId"
    ): NotificationCompat.Builder {
        // clicking takes you back to MainActivity with the courseId in extras
        val contentIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("courseId", courseId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntentFlags =
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            contentIntent,
            pendingIntentFlags
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setOngoing(true)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_fg_dark)
            .setLargeIcon(
                BitmapFactory.decodeResource(resources, R.drawable.logo)
            )
            .setContentIntent(contentPendingIntent)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(contentText)
                    .setBigContentTitle(contentTitle)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
    }

    enum class Action {
        START, STOP
    }
}
