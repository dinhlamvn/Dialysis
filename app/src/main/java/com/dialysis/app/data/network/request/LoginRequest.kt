package com.dialysis.app.data.network.request

import android.os.Build
import com.dialysis.app.BuildConfig
import com.google.gson.annotations.SerializedName
import java.util.Locale

data class LoginRequest(
    @SerializedName("identifier")
    val identifier: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("platform")
    val platform: String,
    @SerializedName("device_name")
    val deviceName: String,
    @SerializedName("app_version")
    val appVersion: String,
) {
    companion object {
        const val PLATFORM = "android"

        /** Builds a login request with this device's platform, name and app version. */
        fun create(identifier: String, password: String) = LoginRequest(
            identifier = identifier,
            password = password,
            platform = PLATFORM,
            deviceName = formatDeviceName(Build.MANUFACTURER, Build.MODEL),
            appVersion = BuildConfig.VERSION_NAME,
        )
    }
}

/**
 * "samsung" + "SM-S918B" -> "Samsung SM-S918B". The manufacturer is dropped when blank or when
 * the model already starts with it ("Google" + "Google Pixel 8" -> "Google Pixel 8").
 */
internal fun formatDeviceName(manufacturer: String?, model: String?): String {
    val maker = manufacturer.orEmpty().trim().replaceFirstChar { it.titlecase(Locale.ROOT) }
    val device = model.orEmpty().trim()
    return when {
        maker.isEmpty() -> device
        device.isEmpty() -> maker
        device.startsWith(maker, ignoreCase = true) -> device
        else -> "$maker $device"
    }
}
