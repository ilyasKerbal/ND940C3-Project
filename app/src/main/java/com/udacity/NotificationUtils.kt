package com.udacity

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

private const val NOTIFICATION_ID = 0

fun NotificationManager.sendNotification(messageBody: String, filename: String, applicationContext: Context, status: Status) {
    val contentIntent = Intent(applicationContext, DetailActivity::class.java)
    contentIntent.apply {
        putExtra("fileName", filename)
        putExtra("status", status.name)
    }

    val contentPendingIntent = PendingIntent.getActivity(applicationContext, NOTIFICATION_ID, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT)

    val builder = NotificationCompat.Builder(
        applicationContext,
        applicationContext.getString(R.string.notification_channel_id))
        .setContentTitle("Git Download manger")
        .setSmallIcon(R.drawable.notifaction)
        .setContentText(messageBody)
        .setContentIntent(contentPendingIntent)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .addAction(R.drawable.notifaction, "Open Status", contentPendingIntent)

    notify(NOTIFICATION_ID, builder.build())
}
