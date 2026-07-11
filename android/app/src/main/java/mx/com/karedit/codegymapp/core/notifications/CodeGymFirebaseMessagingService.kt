package mx.com.karedit.codegymapp.core.notifications

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
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import mx.com.karedit.codegymapp.MainActivity
import mx.com.karedit.codegymapp.R
import mx.com.karedit.codegymapp.di.AppContainer

class CodeGymFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        AppContainer(applicationContext).fcmTokenRegistrar.registerToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title
            ?: message.data["title"]
            ?: getString(R.string.app_name)
        val body = message.notification?.body
            ?: message.data["body"]
            ?: message.data["message"]
            ?: return

        showNotification(
            title = title,
            body = body,
            type = message.data["type"],
            screen = message.data["screen"]
        )
    }

    private fun showNotification(title: String, body: String, type: String?, screen: String?) {
        if (!canPostNotifications()) {
            return
        }

        ensureNotificationChannel()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NOTIFICATION_TYPE, type.orEmpty())
            putExtra(EXTRA_NOTIFICATION_SCREEN, screen.orEmpty())
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.codegym_icon)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(
                NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.codegym_icon)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("Tienes un recordatorio pendiente.")
                    .build()
            )
            .build()

        NotificationManagerCompat.from(this).notify(notificationId(), notification)
    }

    private fun canPostNotifications(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Recordatorios CodeGym",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Avisos de retos pendientes y vencidos"
        }

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun notificationId(): Int {
        return (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
    }

    private companion object {
        const val CHANNEL_ID = "codegym_reminders"
        const val EXTRA_NOTIFICATION_TYPE = "notification_type"
        const val EXTRA_NOTIFICATION_SCREEN = "notification_screen"
    }
}
