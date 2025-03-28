package com.zeppelin.app

import android.app.Application
import com.zeppelin.app.di.AuthModules
import com.zeppelin.app.di.appModule
import com.zeppelin.app.di.characterModules
import com.zeppelin.app.di.courseModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ZeppelinApp : Application(){

    override fun onCreate() {
        super.onCreate()
            startKoin {
                androidContext(this@ZeppelinApp)
                modules(characterModules)
                modules(AuthModules)
                modules(appModule)
                modules(courseModules)
            }

    }
}