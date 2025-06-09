package com.zeppelin.app.screens.courses.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
) : ViewModel() {

    private val _courses = MutableStateFlow<List<CourseCardData>>(emptyList())
    val courses: StateFlow<List<CourseCardData>> = _courses

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    private val _enableClick = MutableStateFlow(true)
    val enableClick: StateFlow<Boolean> = _enableClick

    init {
        loadCourses()
    }

    private fun loadCourses() {
        _loading.value = true
        viewModelScope.launch {
            delay(1000)
            repository.getCourses()
                .onSuccess {
                    _courses.value = it.map { it.toCourseCardData() }
                }
            _loading.value = false
        }
    }

    private suspend fun onProfileClick() {
        _events.emit("profile")
    }

    fun onCourseClick(courseId: Int) {
        viewModelScope.launch {
            _enableClick.value = false
            _events.emit("courseDetail/$courseId")
            delay(500)
            _enableClick.value = true
        }
    }
}
