package com.example.screenshotdetection

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DetectNewImageInStorageService : Service() {
    companion object {
        private const val CHANNEL_ID = "DetectNewImageChannel"
        private const val NOTIFICATION_ID = 1
    }
    private lateinit var screenshotObserver: ScreenshotObserver

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        startScreenshotObserver()
        screenshotObserver.setMyOnScreenshotDetectedListener {
            showToast("Screenshot detected !")
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        super.onDestroy()
        contentResolver.unregisterContentObserver(screenshotObserver)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Screen Capture Service Channel",
                NotificationManager.IMPORTANCE_LOW // if importance default W notification not shown
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        val stopIntent =
            Intent(this, StopDetectNewImageReceiver::class.java) // Define a receiver to stop the service
        val stopPendingIntent = PendingIntent.getBroadcast(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("New image stored detection ")
            .setContentText("service running...")
            .setSmallIcon(android.R.drawable.ic_menu_gallery)
            .addAction(
                android.R.drawable.ic_media_pause,
                "Stop Service",
                stopPendingIntent
            )
            .build()
    }

    private fun showToast(message: String) {
        // Since we're in a service, we need to ensure it runs on the main thread
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(this@DetectNewImageInStorageService, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun startScreenshotObserver() {
        val handler = Handler(Looper.getMainLooper())
        screenshotObserver = ScreenshotObserver(handler, this)

        // URI for external images (where screenshots are usually saved)
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        // Register the content observer to listen for changes in external images
        contentResolver.registerContentObserver(uri, true, screenshotObserver)

        Log.d("ScreenshotObserver", "ScreenshotObserver started and registered.")
    }

}