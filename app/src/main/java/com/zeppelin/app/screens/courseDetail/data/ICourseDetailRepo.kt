package com.zeppelin.app.screens.courseDetail.data

import com.zeppelin.app.screens._common.data.RestClient
import com.zeppelin.app.screens._common.data.WebSocketState
import com.zeppelin.app.screens.auth.domain.NetworkResult

interface ICourseDetailRepo {
    suspend fun getCourseDetail(id: Int): Result<CourseDetail>
    suspend fun getQuizAnswers() : Result<List<QuizSummary>>
    suspend fun connectToSession(courseId:Int, retry:Boolean = false):Result<WebSocketState>
    suspend fun getCourseInfo(courseId: Int): NetworkResult<CourseDetailWithModules, RestClient.ErrorResponse>
    suspend fun getQuizAttempts(courseId: Int): NetworkResult<List<QuizAnswer>, RestClient.ErrorResponse>
     fun disconnectFromSession()
}