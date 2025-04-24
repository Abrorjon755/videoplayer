package dev.abrorjon755.videoplayer

import android.app.PictureInPictureParams
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.abrorjon755.videoplayer.screens.home.HomeScreen
import dev.abrorjon755.videoplayer.ui.theme.VideoPlayerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VideoPlayerTheme {
                HomeScreen()
            }
        }
    }

    override fun onUserLeaveHint() {
        enterPiPMode(this)
        super.onUserLeaveHint()
    }
}

fun enterPiPMode(activity: android.app.Activity?) {
    activity?.let {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = PictureInPictureParams.Builder()
                .build()
            it.enterPictureInPictureMode(params)
        }
    }
}