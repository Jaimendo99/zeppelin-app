package com.zeppelin.app.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer

@Serializable
data class Character(
    val id :Int,
    val name :String,
    val status :CharacterStatus,
    val species :String,
    val type :String,
    val gender: CharacterGender,
    val origin: Origin,
    val location: Location,
    val image: String,
    val episode: List<String>,
    val url: String,
    val created: String
)

@Serializable
enum class CharacterStatus {
    Alive,
    Dead,
    unknown
}

@Serializable
enum class CharacterGender{
   Female,
    Male,
    Genderless,
    unknown
}

@Serializable
data class Origin (
    val name :String,
    val url :String
)

@Serializable
data class Location (
    val name :String,
    val url :String
)