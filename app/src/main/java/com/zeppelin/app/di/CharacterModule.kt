package com.zeppelin.app.di

import com.zeppelin.app.data.CharacterRepository
import com.zeppelin.app.data.DefaultCharacterRepo
import com.zeppelin.app.data.NetworkClient
import com.zeppelin.app.presentation.CharacterViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val characterModules = module {
    single<NetworkClient> {
        NetworkClient()
    }
    single<CharacterRepository> {
        DefaultCharacterRepo(get())
    }
    viewModel {
        CharacterViewModel(get())
    }
}