package com.zeppelin.app.data

interface CharacterRepository {
    suspend fun getCharacter(id: Int) : Character
}