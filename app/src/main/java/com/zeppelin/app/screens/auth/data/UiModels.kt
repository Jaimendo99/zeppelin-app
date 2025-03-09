package com.zeppelin.app.screens.auth.data

data class LoginEvents(
    val eventType : LoginEventsType,
    val eventData: String
)

enum class LoginEventsType{
    NAVIGATE,
    TOAST
}