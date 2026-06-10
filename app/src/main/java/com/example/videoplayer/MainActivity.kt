package com.example.videoplayer

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.videoplayer.ui.theme.VideoPlayerTheme
import com.example.videoplayer.ui.VideoPlayerScreen

class MainActivity : ComponentActivity() {

    private val viewModel: PlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 处理从外部（文件管理器）打开的视频 Intent
        val intentUri: Uri? = if (intent?.action == android.content.Intent.ACTION_VIEW) {
            intent.data
        } else null

        setContent {
            VideoPlayerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VideoPlayerApp(
                        viewModel = viewModel,
                        initialUri = intentUri
                    )
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.pauseForBackground()
    }

    override fun onStart() {
        super.onStart()
        viewModel.resumeAfterForeground()
    }
}

@Composable
fun VideoPlayerApp(
    viewModel: PlayerViewModel,
    initialUri: Uri? = null
) {
    val context = LocalContext.current

    // 权限请求
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* 授权结果，无需特别处理，用户操作文件时自然触发 */ }

    // 启动时检查并请求权限
    LaunchedEffect(Unit) {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_VIDEO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        val hasPermission = ContextCompat.checkSelfPermission(
            context, permission
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            permissionLauncher.launch(permission)
        }
    }

    // 文件选择器
    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.loadVideo(it) }
    }

    // 如果有初始 URI（从文件管理器打开），直接加载
    LaunchedEffect(initialUri) {
        initialUri?.let { viewModel.loadVideo(it) }
    }

    VideoPlayerScreen(
        viewModel = viewModel,
        onOpenFile = {
            filePicker.launch(arrayOf("video/*"))
        }
    )
}
