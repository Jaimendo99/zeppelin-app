package com.zeppelin.app

import android.Manifest.permission
import android.app.ActivityManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
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
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    private val authManager: AuthManager by inject()
    private val sessionEventsManager: SessionEventsManager by inject()
    private val distractionDetectionManager: DistractionDetectionManager by inject()
    private val activityManager by lazy {
        getSystemService(ACTIVITY_SERVICE) as ActivityManager
    }
    private var isAppCurrentlyPinned =
        false // Tracks if the app initiated pinning


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // Call super.onCreate first

        // Request POST_NOTIFICATIONS permission
        if (ContextCompat.checkSelfPermission(
                this,
                permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(permission.POST_NOTIFICATIONS), 0)
        }

        // Check and request Usage Stats permission
        if (!distractionDetectionManager.hasUsageStatsPermission()) {
            distractionDetectionManager.requestUsageStatsPermission()
        }

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