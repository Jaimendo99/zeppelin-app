package com.zeppelin.app.rickandmorty.data

import io.ktor.client.call.body
import io.ktor.client.request.get

class DefaultCharacterRepo(private val networkClient: NetworkClient) : CharacterRepository {
    override suspend fun getCharacter(id: Int): Character {
        return networkClient.getCharacter(id)
    }
}

private suspend fun NetworkClient.getCharacter(id: Int): Character {
    return client.get("character/$id").body()
}
