package com.zeppelin.app.screens.courses.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeppelin.app.screens.courseDetail.data.CourseDetailRepo
import com.zeppelin.app.screens.courseDetail.data.ICourseDetailRepo
import com.zeppelin.app.screens.courses.data.CourseCardData
import com.zeppelin.app.screens.courses.data.ICoursesRepository
import com.zeppelin.app.screens.courses.domain.toCourseCardData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch


class CourseViewModel(
    private val repository: ICoursesRepository,
//    private val detailRepo: ICourseDetailRepo
) : ViewModel() {

    private val _courses = MutableStateFlow<List<CourseCardData>>(emptyList())
    val courses: StateFlow<List<CourseCardData>> = _courses

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _enableClick = MutableStateFlow(true)
    val enableClick: StateFlow<Boolean> = _enableClick

    init {
        viewModelScope.launch {
            _loading.value = true
            loadCourses()
            _loading.value = false
        }
    }

    private suspend fun loadCourses() {
        _courses.value = repository.getCourses().map { it.toCourseCardData() }
    }

    private suspend fun onProfileClick() {
        _events.emit("profile")
    }

    fun onCourseClick(courseId: Int) {
        viewModelScope.launch {
//            detailRepo.connectToSession(courseId)
            _enableClick.value = false
            _events.emit("courseDetail/$courseId")
            delay(500)
            _enableClick.value = true
        }
    }
}
