package com.zeppelin.app

import android.Manifest.permission
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.zeppelin.app.screens._common.ui.ScaffoldViewModel
import com.zeppelin.app.screens._common.ui.ZeppelinScaffold
import com.zeppelin.app.screens.nav.NavigationGraph
import com.zeppelin.app.ui.theme.ZeppelinTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        if (ContextCompat.checkSelfPermission(
                this,
                permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(permission.POST_NOTIFICATIONS), 0)
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
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