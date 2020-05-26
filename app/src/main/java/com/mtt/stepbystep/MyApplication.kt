package com.mtt.stepbystep

import android.app.Application
import com.sap.cloud.mobile.foundation.authentication.AppLifecycleCallbackHandler

class MyApplication() : Application() {
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(AppLifecycleCallbackHandler.getInstance())
    }
}