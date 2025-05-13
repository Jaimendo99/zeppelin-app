package com.zeppelin.app.di

import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.zeppelin.app.screens.auth.data.AuthNetworkClient
import com.zeppelin.app.screens.auth.data.AuthPreferences
import com.zeppelin.app.screens.auth.data.AuthRepository
import com.zeppelin.app.screens.auth.data.IAuthRepository
import com.zeppelin.app.screens.auth.domain.AuthManager
import com.zeppelin.app.screens.auth.ui.LoginViewModel
import com.zeppelin.app.service.pushNotifications.FcmRepository
import com.zeppelin.app.service.pushNotifications.IFcmRepository
import com.zeppelin.app.service.pushNotifications.UploadTokenWorker
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val AuthModules = module {
    single<AuthNetworkClient> { AuthNetworkClient() }

    single<IFcmRepository> { FcmRepository(get(), get()) }

    single<IAuthRepository> { AuthRepository(get()) }

    single { WorkManager.getInstance(androidContext()) }

    worker { (workerParams: WorkerParameters) ->
        UploadTokenWorker(
            appContext = androidContext(),
            workerParams = workerParams,
            fcmRepository = get()
        )
    }

    viewModel { LoginViewModel(get(), get(), get()) }

    single { AuthPreferences(get()) }

    single { AuthManager(get()) }
}

