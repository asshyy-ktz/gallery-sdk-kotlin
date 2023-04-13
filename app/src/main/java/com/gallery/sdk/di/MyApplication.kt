package com.gallery.sdk.di

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {
    companion object {
        private var instance: MyApplication? = null

        fun applicationContext(): MyApplication {
            return instance as MyApplication
        }
    }
}