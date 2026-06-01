package com.example.videoplayer.ui

import android.view.ViewGroup
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.ui.PlayerView
import com.example.videoplayer.PlayerViewModel
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun VideoPlayerScreen(
    viewModel: PlayerViewModel,
    onOpenFile: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val density = LocalDensity.current

    var showControls by remember { mutableStateOf(true) }
    var showSpeedMenu by remember { mutableStateOf(false) }

    // 滑动快进状态
    var seekOffset by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var seekSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(showControls, state.isPlaying) {
        if (showControls && state.isPlaying && !isDragging) {
            delay(3000L)
            showControls = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // ---- 视频渲染层 ----
        if (state.hasVideo) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = viewModel.player
                        useController = false
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(state.duration) {
                        detectTapGestures(
                            onTap = { showControls = !showControls }
                        )
                    }
                    .pointerInput(state.duration) {
                        detectHorizontalDragGestures(
                            onDragStart = {
                                isDragging = true
                                seekOffset = 0f
                            },
                            onDragEnd = {
                                if (seekSeconds != 0) {
                                    viewModel.skipBy(seekSeconds)
                                }
                                isDragging = false
                                seekOffset = 0f
                                seekSeconds = 0
                                showControls = true
                            },
                            onDragCancel = {
                                isDragging = false
                                seekOffset = 0f
                                seekSeconds = 0
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                seekOffset += dragAmount
                                // 每 40dp = 10 秒，用 density 换算
                                val dpDragged = with(density) { seekOffset.toDp() }
                                seekSeconds = (dpDragged.value / 40f * 10f).roundToInt()
                            }
                        )
                    }
            )
        } else {
            WelcomeScreen(onOpenFile = onOpenFile)
        }

        // ---- 滑动快进指示器 ----
        if (isDragging && state.hasVideo) {
            SeekIndicator(seconds = seekSeconds)
        }

        // ---- 自定义控制栏 ----
        if (state.hasVideo) {
            AnimatedVisibility(
                visible = showControls || isDragging,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                PlayerControls(
                    state = state,
                    speedOptions = viewModel.speedOptions,
                    showSpeedMenu = showSpeedMenu,
                    onTogglePlay = {
                        viewModel.togglePlayPause()
                        showControls = true
                    },
                    onSeek = { viewModel.seekTo(it) },
                    onSkip = { viewModel.skipBy(it) },
                    onSpeedClick = { showSpeedMenu = !showSpeedMenu },
                    onSpeedSelect = { speed ->
                        viewModel.setSpeed(speed)
                        showSpeedMenu = false
                        showControls = true
                    },
                    onOpenFile = {
                        onOpenFile()
                        showControls = true
                    }
                )
            }
        }
    }
}

// ============================================================
//  滑动快进指示器（画面中央）
// ============================================================
@Composable
fun SeekIndicator(seconds: Int) {
    val direction = if (seconds > 0) "快进" else "快退"
    val arrow = if (seconds > 0) "⏩" else "⏪"
    val displaySeconds = if (seconds >= 0) seconds else -seconds

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xBB000000))
                .padding(horizontal = 28.dp, vertical = 20.dp)
        ) {
            Text(
                text = arrow,
                fontSize = 32.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "$direction ${displaySeconds}s",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ============================================================
//  欢迎界面
// ============================================================
@Composable
fun WelcomeScreen(onOpenFile: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.PlayCircle,
            contentDescription = null,
            tint = Color(0xFF6C63FF),
            modifier = Modifier.size(100.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "视频播放器",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "滑动快进 · 倍速播放",
            color = Color(0xFF888888),
            fontSize = 14.sp
        )
        Spacer(Modifier.height(40.dp))
        Button(
            onClick = onOpenFile,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6C63FF)
            ),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp)
        ) {
            Icon(Icons.Default.FolderOpen, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("选择视频文件", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}

// ============================================================
//  播放器控制栏
// ============================================================
@Composable
fun PlayerControls(
    state: com.example.videoplayer.PlayerState,
    speedOptions: List<Float>,
    showSpeedMenu: Boolean,
    onTogglePlay: () -> Unit,
    onSeek: (Long) -> Unit,
    onSkip: (Int) -> Unit,
    onSpeedClick: () -> Unit,
    onSpeedSelect: (Float) -> Unit,
    onOpenFile: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 渐变遮罩
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xCC000000), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0xDD000000))
                    )
                )
        )

        // ---- 顶部栏 ----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = state.videoTitle,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onOpenFile) {
                Icon(
                    Icons.Default.FolderOpen,
                    contentDescription = "打开文件",
                    tint = Color.White
                )
            }
        }

        // ---- 中间：快退 / 播放暂停 / 快进 ----
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            SkipButton(
                label = "-10s",
                icon = Icons.Rounded.Replay10,
                onClick = { onSkip(-10) }
            )
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0x99000000))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onTogglePlay
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (state.isPlaying) "暂停" else "播放",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }
            SkipButton(
                label = "+10s",
                icon = Icons.Rounded.Forward10,
                onClick = { onSkip(10) }
            )
        }

        // ---- 底部栏 ----
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatDuration(state.currentPosition),
                    color = Color.White,
                    fontSize = 13.sp
                )
                SpeedButton(
                    currentSpeed = state.playbackSpeed,
                    showMenu = showSpeedMenu,
                    speedOptions = speedOptions,
                    onButtonClick = onSpeedClick,
                    onSpeedSelect = onSpeedSelect
                )
                Text(
                    text = formatDuration(state.duration),
                    color = Color(0xAAFFFFFF),
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(4.dp))

            val progress = if (state.duration > 0) {
                state.currentPosition.toFloat() / state.duration.toFloat()
            } else 0f

            Slider(
                value = progress,
                onValueChange = { newProgress ->
                    onSeek((newProgress * state.duration).toLong())
                },
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF6C63FF),
                    activeTrackColor = Color(0xFF6C63FF),
                    inactiveTrackColor = Color(0x55FFFFFF)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // 滑动提示
            Text(
                text = "← 滑动快退         滑动快进 →",
                color = Color(0x44FFFFFF),
                fontSize = 10.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// ---- 快退/快进按钮 ----
@Composable
fun SkipButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(36.dp)
        )
        Text(text = label, color = Color.White, fontSize = 11.sp)
    }
}

// ---- 倍速按钮 + 弹出菜单 ----
@Composable
fun SpeedButton(
    currentSpeed: Float,
    showMenu: Boolean,
    speedOptions: List<Float>,
    onButtonClick: () -> Unit,
    onSpeedSelect: (Float) -> Unit
) {
    Box {
        TextButton(
            onClick = onButtonClick,
            colors = ButtonDefaults.textButtonColors(
                contentColor = Color(0xFF6C63FF)
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0x33FFFFFF))
        ) {
            Text(
                text = "${currentSpeed}x",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { onSpeedSelect(currentSpeed) },
            modifier = Modifier
                .background(Color(0xFF1E1E2E))
                .clip(RoundedCornerShape(12.dp))
        ) {
            speedOptions.forEach { speed ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = if (speed == 1.0f) "1.0x（正常）" else "${speed}x",
                            color = if (speed == currentSpeed) Color(0xFF6C63FF) else Color.White,
                            fontWeight = if (speed == currentSpeed) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = { onSpeedSelect(speed) },
                    leadingIcon = {
                        if (speed == currentSpeed) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF6C63FF),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                )
            }
        }
    }
}

// ---- 时间格式化 ----
fun formatDuration(ms: Long): String {
    if (ms <= 0) return "00:00"
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}
