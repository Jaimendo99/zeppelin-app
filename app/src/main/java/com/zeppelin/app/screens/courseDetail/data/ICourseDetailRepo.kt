package com.zeppelin.app.screens.courseDetail.data

import com.zeppelin.app.screens._common.data.WebSocketState

interface ICourseDetailRepo {
    suspend fun getCourseDetail(id: Int): Result<CourseDetail>
    suspend fun getQuizAnswers() : Result<List<QuizSummary>>
    suspend fun connectToSession(courseId:Int, retry:Boolean = false):Result<WebSocketState>
     fun disconnectFromSession()
}