package com.example.screenshotdetection

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.screenshotdetection.ui.theme.ScreenshotDetectionTheme

class MainActivity : ComponentActivity() {

    val permissionResults: MutableList<Pair<String, Boolean>> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkPermissions()
        permissionResults.forEach { pair ->
            if(pair.second == false) {
                requestPermissions(arrayOf(pair.first), 1)
            }
        }
        val intent = Intent(this, DetectNewImageInStorageService::class.java)
        this.startService(intent)


        enableEdgeToEdge()
        setContent {
            ScreenshotDetectionTheme {
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

    fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionResults.add(
                Pair(
                    android.Manifest.permission.READ_MEDIA_IMAGES,
                    checkSelfPermission(
                        android.Manifest.permission.READ_MEDIA_IMAGES
                    ) == PackageManager.PERMISSION_GRANTED
                )
            )

        } else {
            permissionResults.add(
                Pair(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                )
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissionResults.add(
                Pair(
                    android.Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC,
                    checkSelfPermission(
                        android.Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC
                    ) == PackageManager.PERMISSION_GRANTED
                )
            )
        } else {
            permissionResults.add(
                Pair(
                    android.Manifest.permission.FOREGROUND_SERVICE,
                    ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.FOREGROUND_SERVICE
                    ) == PackageManager.PERMISSION_GRANTED
                )
            )
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionResults.add(
                Pair(
                    android.Manifest.permission.POST_NOTIFICATIONS,
                    checkSelfPermission(
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                )
            )
        }

    }
}
