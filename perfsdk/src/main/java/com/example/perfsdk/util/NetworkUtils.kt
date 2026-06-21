package com.example.perfsdk.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

internal object NetworkUtils {

    fun getNetworkType(context: Context): String {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return "NO_INTERNET"
        val caps = cm.getNetworkCapabilities(network) ?: return "NO_INTERNET"
        return when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WIFI"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "CELLULAR"
            else -> "UNKNOWN"
        }
    }

    // 5G subtype detection requires READ_PHONE_STATE permission — not collected.
    fun getNetworkSubtype(context: Context): String = ""
}
