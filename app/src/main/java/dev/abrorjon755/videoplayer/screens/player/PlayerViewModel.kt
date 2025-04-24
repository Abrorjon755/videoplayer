package dev.abrorjon755.videoplayer.screens.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlayerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PlayerUiState())

    var uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()
        private set

    private var debouncer: Job? = null

    fun cancelDebouncerAndStart(playing: Boolean) {
        debouncer?.cancel()
        debouncer = viewModelScope.launch {
            delay(5000L)
            changeController(playing)
        }
    }

    fun changePlaybackPosition(newPosition: Long) {
        _uiState.update {
            it.copy(
                playbackPosition = newPosition,
            )
        }
    }

    fun changePlaying(newPlaying: Boolean) {
        _uiState.update {
            it.copy(
                playing = newPlaying,
            )
        }
    }

    fun changeSeconds(newSeconds: Int) {
        _uiState.update {
            it.copy(
                seconds = newSeconds,
            )
        }
    }

    fun changeCurrentSeconds(newCurrentSeconds: Int) {
        _uiState.update {
            it.copy(
                currentSeconds = newCurrentSeconds,
            )
        }
    }

    fun changeController(newController: Boolean) {
        _uiState.update {
            it.copy(
                showController = newController
            )
        }
    }

    fun hideController(playing: Boolean) {
        changeController(!uiState.value.showController)
        cancelDebouncerAndStart(playing)
    }

    fun changeIsLiked(newIsLiked: Boolean) {
        _uiState.update {
            it.copy(
                isLiked = newIsLiked,
            )
        }
        cancelDebouncerAndStart(false)
    }

    fun changeIsLocked(newIsLocked: Boolean) {
        _uiState.update {
            it.copy(
                isLocked = newIsLocked,
            )
        }
        cancelDebouncerAndStart(false)
    }

    fun changeOnChangingSpeed() {
        _uiState.update {
            it.copy(
                onChangingSpeed = !it.onChangingSpeed
            )
        }
        cancelDebouncerAndStart(false)
    }

    fun changeSpeed(newSpeed: Double) {
        _uiState.update {
            it.copy(
                currentSpeed = newSpeed
            )
        }
        changeOnChangingSpeed()
    }

    fun changeOnDrag(onDrag: Boolean) {
        _uiState.update {
            it.copy(
                onDrag = onDrag,
                dragValue = if (onDrag) it.dragValue else 0
            )
        }
    }

    fun changeDragValue(dragValue: Int) {
        _uiState.update {
            it.copy(
                dragValue = it.dragValue + dragValue
            )
        }
    }
}

data class PlayerUiState(
    val url: String = "",
    val playbackPosition: Long = 0L,
    val playing: Boolean = false,
    val seconds: Int = 0,
    val currentSeconds: Int = 0,
    val showController: Boolean = true,
    val isLiked: Boolean = false,
    val isLocked: Boolean = false,
    val currentSpeed: Double = 1.0,
    val onChangingSpeed: Boolean = false,
    val onDrag: Boolean = false,
    val dragValue: Int = 0,
)