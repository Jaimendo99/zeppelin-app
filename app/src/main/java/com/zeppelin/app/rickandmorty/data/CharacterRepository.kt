package com.zeppelin.app.rickandmorty.data

fun interface CharacterRepository {
    suspend fun getCharacter(id: Int) : Character
}