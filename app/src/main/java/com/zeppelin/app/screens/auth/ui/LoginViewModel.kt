package com.zeppelin.app.screens.auth.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeppelin.app.screens.auth.data.ErrorResponseBody
import com.zeppelin.app.screens.auth.data.IAuthRepository
import com.zeppelin.app.screens.auth.data.LoginEvents
import com.zeppelin.app.screens.auth.data.LoginEventsType
import com.zeppelin.app.screens.auth.data.SignInResponse
import com.zeppelin.app.screens.auth.domain.AuthManager
import com.zeppelin.app.screens.auth.domain.NetworkResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: IAuthRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<NetworkResult<SignInResponse, ErrorResponseBody>>(NetworkResult.Idle)
    val loginState: StateFlow<NetworkResult<SignInResponse, ErrorResponseBody>> = _loginState


    private val _loginEvents = MutableSharedFlow<LoginEvents>()
    val loginEvents = _loginEvents.asSharedFlow()


    fun onLoginClick(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = NetworkResult.Loading
            _loginState.value = repository.login(email, password)
            if (_loginState.value is NetworkResult.Success) {
                authManager.saveToken((_loginState.value as NetworkResult.Success).data.client.sessions[0].lastActiveToken.jwt)
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

    private suspend fun onLoginSuccess() {
        _loginEvents.emit(LoginEvents(LoginEventsType.TOAST, "Login Success"))
        _loginEvents.emit(LoginEvents(LoginEventsType.NAVIGATE, "main"))
    }
}
