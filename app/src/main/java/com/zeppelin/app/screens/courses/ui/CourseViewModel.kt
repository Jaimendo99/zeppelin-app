package com.zeppelin.app.screens.courses.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeppelin.app.screens.auth.domain.NetworkResult
import com.zeppelin.app.screens.courses.data.CourseCardData
import com.zeppelin.app.screens.courses.data.CourseCardWithProgress
import com.zeppelin.app.screens.courses.data.CourseWithProgress
import com.zeppelin.app.screens.courses.data.ICoursesRepository
import com.zeppelin.app.screens.courses.domain.toCourseCardData
import com.zeppelin.app.screens.courses.domain.toUi
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

    private val _courseWithProgress = MutableStateFlow<List<CourseCardWithProgress>>(emptyList())
    val courseWithProgress: StateFlow<List<CourseCardWithProgress>> = _courseWithProgress

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    private val _enableClick = MutableStateFlow(true)
    val enableClick: StateFlow<Boolean> = _enableClick

    init {
        loadCoursesWithProgress()
    }

    private fun loadCourses() {
        _loading.value = true
        viewModelScope.launch {
            repository.getCourses()
                .onSuccess {
                    _courses.value = it.map { it.toCourseCardData() }
                }
            _loading.value = false
        }
    }

    private fun loadCoursesWithProgress() {
        _loading.value = true
        viewModelScope.launch {
            when(val result = repository.getCoursesWithProgress()){
                is NetworkResult.Error -> {
                    Log.e("CourseViewModel", "Error loading courses with progress: ${result.exception}")
                }
                NetworkResult.Idle -> {}
                NetworkResult.Loading -> {
                    _loading.value = true
                }
                is NetworkResult.Success -> {
                    _courseWithProgress.value =  result.data.map { it.toUi() }
                }
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
