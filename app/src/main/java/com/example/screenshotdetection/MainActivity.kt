package com.example.screenshotdetection

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.screenshotdetection.ui.theme.ScreenshotDetectionTheme

class MainActivity : ComponentActivity() {

    private val permissionsToRequest: MutableList<String> = mutableListOf()
    private var startServiceFlag = false

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted)  {
                val message = "All permissions are required ! Go to settings to allow them !"
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
            startServiceFlag = handleNextPermissionPrompt()
            if(startServiceFlag) { startDetectNewImageInStorageService() }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkPermissions()
        startServiceFlag = handleNextPermissionPrompt()
        if(startServiceFlag) { startDetectNewImageInStorageService() }

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

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(
                    android.Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(android.Manifest.permission.READ_MEDIA_IMAGES)
            }


            if (checkSelfPermission(
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun handleNextPermissionPrompt() : Boolean {
        if (permissionsToRequest.isNotEmpty()) {
            val nexPermission = permissionsToRequest.removeAt(0)
            requestPermissionLauncher.launch(nexPermission)
            return false
        } else {
            return true
        }
    }

    private fun startDetectNewImageInStorageService() {
        val intent = Intent(this, DetectNewImageInStorageService::class.java)
        this.startService(intent)
    }
}
