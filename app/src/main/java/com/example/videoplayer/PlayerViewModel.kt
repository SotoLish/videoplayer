package com.example.videoplayer

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlayerState(
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val hasVideo: Boolean = false,
    val videoTitle: String = ""
)

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    val player: ExoPlayer = ExoPlayer.Builder(application).build()

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    // 支持的倍速列表
    val speedOptions = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.value = _state.value.copy(isPlaying = isPlaying)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                val duration = if (player.duration > 0) player.duration else 0L
                _state.value = _state.value.copy(duration = duration)
            }
        })

        // 定时刷新进度
        viewModelScope.launch {
            while (true) {
                if (player.isPlaying) {
                    _state.value = _state.value.copy(
                        currentPosition = player.currentPosition,
                        duration = if (player.duration > 0) player.duration else _state.value.duration
                    )
                }
                delay(500L)
            }
        }
    }

    /**
     * 加载并播放视频
     */
    fun loadVideo(uri: Uri, title: String = "") {
        val mediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
        _state.value = _state.value.copy(
            hasVideo = true,
            videoTitle = title.ifEmpty { uri.lastPathSegment ?: "视频" },
            playbackSpeed = 1.0f
        )
        // 重置倍速
        player.playbackParameters = PlaybackParameters(1.0f)
    }

    /**
     * 播放/暂停切换
     */
    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    /**
     * 跳转到指定位置（毫秒）
     */
    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
        _state.value = _state.value.copy(currentPosition = positionMs)
    }

    /**
     * 快进/快退指定秒数（正数=快进，负数=快退）
     */
    fun skipBy(seconds: Int) {
        val newPosition = (player.currentPosition + seconds * 1000L)
            .coerceIn(0L, if (player.duration > 0) player.duration else Long.MAX_VALUE)
        player.seekTo(newPosition)
        _state.value = _state.value.copy(currentPosition = newPosition)
    }

    /**
     * 设置播放速度
     */
    fun setSpeed(speed: Float) {
        player.playbackParameters = PlaybackParameters(speed)
        _state.value = _state.value.copy(playbackSpeed = speed)
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}
