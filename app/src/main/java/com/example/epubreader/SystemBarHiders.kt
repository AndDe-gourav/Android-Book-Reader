package com.example.epubreader

import android.app.Activity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

fun hideSystemBars(activity: Activity) {
    val windowInsetsController = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
    windowInsetsController.let {
        it.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        it.hide(WindowInsetsCompat.Type.systemBars())
    }
}

fun showSystemBars(activity: Activity){
    val windowInsetsController = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
    windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
}