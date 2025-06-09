package com.zeppelin.app.screens._common.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.zeppelin.app.screens.nav.Screens
import com.zeppelin.app.screens.watchLink.data.WatchLinkRepository
import kotlinx.coroutines.launch

class ScaffoldViewModel(
     watchLinkRepository: WatchLinkRepository

): ViewModel() {

    val isWatchLinked = watchLinkRepository.isConnectedToWatch

    fun onParingClicked(navController: NavHostController) {
        viewModelScope.launch {
            navController.navigate(Screens.WatchLink.route)
        }
    }
}