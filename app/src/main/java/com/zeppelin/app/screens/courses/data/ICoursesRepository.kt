package com.zeppelin.app.screens.courses.data

import com.zeppelin.app.screens.nav.Screens

fun interface ICoursesRepository {
    suspend fun getCourses(): List<CoursesData>
}