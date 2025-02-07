package com.zeppelin.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeppelin.app.data.Character
import com.zeppelin.app.data.CharacterRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CharacterViewModel(
    private val characterRepository: CharacterRepository
) : ViewModel() {
    private val _characterState = MutableStateFlow<Character?>(null)
    val characterState: Flow<Character?> = _characterState

    private val _loadingState = MutableStateFlow(true)
    val loadingState: Flow<Boolean> = _loadingState

    private var _characterId: Int = 1
    init {
        getCharacter(_characterId)
    }

    private fun getCharacter(id: Int) {
        _loadingState.value = true
        viewModelScope.launch {
            _characterState.value = characterRepository.getCharacter(id)
            _loadingState.value = false
        }
    }

    fun nextCharacter() {
        _characterId++
        getCharacter(_characterId)
    }


}