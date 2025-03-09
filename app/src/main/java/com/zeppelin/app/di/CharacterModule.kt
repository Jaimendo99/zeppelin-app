package com.zeppelin.app.di

import com.zeppelin.app.rickandmorty.data.CharacterRepository
import com.zeppelin.app.rickandmorty.data.DefaultCharacterRepo
import com.zeppelin.app.rickandmorty.data.NetworkClient
import com.zeppelin.app.rickandmorty.presentation.CharacterViewModel
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