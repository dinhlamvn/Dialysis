package com.dialysis.app.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/** Connectivity seam for view models, so tests can fake being offline. */
fun interface ConnectivityChecker {
    fun isOnline(): Boolean
}

/** Current connectivity, used to choose online-first vs. offline (pending) writes. */
class NetworkMonitor(context: Context) : ConnectivityChecker {

    private val connectivityManager =
        context.applicationContext.getSystemService(ConnectivityManager::class.java)

    override fun isOnline(): Boolean {
        val manager = connectivityManager ?: return false
        val capabilities = manager.getNetworkCapabilities(manager.activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
