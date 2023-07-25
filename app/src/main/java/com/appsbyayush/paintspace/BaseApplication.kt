package com.appsbyayush.paintspace

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.appsbyayush.paintspace.utils.Constants.NOTIFICATION_CHANNEL_HIGH
import com.appsbyayush.paintspace.utils.Constants.NOTIFICATION_CHANNEL_LOW
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BaseApplication: Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        createNotificationChannels()
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelLow = NotificationChannel(
                NOTIFICATION_CHANNEL_LOW,
                "Channel Low",
                NotificationManager.IMPORTANCE_LOW
            )
            val channelHigh = NotificationChannel(
                NOTIFICATION_CHANNEL_HIGH,
                "Channel High",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channelLow)
            manager.createNotificationChannel(channelHigh)
        }
    }
}