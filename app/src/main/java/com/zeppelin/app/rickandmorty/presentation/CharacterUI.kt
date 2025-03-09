package com.zeppelin.app.rickandmorty.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.zeppelin.app.rickandmorty.data.Character
import com.zeppelin.app.rickandmorty.data.CharacterGender
import com.zeppelin.app.rickandmorty.data.CharacterStatus
import com.zeppelin.app.rickandmorty.data.Location
import com.zeppelin.app.rickandmorty.data.Origin


@Composable
fun CharacterUI(character: Character, isLoading: Boolean = false) {
    ElevatedCard (
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
    ) {
        if (isLoading) {
            Box(contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.Gray)
            ){
                CircularProgressIndicator()
            }
        } else {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically // Center items vertically in the Row
        ) {
            // Character Image (Left side)
            CharacterImage(
                imageUrl = character.image,
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = character.name,
                        style = MaterialTheme.typography.titleLarge, // Use a larger heading style
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f)) // Push status to the right
                    CharacterStatusUI(status = character.status)
                }

                Spacer(modifier = Modifier.height(4.dp)) // Small spacing between lines

                Text(
                    text = "${character.species} ${if (character.type.isNotEmpty()) "(${character.type})" else ""}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Gender: ${character.gender.name}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Origin: ${character.origin.name}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Last known location: ${character.location.name}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
    }
}

@Composable
fun CharacterImage(imageUrl: String, modifier: Modifier) {

    Surface(
        modifier = modifier,
        shape = CircleShape,
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(Color.Gray)
        ) {
            // Load image from URL
            AsyncImage(
                model= imageUrl,
                contentDescription = "Character Image",
            )

        }
    }

}


@Composable
fun CharacterStatusUI(modifier: Modifier = Modifier, status: CharacterStatus) {
    val color = when (status) {
        CharacterStatus.Alive -> Color(0xFF00AF40)
        CharacterStatus.Dead -> Color(0xFFAF3312)
        CharacterStatus.unknown -> Color(0xFF444444)
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color)
            .padding(horizontal = 8.dp, vertical = 1.dp)
    ) {
        Text(
            text = status.name,
            color = Color.White,
            fontSize = 10.sp
        )
    }
}

@Composable
@Preview
fun CharacterStatusPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CharacterStatusUI(status = CharacterStatus.Alive)
        CharacterStatusUI(status = CharacterStatus.Dead)
        CharacterStatusUI(status = CharacterStatus.unknown)

    }
}

@Composable
@Preview
fun CharacterUIPreview() {
    CharacterUI(
        Character(
            1,
            "Rick Sanchez",
            CharacterStatus.Alive,
            "Human",
            "Something",
            CharacterGender.Male,
            Origin("Earth (C-137)", "https://rickandmortyapi.com/api/location/1"),
            Location(
                "Earth (Replacement Dimension)",
                "https://rickandmortyapi.com/api/location/20"
            ),
            "https://rickandmortyapi.com/api/character/avatar/1.jpeg",
            listOf("https://rickandmortyapi.com/api/episode/1"),
            "https://rickandmortyapi.com/api/character/1",
            "2017-11-04T18:48:46.250Z"
        )
   ,true )

}