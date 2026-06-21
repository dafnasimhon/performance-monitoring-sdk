package com.example.mysdk

import android.app.Application
import com.example.perfsdk.PerfSDK

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PerfSDK.init(this, "dev-api-key")
    }
}
