# ============================================
# Compose
# ============================================
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ============================================
# Media3 / ExoPlayer
# ============================================
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**
-dontwarn com.google.common.**
-dontwarn org.checkerframework.**

# ============================================
# Kotlin
# ============================================
-keep class kotlin.** { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-keepattributes *Annotation*, InnerClasses
-dontnote kotlin.**

# ============================================
# App classes
# ============================================
-keep class com.example.videoplayer.** { *; }

# ============================================
# General
# ============================================
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature
-keepattributes Exceptions
