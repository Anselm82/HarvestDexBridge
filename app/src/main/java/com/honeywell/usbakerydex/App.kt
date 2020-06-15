package com.honeywell.usbakerydex

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build.VERSION


class App : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (VERSION.SDK_INT >= 26) {
            (getSystemService(NotificationManager::class.java) as NotificationManager).createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }
    }

    companion object {
        const val CHANNEL_ID = "DEXChannel"
        const val CHANNEL_NAME = "Honeywell DEX Channel"
    }
}