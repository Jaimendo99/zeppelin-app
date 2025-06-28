package com.zeppelin.app.service.wearCommunication

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


interface IWatchMetricsRepository {
    val hearRate: Flow<Int?>
    val movementDetected: Flow<Float?> // Now nullable Float? to signify absence of value
    val offWrist: Flow<Boolean?>
    val onWrist: Flow<Boolean?>
    val rssi: Flow<Int?>

    fun emitHeartRate(value: Int?)
    fun emitMovementDetected(value: Float?) // Changed to accept Float?
    fun emitOffWrist(isOffWrist: Boolean?)
    fun emitOnWrist(isOnWrist: Boolean?)
    fun emitRSSI(value: Int?)
}
class WatchMetricsRepository : IWatchMetricsRepository {

    private val _hearRate = MutableStateFlow<Int?>(null)
    override val hearRate: Flow<Int?> = _hearRate.asStateFlow()

    private val _movementDetected = MutableStateFlow<Float?>(null)
    override val movementDetected: Flow<Float?> = _movementDetected.asStateFlow()

    private val _offWrist = MutableStateFlow<Boolean?>(null)
    override val offWrist: Flow<Boolean?> = _offWrist.asStateFlow()

    private val _onWrist = MutableStateFlow<Boolean?>(null)
    override val onWrist: Flow<Boolean?> = _onWrist.asStateFlow()

    private val _rssi = MutableStateFlow<Int?>(null)
    override val rssi: Flow<Int?> = _rssi.asStateFlow()

    // Emit functions now simply update the value of the StateFlow
    override fun emitHeartRate(value: Int?) {
        _hearRate.value = value
    }

    override fun emitMovementDetected(value: Float?) {
        _movementDetected.value = value
    }

    override fun emitOffWrist(isOffWrist: Boolean?) {
        _offWrist.value = isOffWrist
    }

    override fun emitOnWrist(isOnWrist: Boolean?) {
        _onWrist.value = isOnWrist
    }

    override fun emitRSSI(value: Int?) {
        _rssi.value = value
    }
}