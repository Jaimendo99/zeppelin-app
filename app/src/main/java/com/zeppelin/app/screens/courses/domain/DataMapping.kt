package com.zeppelin.app.screens.courses.domain

import com.zeppelin.app.screens.courses.data.CourseCardData
import com.zeppelin.app.screens.courses.data.CoursesData

fun CoursesData.toCourseCardData(): CourseCardData {
    return CourseCardData(
        id = id,
        subject = subject,
        course = course,
        progress = "${progress*100}%",
        imageUrl = imageUrl
    )
}