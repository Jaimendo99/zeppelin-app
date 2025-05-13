package com.zeppelin.app.service.pushNotifications

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zeppelin.app.screens.auth.domain.NetworkResult

class UploadTokenWorker(
    appContext: Context, workerParams: WorkerParameters,
    private val fcmRepository: IFcmRepository
) :
    CoroutineWorker(appContext, workerParams) {
    val TAG = "UploadTokenWorker"

    override suspend fun doWork(): Result {
        Log.d(TAG, "doWork: ")
        return when (val result = fcmRepository.uploadToken()) {
            is NetworkResult.Success -> {
                Log.d(TAG, "doWork: ${result.data}")
                Result.success()
            }

            is NetworkResult.Error -> {
                Log.e(TAG, "doWork: ${result.errorBody}")
                Result.retry()
            }

            NetworkResult.Idle, NetworkResult.Loading -> {
                Log.d(TAG, "doWork: Idle or loading")
                Result.failure()
            }
        }
    }
}