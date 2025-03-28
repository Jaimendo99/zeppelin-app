package com.zeppelin.app.di


import com.zeppelin.app.screens.courses.data.CoursesRepository
import com.zeppelin.app.screens.courses.data.ICoursesRepository
import com.zeppelin.app.screens.courses.ui.CourseViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val courseModules = module {
    single<ICoursesRepository> { CoursesRepository() }
     viewModel { CourseViewModel(get()) }
}