package com.zeppelin.app.screens.courseDetail.domain

import com.zeppelin.app.screens.courseDetail.data.ContentType
import com.zeppelin.app.screens.courseDetail.data.CourseDetail
import com.zeppelin.app.screens.courseDetail.data.CourseDetailApi
import com.zeppelin.app.screens.courseDetail.data.CourseDetailUI
import com.zeppelin.app.screens.courseDetail.data.CourseProgressApi
import com.zeppelin.app.screens.courseDetail.data.CourseProgressUI
import com.zeppelin.app.screens.courseDetail.data.GradeApi
import com.zeppelin.app.screens.courseDetail.data.GradeUI
import com.zeppelin.app.screens.courseDetail.data.Module
import com.zeppelin.app.screens.courseDetail.data.QuizSummary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


fun CourseDetailApi.toCourseDetailUI(): CourseDetailUI {
    return CourseDetailUI(
        id = id,
        subject = subject,
        course = course,
        description = description,
        imageUrl = imageUrl,
        grades = grades.map { it.toGradeUI() },
        progress = progress.toCourseProgressUI()
    )
}

fun GradeApi.toGradeUI(): GradeUI {
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


fun CourseDetail.toCourseDetailUI(quizSummary: List<QuizSummary>): CourseDetailUI {
    val quizzes = this.modules.flatMap { module ->
        module.content.filter { content ->
            content.contentTypeId == ContentType.QUIZ.ordinal + 1
        }
    }

    val quizzAnswers = quizSummary.filter { quiz ->
        quizzes.any { it.contentId == quiz.contentID }
    }

    val content = this.modules.flatMap { module ->
        module.content.filter { content ->
            content.contentTypeId != ContentType.QUIZ.ordinal + 1
        }
    }

    // TODO : Add logic to filter quizzes and content based on user progress
    val quizProgress = quizzes.filter { true }.size
    val contentProgress = content.filter { true }.size

    val lastModule = this.modules.lastOrNull()
        ?: Module(
            moduleId = 0,
            moduleName = "",
            moduleIndex = 0,
            content = emptyList()
        )
    val grades = quizzAnswers.map { quiz ->
        GradeUI(
            id = quiz.contentID.toString(),
            gradeName = quizzes.find { it.contentId == quiz.contentID }?.title
                ?: "Quiz ${quiz.contentID.subSequence(0, 5)}",
            grade = "${quiz.totalGrade}/${quiz.totalPoints}",
            dateGraded = formatDateFromMillis(quiz.lastQuizTime)
        )
    }

    return CourseDetailUI(
        id = this.id,
        subject = lastModule.moduleName,
        description = this.description,
        course = this.title,
        imageUrl = "https://images.unsplash.com/photo-1509228468518-180dd4864904?q=80&w=720&auto=format&fit=crop",
        grades = grades,
        progress = CourseProgressUI(
            contentProgress = "$contentProgress/${content.size}",
            contentPercentage = contentProgress.toFloat() / content.size.toFloat(),
            testProgress = "$quizProgress/${quizzes.size}",
            testPercentage = quizProgress.toFloat() / quizzes.size.toFloat()
        )

    )
}


fun formatDateFromMillis(millis: Long, pattern: String = "dd-MM-yyyy"): String {
    val date = Date(millis)
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(date)
}