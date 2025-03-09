package com.zeppelin.app.screens.profile.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
fun ProfileScreen(modifier: Modifier = Modifier, profileViewModel: ProfileViewModel) {
    Box(modifier = modifier.fillMaxSize()) {
        Text("Profile Screen")
    }
}