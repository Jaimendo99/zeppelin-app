package com.zeppelin.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.zeppelin.app.di.AuthModules
import com.zeppelin.app.di.appModule
import com.zeppelin.app.di.courseDetailModules
import com.zeppelin.app.di.courseModules
import com.zeppelin.app.di.courseSessionModules
import com.zeppelin.app.di.liveSessionModule
import com.zeppelin.app.di.pushNotificationModule
import com.zeppelin.app.di.watchLinkModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class ZeppelinApp : Application(), KoinComponent {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.DEBUG) // Recommended for development
            androidContext(this@ZeppelinApp)
            modules(
                AuthModules,
                appModule,
                courseModules,
                courseDetailModules,
                courseSessionModules,
                pushNotificationModule,
                liveSessionModule,
                watchLinkModule
            )

            workManagerFactory()
        }

        registerNotificationChannel()
    }

    private fun registerNotificationChannel() {
         val channel =  NotificationChannel(
             "live_session_channel",
             "Live Session Channel",
                NotificationManager.IMPORTANCE_HIGH
         )
        val notificationManager = getSystemService(NotificationManager::class.java) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}