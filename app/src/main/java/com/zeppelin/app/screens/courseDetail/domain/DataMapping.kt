package com.zeppelin.app.screens.courseDetail.domain

import com.zeppelin.app.screens.courseDetail.data.CourseDetailApi
import com.zeppelin.app.screens.courseDetail.data.CourseDetailUI
import com.zeppelin.app.screens.courseDetail.data.CourseProgressApi
import com.zeppelin.app.screens.courseDetail.data.CourseProgressUI
import com.zeppelin.app.screens.courseDetail.data.GradeApi
import com.zeppelin.app.screens.courseDetail.data.GradeUI
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


fun CourseDetailApi.toCourseDetailUI(): CourseDetailUI{
    return CourseDetailUI(
        id = id,
        course = course,
        title = title,
        description = description,
        imageUrl = imageUrl,
        grades = grades.map { it.toGradeUI() },
        progress = progress.toCourseProgressUI()
    )
}

fun GradeApi.toGradeUI(): GradeUI{
    return GradeUI(
        id = id,
        gradeName = gradeName,
        grade = "$gradeValue/100",
        dateGraded = formatDateFromMillis(dateGraded)
    )
}

fun CourseProgressApi.toCourseProgressUI(): CourseProgressUI {
    return CourseProgressUI(
        contentProgress = "$viewedContent/$fullContent",
        contentPercentage = viewedContent.toFloat() / fullContent.toFloat(),
        testProgress = "$passedTests/$fullTests",
        testPercentage = passedTests.toFloat() / fullTests.toFloat()
    )
}

fun formatDateFromMillis(millis: Long, pattern: String = "dd-MM-yyyy"): String {
    val date = Date(millis)
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(date)
}