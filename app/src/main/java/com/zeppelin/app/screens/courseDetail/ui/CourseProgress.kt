package com.zeppelin.app.screens.courseDetail.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zeppelin.app.R
import com.zeppelin.app.screens._common.ui.CardWithTitle
import com.zeppelin.app.screens._common.ui.LoadingText
import com.zeppelin.app.screens.courseDetail.data.CourseProgressUI
import com.zeppelin.app.ui.theme.ZeppelinTheme

@Composable
fun CourseProgress(modifier: Modifier = Modifier ,progress: CourseProgressUI, isLoading: Boolean = false) {
    CardWithTitle(modifier, stringResource(R.string.progress)) {
        Spacer(modifier = Modifier.height(8.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround
            ) {
                ProgressItem(stringResource(R.string.content), progress.contentProgress, isLoading)
                ProgressItem(stringResource(R.string.test), progress.testProgress, isLoading)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        CourseProgressBar(progress.contentPercentage, progress.testPercentage)
    }
}

@Composable
fun CourseProgressBar(contentPercentage: Float, testPercentage: Float) {
    val barBackground = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
    Row(
        modifier = Modifier
            .padding(vertical = 16.dp, horizontal = 24.dp)
            .fillMaxWidth(),
    )  {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(topStartPercent = 50, bottomStartPercent = 50))
                .background(barBackground)
                .height(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(contentPercentage)
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(topStartPercent = 50, bottomStartPercent = 50))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .height(20.dp)
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(topEndPercent = 50, bottomEndPercent = 50))
                .background(barBackground)
                .height(20.dp)
                .weight(1f)
        ){
            Box(
                modifier = Modifier
                    .fillMaxWidth(testPercentage)
                    .align(Alignment.TopStart)
                    .clip(RoundedCornerShape(topEndPercent = 50, bottomEndPercent = 50))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .height(20.dp)
            )
        }
    }
}

@Composable
fun ProgressItem(name: String, progress: String, isLoading: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(name, style = MaterialTheme.typography.bodyMedium)
        if (isLoading) LoadingText(length = 10, textStyle = MaterialTheme.typography.titleLarge)
        else Text(progress, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
@Preview
fun CourseProgressPreview() {
    ZeppelinTheme {
        CourseProgress(progress = CourseProgressUI("10/20", 0.3f, "5/10", 0.5f))
    }
}