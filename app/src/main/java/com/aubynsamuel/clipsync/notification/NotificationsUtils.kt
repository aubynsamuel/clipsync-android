package com.aubynsamuel.clipsync.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.aubynsamuel.clipsync.R
import com.aubynsamuel.clipsync.activities.MainActivity
import com.aubynsamuel.clipsync.activities.ShareClipboardActivity

const val channelId = "ClipSyncServiceChannel"

fun createServiceNotification(context: Context): Notification {
    val contentIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val contentPendingIntent = PendingIntent.getActivity(
        context,
        0,
        contentIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val shareIntent = Intent(context, ShareClipboardActivity::class.java).apply {
        action = "ACTION_SHARE"
    }

    val sharePendingIntent = PendingIntent.getActivity(
        context, 1, shareIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val dismissIntent = Intent(context, NotificationReceiver::class.java).apply {
        action = "ACTION_DISMISS"
    }

    val dismissPendingIntent = PendingIntent.getBroadcast(
        context,
        2,
        dismissIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    return NotificationCompat.Builder(context, channelId)
        .setContentTitle("ClipSync Active")
        .setContentText("Ready to share clipboard")
        .setSmallIcon(R.mipmap.ic_launcher)
        .addAction(0, "Share", sharePendingIntent)
        .addAction(0, "Dismiss", dismissPendingIntent)
        .setOngoing(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(contentPendingIntent)
        .build()
}

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "ClipSync Service"
        val descriptionText = "Bluetooth clipboard sharing service"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
        }
        NotificationManagerCompat.from(context).createNotificationChannel(channel)
    }
}

fun showReceivedNotification(text: String, context: Context) {
    val notificationId = Math.random() * 10
    val copyIntent = Intent(context, NotificationReceiver::class.java).apply {
        action = "ACTION_COPY"
        putExtra("CLIP_TEXT", text)
        putExtra("NOTIFICATION_ID", notificationId.toInt())
    }

    val copyPendingIntent = PendingIntent.getBroadcast(
        context, 1, copyIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification =
        NotificationCompat.Builder(context, channelId).setContentTitle("ClipText Received")
            .setContentText(text.take(50) + if (text.length > 50) "..." else "")
            .setSmallIcon(R.mipmap.ic_launcher)
            .addAction(0, "Copy", copyPendingIntent)
            .setAutoCancel(true)
            .build()

    NotificationManagerCompat.from(context).apply {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notify(notificationId.toInt(), notification)
        }
    }
}

fun sharingResultNotification(title: String, text: String, context: Context) {
    val notificationId = 1000
    val contentIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val contentPendingIntent = PendingIntent.getActivity(
        context,
        0,
        contentIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification =
        NotificationCompat.Builder(context, channelId)
            .setContentTitle(title.toString())
            .setContentText(text.take(50) + if (text.length > 50) "..." else "")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .build()

    NotificationManagerCompat.from(context).apply {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notify(notificationId.toInt(), notification)
        }
    }
}