package com.zeppelin.app.di


import com.zeppelin.app.screens.courseDetail.data.CourseDetailRepo
import com.zeppelin.app.screens.courseDetail.data.ICourseDetailRepo
import com.zeppelin.app.screens.courseDetail.ui.CourseDetailsViewModel
import com.zeppelin.app.screens.courseSession.data.CourseSessionRepo
import com.zeppelin.app.screens.courseSession.data.ICourseSessionRepo
import com.zeppelin.app.screens.courseSession.ui.CourseSessionViewModel
import com.zeppelin.app.screens.courses.data.CoursesRepository
import com.zeppelin.app.screens.courses.data.ICoursesRepository
import com.zeppelin.app.screens.courses.ui.CourseViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val courseModules = module {
    single<ICoursesRepository> { CoursesRepository() }
     viewModel { CourseViewModel(get()) }
}

val courseDetailModules = module {
    single<ICourseDetailRepo> { CourseDetailRepo() }
    viewModel { CourseDetailsViewModel(get()) }
}

val courseSessionModules = module {
    single<ICourseSessionRepo> { CourseSessionRepo() }
    viewModel { CourseSessionViewModel(get()) }
}

