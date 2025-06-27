package com.zeppelin.app.screens.courseDetail.ui

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zeppelin.app.R
import com.zeppelin.app.ui.theme.ZeppelinTheme

// WATCH OFF
// SCREEN UNPINNED
// WATCH NOT CONNECTED
// WEAK SIGNAL

@Composable
fun SessionStateView(
    isWatchOff: Boolean,
    isWatchConnected: Boolean,
    isScreenPinned: Boolean,
    isSignalWeak: Boolean,
    onPinClick: () -> Unit,
    modifier: Modifier = Modifier,
    correctContent : @Composable () -> Unit = { Text("All systems operational") }

) {
    if (isWatchOff) { WatchOffStateView(modifier)
    } else if (!isWatchConnected) { WatchNotConnectedStateView(modifier)
    } else if (!isScreenPinned) { UnpinnedStateView { onPinClick() }
    } else if (isSignalWeak) { WeakSignalStateView(modifier)
    } else { correctContent()
    }
}

@Composable
fun WeakSignalStateView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier.size(200.dp),
            painter = painterResource(R.drawable.get_closer),
            contentDescription = "Weak Signal",
        )
        Column(
            modifier = Modifier
                .width(230.dp)
                .offset(y = (-15).dp)
        ) {
            Text(
                "La señal con tu reloj es débil",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                )
            )
            Text(
                "Por favor, acércate al teléfono para mejorar la conexión.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun UnpinnedStateView(modifier: Modifier = Modifier, pinScreen: () -> Unit) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .width(170.dp)
        ) {
            Text( "La aplicación no está pineada",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
            )

            Text( "Por favor, pinéala para continuar con la sesión.",
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(onClick = {pinScreen()}) {
                Text(text = "Pinear pantalla",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
        Image(
            painterResource(R.drawable.unpin_app), contentDescription = "Connect Watch",
            modifier = Modifier
                .size(200.dp)
                .offset(x = 25.dp)
        )
    }
}

@Composable
fun WatchNotConnectedStateView(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .width(170.dp)
        ) {


            Text( "El reloj está desconectado",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
        )

            Text( "Por favor, conéctalo para continuar con la sesión.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Image(
            painterResource(R.drawable.connect_watch), contentDescription = "Connect Watch",
            modifier = Modifier
                .size(200.dp)
                .offset(x = 25.dp)
        )
    }
}

@Composable
fun WatchOffStateView(modifier : Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier.size(200.dp),
            painter = painterResource(R.drawable.wear_watch),
            contentDescription = "Weak Signal",
        )
        Column(
            modifier = Modifier
                .width(230.dp)
        ) {
            Text(
                "El reloj no está puesto",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                )
            )
            Text(
                "Por favor, asegúrate de que el reloj está en tu muñeca bien ajustado.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


@Composable
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
fun WeakSignalStateViewPreviewLight() {
    ZeppelinTheme {
        WeakSignalStateView()
    }
}


@Composable
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
fun UnpinnedStateViewPreview() {
    ZeppelinTheme {
        UnpinnedStateView { /* No-op */ }
    }
}

@Composable
@Preview(showBackground = true)
fun WatchNotConnectedStateViewPreview() {
    ZeppelinTheme {
        WatchNotConnectedStateView()
    }
}

@Composable
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
fun WatchOffStateViewPreview() {
    WatchOffStateView()
}