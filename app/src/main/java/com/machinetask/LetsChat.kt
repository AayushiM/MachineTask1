package com.machinetask

import android.app.Application
import android.content.Context

/**
 * Created by HP on 3/21/2018.
 */
class LetsChat : Application() {
    override fun onCreate() {
        super.onCreate()
        //        CameraApplication.init(this,true);
        appContext = applicationContext
    }

    companion object {
        var appContext: Context? = null
            private set
    }
}