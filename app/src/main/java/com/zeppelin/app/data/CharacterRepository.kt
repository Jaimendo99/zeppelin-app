package com.zeppelin.app.data

fun interface CharacterRepository {
    suspend fun getCharacter(id: Int) : Character
}