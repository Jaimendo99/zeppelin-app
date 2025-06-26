package com.zeppelin.app.screens.courseSession.ui

import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.EaseInOutExpo
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.zeppelin.app.R
import com.zeppelin.app.screens._common.data.CurrentPhase
import com.zeppelin.app.screens._common.data.PomodoroState
import com.zeppelin.app.screens._common.data.TimerDigits
import com.zeppelin.app.screens._common.ui.ButtonWithLoader
import com.zeppelin.app.screens._common.ui.CardWithTitle
import com.zeppelin.app.screens.courseDetail.data.CourseDetailModulesUIState
import com.zeppelin.app.screens.courseDetail.data.CourseDetailUI
import com.zeppelin.app.screens.courseDetail.ui.CourseDetailsViewModel
import com.zeppelin.app.screens.courseSession.data.WatchMetricLists
import com.zeppelin.app.ui.theme.ZeppelinTheme
import com.zeppelin.app.ui.theme.bodyFontFamily
import com.zeppelin.app.ui.theme.displayFontFamily
import kotlin.math.abs

@Composable
fun CourseSessionScreen(
    modifier: Modifier = Modifier,
    id: String,
    navController: NavController,
    courseViewModel: CourseDetailsViewModel
) {

    val courseDetail by courseViewModel.courseInfo.collectAsState()
    val pomodoroState by courseViewModel.pomodoroState.collectAsState()
    val sessionMetrics by courseViewModel.allMetricsHistory.collectAsState()

    BackHandler {
        if (pomodoroState.currentPhase == CurrentPhase.WORK) {
            Log.d("CourseSession", "End session")
        }
    }

    LaunchedEffect(Unit) {
        courseViewModel.events.collect { event ->
            navController.navigate(event) {
            }
        }
    }

        LaunchedEffect(key1 = "data/$id") { courseViewModel.loadCourseDetails(id.toInt()) }

    if (courseDetail == null) return
    CourseSessionContent(
        pomodoroState = pomodoroState,
        courseDetail = courseDetail,
        metrics = sessionMetrics,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
    ) {
        courseViewModel.onDisconnect()
    }
}


data class ContentViewData(
    val url: String = "",
    val backgroundColor: Color = Color.Transparent,
    val textColor: Color = Color.Transparent,
    val token: String = "",
) {
    companion object

    fun buildUrl(): String {
        return "$url?backgroundColor=${backgroundColor.toArgb()}&textColor=${textColor.toArgb()}"
    }
}

@Composable
fun CourseSessionContent(
    pomodoroState: PomodoroState,
    metrics: WatchMetricLists,
    courseDetail: CourseDetailModulesUIState,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp)

        ) {
            Text(
                text = courseDetail.courseDetailModulesUI.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            PomodoroPhase(phase = pomodoroState.currentPhase)
        }
        Spacer(modifier = Modifier.height(8.dp))
        PomodoroClock(pomodoroState = pomodoroState)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Course Content",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(start = 16.dp)
                .align(Alignment.CenterHorizontally)
        )
        Box(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (pomodoroState.currentPhase != CurrentPhase.NONE)
//                ContentView1(
//                    URL = contentViewData.buildUrl(),
//                    token = contentViewData.token,
//                    modifier = Modifier.fillMaxSize()
//                )
                MetricsPlot(metrics)
            else ContentEmpty()
        }
        ButtonWithLoader(
            modifier = Modifier.padding(bottom = 48.dp),
            colors = ButtonDefaults.buttonColors()
                .copy(containerColor = MaterialTheme.colorScheme.error),
            onLongPress = { onBackPressed() }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(Icons.Rounded.Close, contentDescription = "End Session")
                Spacer(Modifier.width(4.dp))
                Text("End Session")
            }
        }
    }
}


@Composable
fun ContentView1(URL: String, token: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }

    Box {
        AndroidView(
            factory = {
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            isLoading = false     // page done
                        }
                    }

                    val headers = mapOf("Authorization" to "Bearer $token")
                    loadUrl(URL, headers)
                }
            },
            modifier = modifier
                .fillMaxSize()
                .alpha(if (isLoading) 0f else 1f)   // hide while loading
        )

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}


@Composable
fun ContentEmpty(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val infiniteTransition = rememberInfiniteTransition()
        val initialValue = 0f
        val targetValue = 360f
        val rotation by infiniteTransition.animateFloat(
            initialValue = initialValue,
            targetValue = targetValue,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 20000, easing = EaseInOutExpo),
                repeatMode = RepeatMode.Restart
            )
        )
        Box {
            Icon(
                painter = painterResource(id = R.drawable.wavy_cirlce),
                contentDescription = "Wavy Circle",
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(200.dp)
                    .rotate(rotation),
                tint = MaterialTheme.colorScheme.primaryContainer
            )
            Icon(
                painter = painterResource(id = R.drawable.wavy_cirlce),
                contentDescription = "Wavy Circle",
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(150.dp)
                    .rotate(abs(rotation - 360)),
                tint = MaterialTheme.colorScheme.secondaryContainer
            )
            Icon(
                painter = painterResource(id = R.drawable.wavy_cirlce),
                contentDescription = "Wavy Circle",
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(100.dp)
                    .rotate(rotation),
                tint = MaterialTheme.colorScheme.primaryContainer
            )

        }

        Text(
            "No Content", style = MaterialTheme.typography.titleSmall,
            modifier = Modifier
                .padding(top = 16.dp)
        )
        Text(
            "Pay Attention to the Class", style = MaterialTheme.typography.titleSmall,
            modifier = Modifier
        )
    }

}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CourseSessionContentPreview() {
    ZeppelinTheme {
        Scaffold { innerPadding ->
            CourseSessionContent(
                PomodoroState().copy(currentPhase = CurrentPhase.WORK),
                metrics = WatchMetricLists(),
                courseDetail = CourseDetailModulesUIState( ),
                Modifier.padding(innerPadding)
            ) {}
        }
    }
}


@Composable
fun PomodoroPhase(phase: CurrentPhase) {

    AnimatedContent(phase) { p ->
        var phaseName = ""
        var phaseColor = Color.Transparent
        when (p) {
            CurrentPhase.WORK -> {
                phaseName = stringResource(id = R.string.workPhase)
                phaseColor = MaterialTheme.colorScheme.primaryContainer
            }

            CurrentPhase.BREAK -> {
                phaseName = stringResource(id = R.string.breakPhase)
                phaseColor = MaterialTheme.colorScheme.secondaryContainer
            }

            CurrentPhase.NONE -> {
                phaseName = stringResource(id = R.string.nosession)
                phaseColor = Color.Gray
            }
        }
        val infiniteTransition = rememberInfiniteTransition()
        val blinking by infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            )
        )
        Surface(
            modifier = Modifier
                .shadow(1.dp, RoundedCornerShape(50))
                .clip(RoundedCornerShape(50)), tonalElevation = 4.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier
                        .alpha(if (phase != CurrentPhase.NONE) blinking else 1f)
                        .padding(start = 8.dp)
                        .width(7.dp)
                        .height(7.dp)
                        .clip(RoundedCornerShape(50))
                        .background(phaseColor)

                ) {}
                Text(
                    phaseName,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 8.dp,
                        top = 4.dp,
                        bottom = 4.dp
                    ),
                )
            }
        }
    }
}

@Composable
@Preview
fun PomodoroPhasePreview() {
    ZeppelinTheme {
        PomodoroPhase(CurrentPhase.WORK)
    }
}

@Composable
fun PomodoroClock(modifier: Modifier = Modifier, pomodoroState: PomodoroState) {
    CardWithTitle(modifier, "Session Timer") {
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            AnimatedContent(pomodoroState.currentPhase) { phase ->
                when (phase) {
                    CurrentPhase.WORK, CurrentPhase.BREAK -> PomodoroSessionOn(
                        pomodoroState,
                        Modifier.padding(vertical = 16.dp)
                    )

                    CurrentPhase.NONE -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            Text(
                                modifier = Modifier.align(Alignment.Center),
                                text = stringResource(id = R.string.session_not_started),
                                style = TextStyle(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = bodyFontFamily
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PomodoroSessionOn(pomodoroState: PomodoroState, modifier: Modifier = Modifier) {
    val nextPhase = when (pomodoroState.currentPhase) {
        CurrentPhase.WORK -> CurrentPhase.BREAK
        CurrentPhase.BREAK -> CurrentPhase.WORK
        CurrentPhase.NONE -> CurrentPhase.NONE
    }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Next ${nextPhase.name.lowercase()} in:", style = MaterialTheme.typography.bodyMedium)
        AnimatedStopwatch(pomodoroState.timerDigits)
    }
}

@Composable
fun AnimatedTimerDigit(
    targetDigit: Char,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle(
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.Thin,
        fontSize = 65.sp,
    ),
) {
    Box(
        modifier = modifier
            .height(style.fontSize.value.dp)
            .clip(RectangleShape),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(targetDigit) {
            Text(
                text = it.toString(),
                fontFamily = displayFontFamily,
                fontWeight = FontWeight.Thin,
                fontSize = style.fontSize,
                modifier = Modifier
            )
        }
    }
}

@Composable
@Preview
fun AnimatedTimerDigitPreview() {
    AnimatedTimerDigit(targetDigit = '5')
}

// Usage example with the complete timer
@Composable
fun AnimatedStopwatch(
    timerDigits: TimerDigits,
    modifier: Modifier = Modifier,
    labelStyle: TextStyle = MaterialTheme.typography.displaySmall,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    AnimatedTimerDigit(targetDigit = timerDigits.minone)
                    AnimatedTimerDigit(targetDigit = timerDigits.mintwo)
                }
                Text(
                    text = "min",
                    style = labelStyle,
                )
            }
            VerticalDivider(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .height(75.dp),
            )

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row {
                    // Seconds
                    AnimatedTimerDigit(
                        targetDigit = timerDigits.secone,
                    )
                    AnimatedTimerDigit(
                        targetDigit = timerDigits.sectwo,
                    )
                }
                Text(
                    text = "sec",
                    style = labelStyle,
                )
            }
        }
    }
}

@Composable
@Preview
fun AnimatedStopwatchPreview() {
    AnimatedStopwatch(TimerDigits('0', '0', '0', '0'))
}


@Composable
@Preview()
fun PomodoroClockPreview() {
    ZeppelinTheme {
        PomodoroClock(pomodoroState = PomodoroState().copy(currentPhase = CurrentPhase.WORK))
    }
}

@Composable
@Preview()
fun PomodoroClockNotStarted() {
    ZeppelinTheme {
        PomodoroClock(pomodoroState = PomodoroState().copy(currentPhase = CurrentPhase.NONE))
    }
}