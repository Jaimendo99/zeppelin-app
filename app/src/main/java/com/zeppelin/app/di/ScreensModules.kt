package com.zeppelin.app.di


import com.zeppelin.app.screens._common.data.SessionEventsManager
import com.zeppelin.app.screens._common.data.WebSocketClient
import com.zeppelin.app.screens.auth.data.AuthPreferences
import com.zeppelin.app.screens.auth.data.IAuthPreferences
import com.zeppelin.app.screens.courseDetail.data.CourseDetailRepo
import com.zeppelin.app.screens.courseDetail.data.ICourseDetailRepo
import com.zeppelin.app.screens.courseDetail.ui.CourseDetailsViewModel
import com.zeppelin.app.screens.courseSession.data.CourseSessionRepo
import com.zeppelin.app.screens.courseSession.data.ICourseSessionRepo
import com.zeppelin.app.screens.courseSession.ui.CourseSessionViewModel
import com.zeppelin.app.screens.courses.data.CoursesRepository
import com.zeppelin.app.screens.courses.data.ICoursesRepository
import com.zeppelin.app.screens.courses.ui.CourseViewModel
import com.zeppelin.app.screens.watchLink.data.WatchLinkRepository
import com.zeppelin.app.screens.watchLink.ui.WatchLinkViewModel
import com.zeppelin.app.watchLink.data.WatchScanner
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val courseModules = module {
    single<ICoursesRepository> { CoursesRepository(get(), get(), get()) }
     viewModel { CourseViewModel(get()) }
}

val courseDetailModules = module {
    single<IAuthPreferences> { AuthPreferences(get()) }
    single<WebSocketClient> { WebSocketClient(get()) }


    single<SessionEventsManager>{ SessionEventsManager()}

    single<ICourseDetailRepo> { CourseDetailRepo(context = get(), get(), get(), get()) }
    viewModel { CourseDetailsViewModel(get(), get(), get(), get(), get(), get()) }
}

val courseSessionModules = module {
    single<ICourseSessionRepo> { CourseSessionRepo() }
    viewModel { CourseSessionViewModel(get(), get()) }
}

val watchLinkModule = module {
    single { WatchLinkRepository(androidContext()) }
    factory { WatchScanner(androidContext()) }
    viewModel { WatchLinkViewModel(get(), get()) }
}
