package com.example.todo.notesappsql

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class ReminderBroadcastReceiver : BroadcastReceiver() {
    private val CHANNEL_1 = "CHANNEL_ID1"
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("Title")
        val desc = intent.getStringExtra("Desc")
        val id = intent.getIntExtra("id", 0)
        Log.d("Not Id", id.toString())
        val notificationManager = ContextCompat.getSystemService(
            context,
            NotificationManager::class.java
        ) as NotificationManager

        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("Note_Id", id) // Pass the note ID to the activity
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val channel = NotificationChannel(CHANNEL_1, title, NotificationManager.IMPORTANCE_HIGH)
        channel.description = desc
        notificationManager.createNotificationChannel(channel)
        val notification = NotificationCompat.Builder(context, CHANNEL_1)
            .setContentTitle(title)
            .setContentText(desc)
            .setSmallIcon(R.drawable.notification)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(1, notification)
        //Toast.makeText(context, title, Toast.LENGTH_SHORT).show()
    }
}
