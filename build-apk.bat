@echo off
set GRADLE_USER_HOME=C:\Users\1\WorkBuddy\2026-06-01-08-35-18\VideoPlayer\gradle-home
set ANDROID_HOME=C:\Users\1\WorkBuddy\2026-06-01-08-35-18\android-sdk
set ANDROID_SDK_ROOT=%ANDROID_HOME%
cd /d C:\Users\1\WorkBuddy\2026-06-01-08-35-18\VideoPlayer
C:\Users\1\WorkBuddy\2026-06-01-08-35-18\gradle-8.8\bin\gradle.bat assembleDebug --no-daemon -Dorg.gradle.native=false 2>&1
echo EXIT_CODE=%ERRORLEVEL%
