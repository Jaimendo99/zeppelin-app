package com.zeppelin.app.di

import com.zeppelin.app.screens.auth.data.AuthNetworkClient
import com.zeppelin.app.screens.auth.data.AuthPreferences
import com.zeppelin.app.screens.auth.data.AuthRepository
import com.zeppelin.app.screens.auth.data.IAuthRepository
import com.zeppelin.app.screens.auth.domain.AuthManager
import com.zeppelin.app.screens.auth.ui.LoginViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val AuthModules = module {
    single<AuthNetworkClient> { AuthNetworkClient() }

    single<IAuthRepository> { AuthRepository(get()) }

    viewModel { LoginViewModel(get(), get()) }

    single { AuthPreferences(get()) }

    single { AuthManager(get()) }
}

