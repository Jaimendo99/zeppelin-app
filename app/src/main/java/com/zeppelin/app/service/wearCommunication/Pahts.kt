package com.zeppelin.app.service.wearCommunication



/*
/event/off_wrist
/event/movement_detected
/data/heart_rate_summary
/command/start_monitoring
/command/stop_monitoring
 */
sealed class WearOsPaths(val path: String) {
    object EventOffWrist : WearOsPaths("/event/off_wrist")
    object EventOnWrist : WearOsPaths("/event/on_wrist")
    object EventMovementDetected : WearOsPaths("/event/movement_detected")
    object DataHeartRateSummary : WearOsPaths("/data/heart_rate_summary")
    object CommandStartMonitoring : WearOsPaths("/command/start_monitoring")
    object CommandStopMonitoring : WearOsPaths("/command/stop_monitoring")
    object CommandLiveSessionConnected : WearOsPaths("/command/live_session_connected")
    object CommandWorkPhase : WearOsPaths("/command/work_phase_started")
    object CommandBreakPhase : WearOsPaths("/command/break_phase_started")
}