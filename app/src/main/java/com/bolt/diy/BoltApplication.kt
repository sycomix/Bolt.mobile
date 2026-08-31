package com.bolt.diy

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BoltApplication : Application() {
  override fun onCreate() {
    super.onCreate()
  }
}
