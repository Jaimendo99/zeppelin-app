package com.zeppelin.app.di

import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable
import com.zeppelin.app.screens._common.data.AnalyticsClient
import com.zeppelin.app.screens._common.data.ApiClient
import com.zeppelin.app.screens._common.ui.ScaffoldViewModel
import com.zeppelin.app.service.ILiveSessionPref
import com.zeppelin.app.service.LiveSessionPref
import com.zeppelin.app.service.distractionDetection.DistractionDetectionManager
import com.zeppelin.app.service.pushNotifications.FcmRepository
import com.zeppelin.app.service.pushNotifications.IFcmRepository
import com.zeppelin.app.service.pushNotifications.NotificationService
import com.zeppelin.app.service.pushNotifications.PushNotiPreferences
import com.zeppelin.app.service.wearCommunication.WatchProximityMonitor
import com.zeppelin.app.service.wearCommunication.WearCommunicator
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single {ScaffoldViewModel(get())}
    single { ApiClient(get()) }
    single { AnalyticsClient(get()) }
    single<ILiveSessionPref> { LiveSessionPref(get()) }
    single { DistractionDetectionManager(get()) }
}

val pushNotificationModule = module {
    single { PushNotiPreferences(get()) }
    single<IFcmRepository>{ FcmRepository(get(), get()) }
    single { NotificationService() }
    single { DistractionDetectionManager(get()) }
}

val liveSessionModule = module {
    single<NodeClient> { Wearable.getNodeClient(androidContext()) }
    single<MessageClient> { Wearable.getMessageClient(androidContext()) }
    single<DataClient> { Wearable.getDataClient(androidContext()) }
    single { WearCommunicator(get(), get()) }
    single { WatchProximityMonitor(get()) }
}