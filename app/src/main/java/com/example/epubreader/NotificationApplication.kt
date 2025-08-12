package com.example.epubreader

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager

class NotificationApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val notificationChannel = NotificationChannel(
            "daily_reminder",
             "reminder",
            NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.createNotificationChannel(notificationChannel)
    }
}