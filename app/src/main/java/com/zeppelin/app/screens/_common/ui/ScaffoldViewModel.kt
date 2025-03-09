package com.zeppelin.app.screens._common.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeppelin.app.screens.auth.domain.AuthManager
import kotlinx.coroutines.launch

class ScaffoldViewModel(
    private val authManager: AuthManager
): ViewModel() {
    fun onProfileLongPressed() {
        viewModelScope.launch {
            authManager.logout()
        }
    }
}