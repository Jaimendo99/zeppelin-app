package com.zeppelin.app.screens._common.data

sealed class PinningUiEvent {
    object StartPinning : PinningUiEvent()
    object StopPinning : PinningUiEvent()
}