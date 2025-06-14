package com.zeppelin.app.screens.courses.domain

import com.zeppelin.app.screens.courses.data.CourseCardData
import com.zeppelin.app.screens.courses.data.CourseCardWithProgress
import com.zeppelin.app.screens.courses.data.CourseWithProgress
import com.zeppelin.app.screens.courses.data.CoursesData
import kotlin.math.round

fun CoursesData.toCourseCardData(): CourseCardData {
    return CourseCardData(
        id = id,
        subject = subject,
        course = course,
        progress = "${round(progress.times(10000)).div(100)}%",
        imageUrl = imageUrl
    )
}

fun CourseWithProgress.toUi(): CourseCardWithProgress {
    return CourseCardWithProgress(
        courseId = courseId,
        title = title,
        startDate = startDate,
        description = description,
        moduleCount = moduleCount,
        videoCount = videoCount,
        textCount = textCount,
        quizCount = quizCount,
        lastModule = lastModule,
        percentage = percentage
    )
}
