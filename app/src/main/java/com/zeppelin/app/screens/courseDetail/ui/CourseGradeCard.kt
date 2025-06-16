package com.zeppelin.app.screens.courseDetail.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zeppelin.app.R
import com.zeppelin.app.screens._common.ui.CardWithTitle
import com.zeppelin.app.screens._common.ui.LoadingText
import com.zeppelin.app.screens._common.ui.TextWithLoader
import com.zeppelin.app.screens.courseDetail.data.GradeUI
import com.zeppelin.app.screens.courseDetail.data.QuizGradesUi
import com.zeppelin.app.screens.courseDetail.data.QuizGradesUiState
import com.zeppelin.app.ui.theme.ZeppelinTheme


@Composable
fun GradesCard(
    modifier: Modifier = Modifier,
    quizState: QuizGradesUiState,
    onOpenDialog: (QuizGradesUi) -> Unit,
    onDismissDialog: () -> Unit
) {
    AnimatedContent(quizState.isLoading,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        ) { isLoading ->
        CardWithTitle(modifier, stringResource(R.string.grades)) {
            if (isLoading) {
                for (i in 1..2) {
                    QuizGradesItem(
                        grade = QuizGradesUi("", "", "", "", "", false, "", ""),
                        modifier = Modifier
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                        isLoading = true,
                    )
                }
            } else {
                quizState.quizGrades.forEach {
                    QuizGradesItem(
                        modifier = Modifier
                            .clickable { onOpenDialog(it) }
                            .padding(vertical = 8.dp),
                        grade = it,
                        isLoading = false,
                    )
                    if (it != quizState.quizGrades.last()) HorizontalDivider()
                    if (quizState.showDetails) {
                        GradeDetailDialog(
                            grade = it,
                            onDismiss = {
                                onDismissDialog()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GradeDetailDialog(
    modifier: Modifier = Modifier,
    grade: QuizGradesUi,
    onDismiss: () -> Unit
) {
    AlertDialog(
        modifier = modifier,
        title = {
            Text(grade.title)
        },
        text = {
            Text(grade.description)
        },
        dismissButton = {
            TextButton(onDismiss) {
                Text("Dismiss")
            }
        },
        onDismissRequest = { onDismiss() },
        confirmButton = { }
    )
}

@Composable
@Preview
fun DialogPreview() {
    ZeppelinTheme {
        GradeDetailDialog(
            grade = QuizGradesUi(
                contentId = "1",
                startTime = "10-10-2025",
                endTime = "10-10-2025",
                title = "Quiz 1",
                grade = "10/10",
                finalGrade = true,
                reviewedAt = "10-10-2025",
                description = "This is a sample description for the quiz grade item."
            ),
            onDismiss = {}
        )
    }
}

@Composable
fun GradeCardItem(modifier: Modifier = Modifier, grade: GradeUI, isLoading: Boolean = false) {
    Row(modifier = modifier) {
        Column(modifier = Modifier.weight(1f)) {
            if (!isLoading) {
                Text(text = grade.dateGraded, style = MaterialTheme.typography.labelSmall)
                Text(text = grade.gradeName, style = MaterialTheme.typography.titleMedium)
            } else {
                LoadingText(
                    length = 20,
                    textStyle = MaterialTheme.typography.labelSmall,
                )
                LoadingText(
                    length = 20,
                    textStyle = MaterialTheme.typography.titleMedium,
                )
            }

        }
        if (!isLoading) {
            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = grade.grade,
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            LoadingText(
                length = 10,
                textStyle = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}


@Composable
fun QuizGradesItem(modifier: Modifier = Modifier, grade: QuizGradesUi, isLoading: Boolean) {
    val reviewed = if (grade.finalGrade) painterResource(R.drawable.rounded_check_circle_24)
    else painterResource(R.drawable.rounded_schedule_24)
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
    ) {
        if (!isLoading)
            Icon(reviewed, contentDescription = null, modifier = Modifier.size(16.dp),tint=MaterialTheme.colorScheme.tertiary)
        else LoadingText(8, MaterialTheme.typography.bodyMedium)
        Column(modifier = Modifier.weight(1f)) {
            TextWithLoader(
                grade.reviewedAt,
                25,
                MaterialTheme.typography.labelSmall,
                isLoading = isLoading
            )
            TextWithLoader(
                grade.title,
                15,
                MaterialTheme.typography.titleMedium,
                isLoading = isLoading
            )
        }
        TextWithLoader(grade.grade, 10, MaterialTheme.typography.bodyMedium, isLoading = isLoading)
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null)
    }
}

@Composable
@Preview(showBackground = true)
fun QuizGradesItemPreview() {
    ZeppelinTheme {
        QuizGradesItem(
            grade = QuizGradesUi(
                contentId = "aisjda",
                startTime = "10-10-2025",
                endTime = "10-10-2025",
                title = "Quiz 1",
                grade = "10/10",
                finalGrade = true,
                reviewedAt = "10-10-2025",
                description = "This is a sample description for the quiz grade item."
            ),
            isLoading = true,
        )
    }
}

@Composable
@Preview(showBackground = true)
fun GradesCardPreview() {
    GradesCard(
        onOpenDialog = {},
        onDismissDialog = {},
        quizState = QuizGradesUiState(
            isLoading = false,
            quizGrades = listOf(
                QuizGradesUi(
                    contentId = "1",
                    startTime = "10-10-2025",
                    endTime = "10-10-2025",
                    title = "Quiz 1",
                    grade = "10/10",
                    finalGrade = true,
                    reviewedAt = "10-10-2025",
                    description = "This is a sample description for the quiz grade item."
                ),
                QuizGradesUi(
                    contentId = "2",
                    startTime = "11-11-2025",
                    endTime = "11-11-2025",
                    title = "Quiz 2",
                    grade = "8/10",
                    finalGrade = false,
                    reviewedAt = "11-11-2025",
                    description = "This is another sample description for the quiz grade item."
                )
            )
        )
    )
}

