package com.zeppelin.app.screens.courses.data

import com.zeppelin.app.screens._common.data.RestClient
import com.zeppelin.app.screens.auth.domain.NetworkResult

interface ICoursesRepository {
    suspend fun getCourses(): Result<List<CoursesData>>
    suspend fun getCoursesWithProgress(): NetworkResult<List<CourseWithProgress>, RestClient.ErrorResponse >
}