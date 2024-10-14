package com.example.screenshotdetection

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.screenshotdetection.ui.theme.ScreenshotDetectionTheme

class MainActivity : ComponentActivity() {

    private lateinit var screenshotObserver: ScreenshotObserver


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val handler = Handler(Looper.getMainLooper())
        screenshotObserver = ScreenshotObserver(handler, this)

        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        contentResolver.registerContentObserver(uri, true, screenshotObserver)


        enableEdgeToEdge()
        setContent {
            ScreenshotDetectionTheme {
                if (!hasPermissions()) {
                    requestPermissionReadMediaImages()
                    Log.d("ScreenshotObserver", "permission not granted")
                } else {
                    Log.d("ScreenshotObserver", "permission granted")
                    startScreenshotObserver()
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text("Hello World")
                }

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        contentResolver.unregisterContentObserver(screenshotObserver)
    }

    fun requestPermissionReadMediaImages() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES), 1)
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)
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

    fun hasPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }
}
