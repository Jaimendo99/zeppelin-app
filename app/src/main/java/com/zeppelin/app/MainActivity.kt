package com.zeppelin.app

import android.Manifest
import android.app.ActivityManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.zeppelin.app.screens._common.data.PinningUiEvent
import com.zeppelin.app.screens._common.data.SessionEventsManager
import com.zeppelin.app.screens._common.ui.ScaffoldViewModel
import com.zeppelin.app.screens._common.ui.ZeppelinScaffold
import com.zeppelin.app.screens.auth.domain.AuthManager
import com.zeppelin.app.screens.nav.NavigationGraph
import com.zeppelin.app.service.distractionDetection.DistractionDetectionManager
import com.zeppelin.app.ui.theme.ZeppelinTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    private val authManager: AuthManager by inject()
    private val sessionEventsManager: SessionEventsManager by inject()
    private val distractionDetectionManager: DistractionDetectionManager by inject()
    private val activityManager by lazy {
        getSystemService(ACTIVITY_SERVICE) as ActivityManager
    }
    private var isAppCurrentlyPinned = false

    private val requestPermissionsLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            permissions.entries.forEach { entry ->
                Log.i( "PermissionRequest", "Permission: ${entry.key}, Granted: ${entry.value}" )
                if (!entry.value) {
                    Log.w( "PermissionRequest", "Permission ${entry.key} was denied." )
                }
            }
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // Call super.onCreate first

        val keepSplash: MutableStateFlow<Boolean?> = MutableStateFlow(null)

        installSplashScreen().apply {
            setKeepOnScreenCondition {
                keepSplash.value == null || keepSplash.value == true // Keep splash if null or explicitly true
            }
        }

        enableEdgeToEdge()
        setContent {
            val isLoggedIn = authManager.observeAuthState().collectAsState(null)

            LaunchedEffect(isLoggedIn.value) { // Re-evaluate when isLoggedIn.value changes
                if (isLoggedIn.value != null) { // Only proceed if auth state is determined
                    delay(1000) // Keep splash for a bit longer after auth state is known
                    keepSplash.value = false // Hide splash
                }
            }

            LaunchedEffect(Unit) {
                sessionEventsManager.pinningUiEventFlow.collect { event ->
                    when (event) {
                        is PinningUiEvent.StartPinning -> {
                            try {
                                startLockTask()
                                isAppCurrentlyPinned = true // App initiated pinning
                                Log.d(
                                    "MainActivity",
                                    "Called startLockTask(), isAppCurrentlyPinned = true"
                                )
                            } catch (e: Exception) {
                                Log.e("MainActivity", "Failed to start lock task on event", e)
                            }
                        }

                        is PinningUiEvent.StopPinning -> {
                            try {
                                if (activityManager.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE) {
                                    stopLockTask()
                                    Log.d("MainActivity", "Called stopLockTask()")
                                } else {
                                    Log.d( "MainActivity", "Skipped stopLockTask app is not pinned.")
                                }
                                isAppCurrentlyPinned = false // App initiated/acknowledged unpinning
                            } catch (e: Exception) {
                                Log.e("MainActivity", "Failed to stop lock task on event", e)
                            }
                        }
                    }
                }
            }

            // e.g. in MainActivity.onCreate

            ZeppelinTheme(dynamicColor = false) {
                val navController = rememberNavController()
                ZeppelinScaffold(
                    koinViewModel<ScaffoldViewModel>(),
                    navController
                ) { innerPadding, navHostController ->
                    NavigationGraph(
                        Modifier.padding(innerPadding),
                        navHostController,
                    )
                }
            }
        }

        if (!distractionDetectionManager.hasUsageStatsPermission()) {
            distractionDetectionManager.requestUsageStatsPermission()
        }
        checkAndRequestPermissions()
    }
    private fun checkAndRequestPermissions() {
        val permissionsToAskFor = mutableListOf<String>()

        // 1. POST_NOTIFICATIONS (Required for Android 13 (API 33) and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToAskFor.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // 2. BLUETOOTH_CONNECT (Required for Android 12 (API 31) and above)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToAskFor.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        if (permissionsToAskFor.isNotEmpty()) {
            Log.i( "PermissionRequest", "Requesting permissions: ${permissionsToAskFor.joinToString()}" )
            requestPermissionsLauncher.launch(permissionsToAskFor.toTypedArray())
        } else {
            Log.i( "PermissionRequest", "All necessary runtime permissions already granted." )
        }
    }
}




@OptIn(ExperimentalSharedTransitionApi::class)
data class SharedTransitionScopes(
    val animatedVisibilityScope: AnimatedVisibilityScope,
    val sharedTransitionScope: SharedTransitionScope
)

val LocalSharedTransitionScopes =
    compositionLocalOf<SharedTransitionScopes> {
        error("SharedTransitionScopes not provided")
    }