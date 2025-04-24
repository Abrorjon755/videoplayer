package dev.abrorjon755.videoplayer.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.abrorjon755.videoplayer.screens.player.PlayerScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val viewModel = viewModel<HomeViewModel>()
    val uiState = viewModel.uiState.collectAsState()
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black),
    ) { innerPadding ->
        PlayerScreen(
            url = "https://cdn.uzd.udevs.io/uzdigital/videos/76cb1319e234a59764658c0c9d566d1e/master.m3u8",
            modifier = Modifier.padding(innerPadding)
        )
    }
}