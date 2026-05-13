# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Retrofit
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }

# Keep Retrofit interfaces
-keep interface * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Gson
-keepattributes Signature
-keepattributes *Annotation*

# Keep model classes (adjust package)
-keep class com.yourpackage.model.** { *; }

# Keep Gson serialized names
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Kotlin Metadata
-keep class kotlin.Metadata { *; }

# Coroutines
-dontwarn kotlinx.coroutines.**

-keepattributes Signature

-keepattributes Signature
-keepattributes *Annotation*

-keep class com.yourpackage.model.** { *; }

-keep interface * {
    @retrofit2.http.* <methods>;
}

# Koin
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

-keep class * extends androidx.lifecycle.ViewModel

-keep class org.koin.ksp.generated.** { *; }

-dontwarn org.koin.androidx.compose.**