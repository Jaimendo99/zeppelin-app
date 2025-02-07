package com.zeppelin.app

import android.app.Application
import com.zeppelin.app.di.characterModules
import org.koin.core.context.startKoin
import org.koin.dsl.module

class ZeppelinApp : Application(){

    override fun onCreate() {
        super.onCreate()
            startKoin {
                modules(characterModules)
            }

    }
}