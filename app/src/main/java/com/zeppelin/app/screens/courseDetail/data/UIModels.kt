package com.zeppelin.app.screens.courseDetail.data

import com.zeppelin.app.screens.courseDetail.data.CourseDetailWithModules.ContentStatus


data class CourseDetailUI(
    val id: Int = 0,
    val subject: String = "",
    val course: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val grades: List<GradeUI> = listOf(),
    val progress: CourseProgressUI = CourseProgressUI("", 0f, "", 0f)
)

data class CourseProgressUI(
    val contentProgress : String,
    val contentPercentage: Float,
    val testProgress : String,
    val testPercentage: Float
)

data class GradeUI(
    val id: String,
    val gradeName: String,
    val grade: String,
    val dateGraded: String,
)
data class CourseDetailModulesUIState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val courseDetailModulesUI: CourseDetailModulesUI = CourseDetailModulesUI()
)

data class CourseDetailModulesUI(
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val startDate: String = "",
    val progress: CourseProgressUI = CourseProgressUI("", 0f, "", 0f),
    val modules: List<ModuleListUI> = listOf()
)

data class QuizGradesUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val showDetails : Boolean = false,
    val quizGrades: List<QuizGradesUi> = listOf(),
)
data class QuizGradesUi(
    val contentId: String,
    val startTime: String,
    val endTime: String,
    val title: String,
    val grade: String,
    val finalGrade: Boolean,
    val reviewedAt: String,
    val description: String
)

data class ModuleListUI(
    val moduleName: String,
    val moduleIndex: Int,
    val contentCount: Int,
    val showContent: Boolean = false,
    val isLoading: Boolean = false,
    val content: List<ContentItemUI>
){
    data class ContentItemUI(
        val contentId: String,
        val title: String,
        val contentType: CourseDetailWithModules.ContentType,
        val contentStatus: ContentStatus,
    )
}