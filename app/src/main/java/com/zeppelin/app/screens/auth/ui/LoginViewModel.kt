package com.zeppelin.app.screens.auth.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.zeppelin.app.screens.auth.data.ErrorResponse
import com.zeppelin.app.screens.auth.data.IAuthRepository
import com.zeppelin.app.screens.auth.data.LoginEvents
import com.zeppelin.app.screens.auth.data.LoginEventsType
import com.zeppelin.app.screens.auth.data.Session
import com.zeppelin.app.screens.auth.domain.AuthManager
import com.zeppelin.app.screens.auth.domain.NetworkResult
import com.zeppelin.app.service.pushNotifications.UploadTokenWorker
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: IAuthRepository,
    private val authManager: AuthManager,
    private val workManager: WorkManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<NetworkResult<Session.SessionToken, ErrorResponse>>(NetworkResult.Idle)
    val loginState: StateFlow<NetworkResult<Session.SessionToken, ErrorResponse>> = _loginState

    private val _loginEvents = MutableSharedFlow<LoginEvents>()
    val loginEvents = _loginEvents.asSharedFlow()


    fun onLoginClick(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = NetworkResult.Loading
            _loginState.value = repository.login(email, password)
            Log.d("LoginViewModel", "onLoginClick: ${_loginState.value}")
            if (_loginState.value is NetworkResult.Success) {
                authManager.saveToken((_loginState.value as NetworkResult.Success).data.jwt)
                authManager.saveUserId((_loginState.value as NetworkResult.Success).data.jwt)
                onLoginSuccess()
            }
        }
        Log.d("LoginViewModel", "onLoginClick: $email, $password")
    }

    fun onLogoutClick() {
        viewModelScope.launch {
            authManager.logout()
            _loginEvents.emit(LoginEvents(LoginEventsType.TOAST, "Logout Success"))
            _loginEvents.emit(LoginEvents(LoginEventsType.NAVIGATE, "auth"))
        }
    }

    private fun uploadFcmToken(){
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val uploadRequest = OneTimeWorkRequestBuilder<UploadTokenWorker>()
                .setConstraints(constraints)
                .build()

            workManager.enqueueUniqueWork(
                "uploadTokenWork",
                ExistingWorkPolicy.REPLACE,
                uploadRequest
            )
    }

    private suspend fun onLoginSuccess() {
        uploadFcmToken()
        _loginEvents.emit(LoginEvents(LoginEventsType.TOAST, "Login Success"))
        _loginEvents.emit(LoginEvents(LoginEventsType.NAVIGATE, "main"))
    }
}
