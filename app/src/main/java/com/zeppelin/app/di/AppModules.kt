package com.zeppelin.app.di

import com.zeppelin.app.screens._common.ui.ScaffoldViewModel
import org.koin.dsl.module

val appModule = module {
    single {ScaffoldViewModel(get())}
}