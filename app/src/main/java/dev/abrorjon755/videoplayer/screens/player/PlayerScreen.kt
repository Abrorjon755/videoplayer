@file:Suppress("DEPRECATION")

package dev.abrorjon755.videoplayer.screens.player

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.media.AudioManager
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import dev.abrorjon755.videoplayer.composables.MyButton
import dev.abrorjon755.videoplayer.composables.MyClickableText
import dev.abrorjon755.videoplayer.composables.TimeCalculator
import dev.abrorjon755.videoplayer.enterPiPMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@ExperimentalMaterial3Api
@SuppressLint("SourceLockedOrientationActivity")
@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    url: String,
    modifier: Modifier = Modifier,
) {
    val viewModel = viewModel<PlayerViewModel>()
    val uiState = viewModel.uiState.collectAsState().value
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val activity = context.findActivity()
    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(context))
            .build().apply {
                val mediaItem = MediaItem.Builder()
                    .setUri(Uri.parse(url))
                    .setMimeType(MimeTypes.APPLICATION_M3U8)
                    .build()

                setMediaItem(mediaItem)
                prepare()
                seekTo(uiState.playbackPosition.coerceAtLeast(0L))
                videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
                playWhenReady = true
            }
    }
    val coroutineScope = rememberCoroutineScope()
    val shareLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {}
    var isDropdownShowed by remember { mutableStateOf(false) }
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val trackSelector = exoPlayer.trackSelector as DefaultTrackSelector
    val qualityOptions = listOf("Auto", "360p", "480p", "720p", "1080p", "4k")
    var selectedQuality by remember { mutableStateOf("Auto") }
    val parameters = trackSelector.buildUponParameters()


    activity?.window?.let {
        it.statusBarColor = Color.Black.toArgb()
        it.navigationBarColor = Color.Black.toArgb()
    }

    // When Landscape change Full Screen
    LaunchedEffect(!uiState.showController) {
        activity?.let {
            val controller = WindowCompat.getInsetsController(it.window, it.window.decorView)
            controller.apply {
                hide(WindowInsetsCompat.Type.systemBars())
                systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    LaunchedEffect(exoPlayer) {
        while (true) {
            viewModel.changeCurrentSeconds(exoPlayer.currentPosition.toInt())
            delay(100)
        }
    }

    exoPlayer.addListener(object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            super.onPlaybackStateChanged(state)
            if (state == Player.STATE_ENDED) {
                exoPlayer.seekTo(0L)
                exoPlayer.playWhenReady = false
            } else if (state == Player.STATE_READY) {
                viewModel.changeSeconds(exoPlayer.duration.toInt())
                viewModel.hideController(!exoPlayer.isPlaying)
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            viewModel.changePlaying(isPlaying)
            viewModel.changeCurrentSeconds(exoPlayer.currentPosition.toInt())
            coroutineScope.launch {
                delay(5000L)
                viewModel.changeController(!exoPlayer.isPlaying)
            }
        }
    })

    DisposableEffect(exoPlayer) {
        onDispose {
            viewModel.changePlaybackPosition(exoPlayer.currentPosition)
            exoPlayer.release()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.changePlaybackPosition(exoPlayer.currentPosition)
                exoPlayer.playWhenReady = false
            } else if (event == Lifecycle.Event.ON_START) {
                exoPlayer.seekTo(uiState.playbackPosition)
                exoPlayer.playWhenReady = true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                useController = false
            }
        },
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.Black),
    )

    if (!uiState.isLocked) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                            viewModel.hideController(!exoPlayer.isPlaying)
                        },
                        onTap = { viewModel.hideController(!exoPlayer.isPlaying) }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            viewModel.changeOnDrag(true)
                            viewModel.changeController(true)
                        },
                        onDragEnd = {
                            viewModel.changeOnDrag(false)
                            viewModel.hideController(false)
                        },
                        onDragCancel = {
                            viewModel.changeOnDrag(false)
                            viewModel.hideController(false)
                        },
                        onDrag = { _, dragAmount ->
                            if (dragAmount.y > 15) {
                                audioManager.setStreamVolume(
                                    AudioManager.STREAM_MUSIC,
                                    audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) - 1,
                                    AudioManager.FLAG_SHOW_UI
                                )
                            } else if (dragAmount.y < -15) {
                                audioManager.setStreamVolume(
                                    AudioManager.STREAM_MUSIC,
                                    audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) + 1,
                                    AudioManager.FLAG_SHOW_UI
                                )
                            } else if (dragAmount.x > 1) {
                                exoPlayer.seekTo(exoPlayer.currentPosition + 1000)
                                viewModel.changeDragValue(1)
                            } else if (dragAmount.x < -1) {
                                exoPlayer.seekTo(exoPlayer.currentPosition - 1000)
                                viewModel.changeDragValue(-1)
                            }

                        }
                    )
                }
                .padding(horizontal = 25.dp),
        ) {
            BackHandler { exoPlayer.release() }
            Spacer(modifier = Modifier.weight(1f))
            AnimatedVisibility(visible = uiState.showController) {
                Column {
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = exoPlayer.mediaMetadata.displayTitle.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        MyButton(
                            size = 40.dp,
                            func = {
                                shareLauncher.launch(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                                    putExtra(Intent.EXTRA_TEXT, url)
                                    type = "text/plain"
                                }, null))
                            },
                            icon = Icons.Rounded.Share,
                            hideRipple = false,
                            modifier = Modifier.clip(RoundedCornerShape(40.dp)),
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box {
                            MyButton(
                                size = 40.dp,
                                func = {
                                    isDropdownShowed = !isDropdownShowed
                                    viewModel.cancelDebouncerAndStart(true)
                                },
                                icon = Icons.Rounded.MoreVert,
                                hideRipple = false,
                                modifier = Modifier.clip(RoundedCornerShape(40.dp)),
                            )
                            QualitySelector(
                                onQualitySelected = {
                                    selectedQuality = it
                                    when (it) {
                                        "360p" -> parameters.setMaxVideoBitrate(1500000)
                                            .setMinVideoBitrate(500000)

                                        "480p" -> parameters.setMaxVideoBitrate(2500000)
                                            .setMinVideoBitrate(1000000)

                                        "720p" -> parameters.setMaxVideoBitrate(5000000)
                                            .setMinVideoBitrate(2500000)

                                        "1080p" -> parameters.setMaxVideoBitrate(8000000)
                                            .setMinVideoBitrate(5000000)

                                        "4k" -> parameters.setMaxVideoBitrate(50000000)
                                            .setMinVideoBitrate(15000000)

                                        "Auto" -> parameters.clearOverrides()
                                    }
                                    trackSelector.setParameters(parameters)
                                    isDropdownShowed = false
                                },
                                expanded = isDropdownShowed,
                                qualityOptions = qualityOptions,
                                closeDropdown = { isDropdownShowed = false },
                                isSelected = selectedQuality
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                    ) {
                        TimeCalculator(uiState.currentSeconds)
                        TimeCalculator(uiState.seconds)
                    }
                    Slider(
                        value = uiState.currentSeconds.toFloat(),
                        onValueChange = {
                            viewModel.changeOnDrag(true)
                            viewModel.changeDragValue(((it - exoPlayer.currentPosition) / 1000).toInt())
                            exoPlayer.seekTo(it.toLong())
                        },
                        valueRange = if (uiState.seconds.toFloat() > 0f) 0f..uiState.seconds.toFloat()
                        else 0f..1f,
                        enabled = uiState.seconds.toFloat() > 0f,
                        onValueChangeFinished = {
                            viewModel.changeOnDrag(false)
                            exoPlayer.playWhenReady = true
                            viewModel.changeController(false)
                        },
                        track = {
                            SliderDefaults.Track(
                                sliderState = remember {
                                    SliderState(
                                        value = uiState.currentSeconds.toFloat(),
                                        valueRange = if (uiState.seconds.toFloat() > 0f) 0f..uiState.seconds.toFloat()
                                        else 0f..1f,
                                    )
                                },
                                enabled = true,
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(20.dp),
                                    )
                                    .height(4.dp)
                                    .fillMaxWidth()
                            )
                        },
                        thumb = {
                            SliderDefaults.Thumb(
                                interactionSource = remember { MutableInteractionSource() },
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    AnimatedVisibility(!uiState.onChangingSpeed) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceAround,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            MyButton(
                                size = 40.dp,
                                func = { viewModel.changeIsLiked(!uiState.isLiked) },
                                icon = if (uiState.isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            )
                            MyButton(
                                size = 40.dp,
                                func = { enterPiPMode(activity) },
                                icon = Icons.AutoMirrored.Rounded.ExitToApp,
                            )
                            MyClickableText(
                                text = "${uiState.currentSpeed}x",
                                onTap = { viewModel.changeOnChangingSpeed() }
                            )
                            MyButton(
                                size = 40.dp,
                                func = {
                                    activity?.requestedOrientation =
                                        when (configuration.orientation) {
                                            Configuration.ORIENTATION_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                            Configuration.ORIENTATION_PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                            else -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                                        }
                                },
                                icon = Icons.Rounded.Refresh,
                            )
                            MyButton(
                                size = 40.dp,
                                func = { viewModel.changeIsLocked(true) },
                                icon = Icons.Rounded.Lock
                            )
                        }
                    }
                    AnimatedVisibility(uiState.onChangingSpeed) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceAround,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            MyClickableText(
                                text = "0.5x",
                                onTap = {
                                    viewModel.changeSpeed(0.5)
                                    exoPlayer.setPlaybackSpeed(0.5.toFloat())
                                }
                            )
                            MyClickableText(
                                text = "0.75x",
                                onTap = {
                                    viewModel.changeSpeed(0.75)
                                    exoPlayer.setPlaybackSpeed(0.75.toFloat())
                                }
                            )
                            MyClickableText(
                                text = "1.0x",
                                onTap = {
                                    viewModel.changeSpeed(1.0)
                                    exoPlayer.setPlaybackSpeed(1.0.toFloat())
                                }
                            )
                            MyClickableText(
                                text = "1.25x",
                                onTap = {
                                    viewModel.changeSpeed(1.25)
                                    exoPlayer.setPlaybackSpeed(1.25.toFloat())
                                }
                            )
                            MyClickableText(
                                text = "1.5x",
                                onTap = {
                                    viewModel.changeSpeed(1.5)
                                    exoPlayer.setPlaybackSpeed(1.5.toFloat())
                                }
                            )
                            MyClickableText(
                                text = "1.75x",
                                onTap = {
                                    viewModel.changeSpeed(1.75)
                                    exoPlayer.setPlaybackSpeed(1.75.toFloat())
                                }
                            )
                            MyClickableText(
                                text = "2.0x",
                                onTap = {
                                    viewModel.changeSpeed(2.0)
                                    exoPlayer.setPlaybackSpeed(2.0.toFloat())
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(30.dp))
                }
                Column {
                    Spacer(modifier = Modifier.weight(1f))
                    AnimatedVisibility(!uiState.onDrag) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            MyButton(
                                size = 50.dp,
                                func = { exoPlayer.seekTo(exoPlayer.currentPosition - 1000) },
                                icon = Icons.AutoMirrored.Rounded.KeyboardArrowLeft
                            )
                            Spacer(modifier = Modifier.width(20.dp))
                            MyButton(
                                size = 70.dp,
                                func = { if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play() },
                                icon = if (!uiState.playing) Icons.Rounded.PlayArrow else Icons.Rounded.Close,
                            )
                            Spacer(modifier = Modifier.width(20.dp))
                            MyButton(
                                size = 50.dp,
                                func = { exoPlayer.seekTo(exoPlayer.currentPosition + 1000) },
                                icon = Icons.AutoMirrored.Rounded.KeyboardArrowRight
                            )
                        }
                    }
                    AnimatedVisibility(uiState.onDrag) {
                        Column {
                            TimeCalculator(
                                seconds = uiState.currentSeconds,
                                isBig = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            TimeCalculator(
                                seconds = uiState.dragValue * 1000,
                                isBig = false,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { viewModel.hideController(!exoPlayer.isPlaying) })
                }
                .padding(horizontal = 40.dp),
        ) {
            BackHandler { }
            Spacer(modifier = Modifier.weight(1f))
            AnimatedVisibility(visible = uiState.showController) {
                Column {
                    Spacer(modifier = Modifier.weight(1f))
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        MyButton(
                            size = 40.dp,
                            func = { viewModel.changeIsLocked(false) },
                            icon = Icons.Outlined.Lock
                        )
                    }
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}

fun Context.findActivity(): android.app.Activity? {
    var context = this
    while (context is android.content.ContextWrapper) {
        if (context is android.app.Activity) return context
        context = context.baseContext
    }
    return null
}

@Composable
fun QualitySelector(
    onQualitySelected: (String) -> Unit,
    expanded: Boolean = false,
    closeDropdown: () -> Unit,
    qualityOptions: List<String>,
    isSelected: String,
) {

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { closeDropdown() }
    ) {
        qualityOptions.forEach { quality ->
            DropdownMenuItem(
                text = {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = quality)
                        if (isSelected == quality) {
                            Icon(
                                Icons.Rounded.Check,
                                contentDescription = null,
                            )
                        }
                    }
                },
                onClick = {
                    onQualitySelected(quality)
                })
        }
    }
}