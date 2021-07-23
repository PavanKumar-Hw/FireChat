package com.example.firechat

import android.app.Application
import com.example.firechat.common.Constants
import com.example.firechat.common.Util
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FireChat : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Constants.FIRE_BASE_TOKEN = task.result.toString()
                Util.updateDeviceToken(this, task.result.toString())
            }
        }
    }
}