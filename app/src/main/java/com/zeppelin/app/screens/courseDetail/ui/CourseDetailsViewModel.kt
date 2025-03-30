package com.zeppelin.app.screens.courseDetail.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeppelin.app.screens.courseDetail.data.CourseDetailUI
import com.zeppelin.app.screens.courseDetail.data.ICourseDetailRepo
import com.zeppelin.app.screens.courseDetail.domain.toCourseDetailUI
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class CourseDetailsViewModel(
    private val courseDetailRepo: ICourseDetailRepo
) : ViewModel() {

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    private val _courseDetail = MutableStateFlow<CourseDetailUI?>(null)
    val courseDetail: StateFlow<CourseDetailUI?> = _courseDetail

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun getCourseDetail(id: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            courseDetailRepo.getCourseDetail(id)?.let {
                _courseDetail.value = it.toCourseDetailUI()
            }
            _isLoading.value = false
        }
    }

    fun onSessionStartClick(courseId: Int) {
        viewModelScope.launch {
            _events.emit("session/$courseId")
        }
    }

}