package com.example.perfsdk.device

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.PowerManager
import com.example.perfsdk.util.NetworkUtils

internal class DeviceInfoCollector(private val context: Context) {

    data class DeviceSnapshot(
        val networkType: String,
        val networkSubtype: String,
        val availableRamMb: Long,
        val isBatterySaverActive: Boolean,
        val apiLevel: Int,
        val isEmulator: Boolean,
        val appPackageName: String
    )

    fun collect(): DeviceSnapshot = DeviceSnapshot(
        networkType = NetworkUtils.getNetworkType(context),
        networkSubtype = NetworkUtils.getNetworkSubtype(context),
        availableRamMb = availableRamMb(),
        isBatterySaverActive = isBatterySaverActive(),
        apiLevel = Build.VERSION.SDK_INT,
        isEmulator = isEmulator(),
        appPackageName = context.packageName
    )

    private fun availableRamMb(): Long {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)
        return info.availMem / (1024L * 1024L)
    }

    private fun isBatterySaverActive(): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isPowerSaveMode
    }

    private fun isEmulator(): Boolean =
        Build.HARDWARE == "ranchu" ||           // all modern QEMU-based AVDs (sdk_gphone* etc.)
        Build.HARDWARE == "goldfish" ||          // legacy AVDs
        Build.FINGERPRINT.startsWith("generic") ||
        Build.FINGERPRINT.startsWith("unknown") ||
        Build.FINGERPRINT.contains("generic") ||
        Build.MODEL.startsWith("sdk_") ||        // sdk_gphone16k_x86_64, sdk_gpc*, etc.
        Build.MODEL.contains("Emulator") ||
        Build.MODEL.contains("Android SDK built for x86") ||
        Build.MANUFACTURER.contains("Genymotion") ||
        (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
        Build.PRODUCT == "google_sdk"
}
