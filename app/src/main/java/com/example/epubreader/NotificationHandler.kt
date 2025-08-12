package com.example.epubreader

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlin.random.Random

class NotificationHandler(private val context: Context){
    private val notificationManager = context.getSystemService(NotificationManager::class.java)
    private val notificationChannelId = "daily_reminder"

    fun showNotification(){
        Log.d("notify", "notify")
        val notification = NotificationCompat.Builder(context, notificationChannelId)
            .setContentTitle("Book Notification")
            .setContentText("This is a book notification")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(Notification.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(Random.nextInt(), notification)
    }
}