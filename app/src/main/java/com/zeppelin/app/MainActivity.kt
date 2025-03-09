package com.zeppelin.app

import android.Manifest.permission
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.zeppelin.app.screens._common.ui.ScaffoldViewModel
import com.zeppelin.app.screens._common.ui.ZeppelinScaffold
import com.zeppelin.app.screens.nav.NavigationGraph
import com.zeppelin.app.screens.nav.Screens
import com.zeppelin.app.ui.theme.ZeppelinTheme
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {


        if (ContextCompat.checkSelfPermission(this, permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            val request_code = 0
            ActivityCompat.requestPermissions(
                this,
                arrayOf(permission.POST_NOTIFICATIONS),
                request_code
            )
        }


        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZeppelinTheme(dynamicColor = false) {
                val navController = rememberNavController()
                ZeppelinScaffold( koinViewModel<ScaffoldViewModel>(), navController) { innerPadding, navHostController ->
                    NavigationGraph(
                        Modifier.padding(innerPadding),
                        navHostController,
                    )
                }
            }
        }
    }
}