package com.premium.auranotes.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.premium.auranotes.MainActivity
import com.premium.auranotes.R
import com.premium.auranotes.repository.NoteRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: NoteRepository

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule all future reminders
            val reminderManager = ReminderManager(context)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val scheduledNotes = repository.getScheduledNotes().first()
                    val now = System.currentTimeMillis()
                    for (note in scheduledNotes) {
                        note.reminderTime?.let { time ->
                            if (time > now) {
                                reminderManager.scheduleReminder(note.id, note.title, time)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            val noteId = intent.getIntExtra(EXTRA_NOTE_ID, 0)
            val noteTitle = intent.getStringExtra(EXTRA_NOTE_TITLE) ?: context.getString(R.string.app_name)
            val noteContent = intent.getStringExtra(EXTRA_NOTE_CONTENT) ?: ""
            showNotification(context, noteId, noteTitle, noteContent)
        }
    }

    private fun showNotification(context: Context, noteId: Int, title: String, content: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = CHANNEL_ID

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_desc)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NOTE_ID, noteId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            noteId,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(if (title.isBlank()) context.getString(R.string.app_name) else title)
            .setContentText(content.ifBlank { context.getString(R.string.app_tagline) })
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(noteId, notification)
    }

    companion object {
        const val CHANNEL_ID = "aura_note_reminders"
        const val EXTRA_NOTE_ID = "extra_note_id"
        const val EXTRA_NOTE_TITLE = "extra_note_title"
        const val EXTRA_NOTE_CONTENT = "extra_note_content"
    }
}
