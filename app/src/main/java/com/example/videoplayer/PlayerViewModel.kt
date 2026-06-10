package com.example.videoplayer

import android.app.Application
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.net.toUri
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
import java.security.MessageDigest

data class PlayerState(
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val hasVideo: Boolean = false,
    val videoTitle: String = ""
)

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs: SharedPreferences =
        application.getSharedPreferences("videoplayer_prefs", Application.MODE_PRIVATE)

    // 当前视频的存储 Key，空则表示没有加载视频
    private var currentVideoKey: String = ""

    val player: ExoPlayer = ExoPlayer.Builder(application).build()

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    // 支持的倍速列表（最高 6 倍）
    val speedOptions = listOf(
        0.5f, 0.75f, 1.0f, 1.25f, 1.5f,
        2.0f, 3.0f, 4.0f, 5.0f, 6.0f
    )

    /**
     * 根据视频 URI 生成唯一的存储 Key（SHA-256 哈希，避免特殊字符和长度问题）
     */
    private fun getVideoKey(uri: Uri): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(uri.toString().toByteArray())
        return "pos_" + digest.digest().joinToString("") { "%02x".format(it) }
    }

    /**
     * 保存当前播放位置到 SharedPreferences
     * 仅当进度超过 5 秒时才保存，避免片头误记
     */
    private fun savePosition() {
        if (currentVideoKey.isNotEmpty() && player.currentPosition > 5000) {
            prefs.edit().putLong(currentVideoKey, player.currentPosition).apply()
        }
    }

    // 进入后台前是否在播放（用于回到前台时恢复）
    private var wasPlayingBeforeBackground = false

    fun pauseForBackground() {
        wasPlayingBeforeBackground = player.isPlaying
        player.pause()
        savePosition()
    }

    fun resumeAfterForeground() {
        if (wasPlayingBeforeBackground && _state.value.hasVideo) {
            player.play()
        }
    }

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

        // 定时刷新进度（UI 用）
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

        // 每 5 秒自动保存一次播放位置
        viewModelScope.launch {
            while (true) {
                delay(5000L)
                savePosition()
            }
        }
    }

    /**
     * 加载并播放视频（自动恢复上次播放位置）
     */
    fun loadVideo(uri: Uri, title: String = "") {
        currentVideoKey = getVideoKey(uri)
        val savedPosition = prefs.getLong(currentVideoKey, 0L)

        val mediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)
        player.prepare()

        // 恢复上次播放位置（超过 5 秒才恢复，避免片头）
        if (savedPosition > 5000) {
            player.seekTo(savedPosition)
        }

        player.play()
        _state.value = _state.value.copy(
            hasVideo = true,
            videoTitle = title.ifEmpty { uri.lastPathSegment ?: "视频" },
            playbackSpeed = 1.0f,
            currentPosition = if (savedPosition > 5000) savedPosition else 0L
        )
        player.playbackParameters = PlaybackParameters(1.0f)
    }

    /**
     * 播放/暂停切换
     */
    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
            savePosition()
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
        savePosition()
        super.onCleared()
        player.release()
    }
}
