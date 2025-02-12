package com.zeppelin.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zeppelin.app.presentation.CharacterUI
import com.zeppelin.app.presentation.CharacterViewModel
import com.zeppelin.app.ui.theme.ZeppelinTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZeppelinTheme {
                val viewModel = koinViewModel<CharacterViewModel>()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val characterState = viewModel.characterState.collectAsState(null)
                    val loadingState = viewModel.loadingState.collectAsState(false)

                    Column(modifier = Modifier.padding(innerPadding)) {
                        CharacterUI(characterState.value ?: return@Column, loadingState.value)

                        Row(modifier = Modifier.padding(16.dp)) {
                            Button(
                                onClick = { viewModel.previousCharacter() },
                                enabled = !loadingState.value
                            ) { Text("Previous") }
                            Spacer(modifier = Modifier.weight(1f))
                            Button(
                                onClick = { viewModel.nextCharacter() },
                                enabled = !loadingState.value
                            ) { Text("Next") }
                        }
                    }
                }
            }
        }
    }
}