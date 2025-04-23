package com.zeppelin.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.zeppelin.app.di.AuthModules
import com.zeppelin.app.di.appModule
import com.zeppelin.app.di.characterModules
import com.zeppelin.app.di.courseDetailModules
import com.zeppelin.app.di.courseModules
import com.zeppelin.app.di.courseSessionModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ZeppelinApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ZeppelinApp)
            modules(characterModules)
            modules(AuthModules)
            modules(appModule)
            modules(courseModules)
            modules(courseDetailModules)
            modules(courseSessionModules)
        }
        registerNotificationChannel()

    }
    private fun registerNotificationChannel() {
         val channel =  NotificationChannel(
             "live_session_channel",
             "Live Session Channel",
                NotificationManager.IMPORTANCE_LOW
         )
        val notificationManager = getSystemService(NotificationManager::class.java) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}