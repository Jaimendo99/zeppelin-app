package com.zeppelin.app.screens.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.zeppelin.app.R
import com.zeppelin.app.screens.auth.data.ErrorResponseBody
import com.zeppelin.app.screens.auth.data.SignInResponse
import com.zeppelin.app.screens.auth.domain.NetworkResult

@Composable
fun LoginForm(
    modifier: Modifier = Modifier,
    loginScreenData: NetworkResult<SignInResponse, ErrorResponseBody>,
    onLogin: (String, String) -> Unit
) {
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    Column(
        modifier = modifier
            .defaultMinSize(minWidth = 280.dp)
    ) {
        EmailField(
            email.value,
            { email.value = it },
            loginScreenData is NetworkResult.Error,
            loginScreenData is NetworkResult.Loading
        )
        Spacer(modifier = Modifier.height(8.dp))
        PasswordField(
            password.value,
            { password.value = it },
            loginScreenData is NetworkResult.Error,
            loginScreenData is NetworkResult.Loading
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            LoginButton(
                loginScreenData is NetworkResult.Error,
                loginScreenData is NetworkResult.Loading
            ) {
                onLogin(email.value, password.value)
            }
        }
    }
}


@Composable
fun LoginButton(isError: Boolean, isLoading: Boolean = false, onLogin: () -> Unit) {
    val normalButtonColor = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    )
    Button(
        enabled = !isLoading,
        modifier = Modifier.fillMaxWidth(),
        onClick = onLogin,
        colors = if (isError)
            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        else
            normalButtonColor
    ) {
        Text(text = if (isError) stringResource(R.string.loginButtonError) else stringResource(R.string.loginButton))
    }
}


@Composable
fun PasswordField(
    password: String,
    onChanged: (String) -> Unit,
    isError: Boolean = false,
    isLoading: Boolean = false
) {
    OutlinedTextField(
        enabled = !isLoading,
        modifier = Modifier.fillMaxWidth(),
        value = password, onValueChange = onChanged,
        leadingIcon = {
            Icon(
                Icons.Filled.Lock,
                contentDescription = "Password",
                tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondaryContainer
            )
        },
        placeholder = { Text("") },
        label = { Text("Password") },
        visualTransformation = PasswordVisualTransformation(),
        singleLine = true,
        isError = isError,
    )
}

@Composable
fun EmailField(
    email: String,
    onChanged: (String) -> Unit,
    isError: Boolean = false,
    isLoading: Boolean = false
) {
    OutlinedTextField(
        enabled = !isLoading,
        modifier = Modifier.fillMaxWidth(),
        value = email, onValueChange = onChanged,
        leadingIcon = {
            Icon(
                Icons.Filled.Email,
                contentDescription = "Email",
                tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondaryContainer
            )
        },
        label = { Text("Email") },
        singleLine = true,
        isError = isError,
        suffix = {
            Text(
                text = "@uda.edu.ec",
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next
        )
    )
}

