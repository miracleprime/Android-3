package com.example.emptyactivity.data.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object FavoritePairNotificationHelper {
    const val CHANNEL_ID = "favorite_pair_channel"
    private const val CHANNEL_NAME = "Любимая пара"
    private const val CHANNEL_DESCRIPTION = "Напоминания о начале любимой пары"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
            }

            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}