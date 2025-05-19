package com.zeppelin.app.di

import com.zeppelin.app.screens._common.data.ApiClient
import com.zeppelin.app.screens._common.ui.ScaffoldViewModel
import com.zeppelin.app.service.distractionDetection.DistractionDetectionManager
import com.zeppelin.app.service.pushNotifications.FcmRepository
import com.zeppelin.app.service.pushNotifications.IFcmRepository
import com.zeppelin.app.service.pushNotifications.NotificationService
import com.zeppelin.app.service.pushNotifications.PushNotiPreferences
import com.zeppelin.app.service.pushNotifications.UploadTokenWorker
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val appModule = module {
    single {ScaffoldViewModel(get())}
    single { ApiClient(get()) }
}

val pushNotificationModule = module {
    single { PushNotiPreferences(get()) }
    single<IFcmRepository>{ FcmRepository(get(), get()) }
    single { NotificationService() }
    single { DistractionDetectionManager(get()) }
}