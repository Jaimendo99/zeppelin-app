package com.zeppelin.app.screens._common.ui

import android.content.Context
import android.os.VibrationEffect
import android.os.VibratorManager
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zeppelin.app.ui.theme.ZeppelinTheme


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ButtonWithLoader(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongPress: (() -> Unit)? = null,
    isLoading: Boolean = false,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val vibrator: VibratorManager by lazy {
        context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
    }
    val enabled = !isLoading

    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        shape = ButtonDefaults.shape,
        color = colors.containerColor,
        contentColor = colors.contentColor,
        modifier = modifier
            .clip(ButtonDefaults.shape)
            .combinedClickable(
                enabled = enabled,
                onClick = {
                    Log.d("ButtonWithLoader", "onClick")
                    onClick()
                },
                onLongClick = {
                    if (onLongPress != null) {
                        vibrator.defaultVibrator.vibrate(
                            VibrationEffect.createOneShot(
                                100,
                                VibrationEffect.EFFECT_HEAVY_CLICK
                            )
                        )
                        Log.d("ButtonWithLoader", "onLongPress")
                        onLongPress()
                    }
                },
                interactionSource = interactionSource,
                indication = LocalIndication.current
            )
            .semantics { role = Role.Button }
    ) {
        ProvideTextStyle(value = MaterialTheme.typography.labelLarge) {
            Row(
                modifier = Modifier
                    .defaultMinSize(
                        minWidth = ButtonDefaults.MinWidth,
                        minHeight = ButtonDefaults.MinHeight
                    )
                    .padding(ButtonDefaults.ContentPadding),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedContent(targetState = isLoading) { loading ->
                    if (loading) {
                        SpinningCircleWave(
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.secondaryContainer
                        )
                    } else {
                        content()
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = false)
fun ButtonWithLoadingIndicatorPreview() {
    ZeppelinTheme {
        ButtonWithLoader(
            onClick = {},
            isLoading = true
        ) {
            Text("Button")
        }
    }
}