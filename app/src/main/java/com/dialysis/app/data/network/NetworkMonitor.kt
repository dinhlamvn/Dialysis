package com.dialysis.app.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/** Current connectivity, used to choose online-first vs. offline (pending) writes. */
class NetworkMonitor(context: Context) {

    private val connectivityManager =
        context.applicationContext.getSystemService(ConnectivityManager::class.java)

    fun isOnline(): Boolean {
        val manager = connectivityManager ?: return false
        val capabilities = manager.getNetworkCapabilities(manager.activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
