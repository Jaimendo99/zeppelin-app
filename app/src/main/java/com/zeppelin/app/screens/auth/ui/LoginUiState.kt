package com.zeppelin.app.screens.auth.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zeppelin.app.R
import com.zeppelin.app.screens._common.ui.SpinningCircleWave
import com.zeppelin.app.screens.auth.data.ErrorResponseBody
import com.zeppelin.app.screens.auth.data.SignInResponse
import com.zeppelin.app.screens.auth.domain.NetworkResult

@Composable
fun LoginState(
    modifier: Modifier = Modifier,
    networkResult: NetworkResult<SignInResponse, ErrorResponseBody>
) {
    val scrollState = rememberScrollState()
    AnimatedContent(
        modifier = modifier
            .verticalScroll(scrollState)
            .fillMaxSize(),
        targetState = networkResult
    ) { result ->
        Box(
            contentAlignment = Alignment.Center
        ) {
            when (result) {
                is NetworkResult.Error -> LoginStateError(result)
                is NetworkResult.Success -> LoginStateSuccess(result)
                NetworkResult.Idle -> LoginStateIdle()
                NetworkResult.Loading -> LoginStateLoading()
            }
        }
    }
}




@Composable
fun LoginStateSuccess(success: NetworkResult.Success<SignInResponse>) {
    Column {
        Text(text = "Login Success")
        LoginTitleDrawing()
    }
}

@Composable
fun LoginStateLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 30.dp),
        contentAlignment = Alignment.Center
    ) {
        SpinningCircleWave()
        SpinningCircleWave(
            Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.secondaryContainer,
            direction = -1
        )
    }
}


@Composable
fun LoginStateIdle(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LoginTitleDrawing()
        Spacer(Modifier.padding(8.dp))
        Text(
            stringResource(R.string.welcome_to_focused),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )
        Text(
            stringResource(R.string.please_log_in_with_your_student_email),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LoginStateError(error: NetworkResult.Error<ErrorResponseBody>) {
    val errors = listOf(
        "Enter password.",
        "Identifier is invalid.",
        "Couldn't find your account.",
        "Password is incorrect. Try again, or use another method.",
    )
    SelectionContainer {
        when (error.errorBody?.errors?.get(0)?.message) {
            errors[0] -> {
                Text("Password is required.")
            }

            errors[1] -> {
                Text("Email is invalid.")
            }

            errors[2] -> {
                Text("Couldn't find your account.")
            }

            errors[3] -> {
                Text("Password is incorrect.")
            }

            else -> {
                Text("An error occurred.")
            }

        }
    }
}

