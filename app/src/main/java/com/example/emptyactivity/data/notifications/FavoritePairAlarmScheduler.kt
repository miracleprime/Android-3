package com.example.emptyactivity.data.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.emptyactivity.receivers.FavoritePairReminderReceiver
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FavoritePairAlarmScheduler(
    private val context: Context
) {
    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(fullName: String, time: String) {
        val triggerAtMillis = parseTimeToTriggerMillis(time) ?: return
        val pendingIntent = createPendingIntent(fullName, time)

        alarmManager.cancel(pendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            alarmManager.canScheduleExactAlarms()
        ) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    fun cancel() {
        alarmManager.cancel(createPendingIntent("", ""))
    }

    private fun createPendingIntent(fullName: String, time: String): PendingIntent {
        val intent = Intent(context, FavoritePairReminderReceiver::class.java).apply {
            putExtra(FavoritePairReminderReceiver.EXTRA_FULL_NAME, fullName)
            putExtra(FavoritePairReminderReceiver.EXTRA_TIME, time)
        }

        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun parseTimeToTriggerMillis(time: String): Long? {
        return try {
            val parser = SimpleDateFormat("HH:mm", Locale.getDefault())
            parser.isLenient = false

            val parsedDate = parser.parse(time) ?: return null

            val source = Calendar.getInstance().apply {
                this.time = parsedDate
            }

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, source.get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, source.get(Calendar.MINUTE))
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                return null
            }

            calendar.timeInMillis
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private const val REQUEST_CODE = 7001
    }
}