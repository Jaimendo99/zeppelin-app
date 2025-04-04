package com.zeppelin.app.screens.courseDetail.data

interface ICourseDetailRepo {
    suspend fun getCourseDetail(id: Int): CourseDetailApi?
    suspend fun connectToSession(courseId:Int):Result<String>
}