package com.zeppelin.app.screens.courseSession.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.zeppelin.app.ui.theme.ZeppelinTheme
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.Line


@Composable
fun SessionCharts() {
    var lineValues by remember { mutableStateOf(listOf(1.0, 2.0, 3.0, 4.0, 5.0)) }

    LaunchedEffect(Unit) {
        for (i in 1..10) {
            kotlinx.coroutines.delay(5000)
            lineValues += (i).toDouble()
        }
    }
    Column {
        LineChart(
            data =
                listOf(
                    Line(
                        label = "1",
                        values = lineValues,
                        color = Brush.linearGradient(listOf(Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFFFF9800))),
                    )
                )
        )
    }
}


@Preview(
)
@Composable
fun SessionChartsPreview() {
    ZeppelinTheme {
        SessionCharts()
    }
}