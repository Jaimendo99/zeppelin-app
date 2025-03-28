package com.zeppelin.app.screens.auth.ui

import android.widget.Toast
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.zeppelin.app.R
import com.zeppelin.app.screens._common.ui.Logo
import com.zeppelin.app.screens._common.ui.SpinningCircleWave
import com.zeppelin.app.screens.auth.data.LoginEventsType
import com.zeppelin.app.ui.theme.ZeppelinTheme

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    loginViewModel: LoginViewModel,
    navController: NavController
) {
    val loginScreenData = loginViewModel.loginState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        loginViewModel.loginEvents.collect {
            when (it.eventType) {
                LoginEventsType.NAVIGATE -> {
                    navController.navigate(it.eventData) {
                        popUpTo("auth") { inclusive = true }
                    }
                }

                LoginEventsType.TOAST -> {
                    Toast.makeText(context, it.eventData, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Logo(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .padding(top = 16.dp)
        )
        LoginState(
            Modifier.weight(1f), loginScreenData.value

        )
        Card(
            modifier = Modifier
                .fillMaxWidth()

        ) {
            LoginForm(
                modifier = Modifier.padding(16.dp),
                loginScreenData = loginScreenData.value,
                onLogin = { email, password -> loginViewModel.onLoginClick(email, password) }
            )
        }
    }
}


@Composable
fun LoginTitleDrawing() {
    val yoffset = -30f
    val infiniteTransition = rememberInfiniteTransition()

    val upAndDown by infiniteTransition.animateFloat(
        initialValue = yoffset,
        targetValue = -18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    Box(
        modifier = Modifier.padding(top = -yoffset.dp),
        contentAlignment = Alignment.Center
    ) {
        SpinningCircleWave()
        Image(
            painter = painterResource(id = R.drawable.login_person),
            contentDescription = "Login person",
            modifier = Modifier
                .size(185.dp)
                .offset(y = upAndDown.dp)
        )
    }
}


@Composable
@Preview(showBackground = true)
fun IdleLoginStatePreview() {
    ZeppelinTheme {
        LoginStateIdle()
    }
}
