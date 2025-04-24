package dev.abrorjon755.videoplayer.screens.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())

    var uiState = _uiState
        private set
}

data class HomeUiState(
    val url: String = "",
)