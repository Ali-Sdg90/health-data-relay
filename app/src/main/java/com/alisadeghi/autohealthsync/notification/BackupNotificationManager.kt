package com.alisadeghi.autohealthsync.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.alisadeghi.autohealthsync.MainActivity
import com.alisadeghi.autohealthsync.R

class BackupNotificationManager(private val context: Context) {
    init {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.notification_channel_description)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun notifyFinalFailure(detail: String? = null) {
        val localizedDetail = when (detail) {
            "The daily backup file could not be created." ->
                context.getString(R.string.notification_failure_json)
            null -> context.getString(R.string.notification_failure_default)
            else -> detail
        }
        show(
            NOTIFICATION_FAILURE,
            context.getString(R.string.notification_failure_title),
            context.getString(R.string.notification_open_details, localizedDetail),
        )
    }

    fun notifyRecovered(fileName: String) {
        show(
            fileName.hashCode(),
            context.getString(R.string.notification_recovered_title),
            context.getString(R.string.notification_recovered_body, fileName),
        )
    }

    fun notifyActionRequired() {
        show(
            NOTIFICATION_ATTENTION,
            context.getString(R.string.notification_attention_title),
            context.getString(R.string.notification_attention_body),
        )
    }

    private fun show(id: Int, title: String, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return

        val intent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).notify(id, notification)
    }

    companion object {
        const val CHANNEL_ID = "backup_status"
        private const val NOTIFICATION_FAILURE = 1001
        private const val NOTIFICATION_ATTENTION = 1002
    }
}
