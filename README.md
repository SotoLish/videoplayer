# VideoPlayer 安卓视频播放器

## 项目说明

一个简洁的 Android 原生视频播放器，基于 Jetpack Compose + Media3 ExoPlayer 构建。

## 功能

- **播放/暂停**：点击屏幕中央按钮，或单击视频区域
- **快进/快退**：
  - 点击 `-10s` / `+10s` 按钮，步进 10 秒
  - **双击**视频左侧快退 10 秒，双击右侧快进 10 秒
- **拖拽进度条**：底部进度条可拖拽到任意位置
- **倍速播放**：支持 0.5x / 0.75x / 1.0x / 1.25x / 1.5x / 2.0x
- **打开文件**：支持从文件管理器选择，也支持从外部 App 直接打开视频
- **自动隐藏控制栏**：播放状态下 3 秒后自动隐藏，点击恢复显示

## 构建要求

- Android Studio Hedgehog 或更新版本
- JDK 17+
- Android Gradle Plugin 8.5.2
- Kotlin 2.0.21
- 最低 Android SDK 24（Android 7.0）

## 构建步骤

```bash
# 克隆或下载项目后，在 Android Studio 中直接打开
# 或使用命令行构建 Debug APK：
./gradlew assembleDebug
```

APK 输出路径：`app/build/outputs/apk/debug/app-debug.apk`

## 技术栈

| 组件 | 版本 |
|------|------|
| Kotlin | 2.0.21 |
| Jetpack Compose BOM | 2024.09.03 |
| Media3 ExoPlayer | 1.4.1 |
| Material3 | 1.3.0 |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 35 (Android 15) |
