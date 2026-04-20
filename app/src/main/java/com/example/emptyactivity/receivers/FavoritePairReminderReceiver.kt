package com.example.emptyactivity.receivers

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.emptyactivity.MainActivity
import com.example.emptyactivity.data.notifications.FavoritePairNotificationHelper

class FavoritePairReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        FavoritePairNotificationHelper.createChannel(context)

        val fullName = intent.getStringExtra(EXTRA_FULL_NAME).orEmpty()
        val time = intent.getStringExtra(EXTRA_TIME).orEmpty()

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val contentPendingIntent = PendingIntent.getActivity(
            context,
            8001,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "Любимая пара начинается"
        val text = if (fullName.isBlank()) {
            "Пора на мобильную разработку. Время: $time"
        } else {
            "$fullName, пора на любимую пару. Время: $time"
        }

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notification = NotificationCompat.Builder(
            context,
            FavoritePairNotificationHelper.CHANNEL_ID
        )
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val EXTRA_FULL_NAME = "extra_full_name"
        const val EXTRA_TIME = "extra_time"
        private const val NOTIFICATION_ID = 7002
    }
}