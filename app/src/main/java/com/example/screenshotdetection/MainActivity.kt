package com.example.screenshotdetection

import android.Manifest
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
    private val permissionsStatusList: MutableList<PermissionStatus> = mutableListOf()
    private lateinit var permissionHandled: PermissionStatus

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                val message = "All permissions are required ! Go to settings to allow them !"
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            } else {
                permissionHandled.isGranted = true
            }
            handleNextPermissionPrompt()
            if (permissionsStatusList.all { it.isGranted }) {
                startDetectNewImageInStorageService()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkPermissions()
        if (permissionsStatusList.all { it.isGranted }) {
            startDetectNewImageInStorageService()
        } else {
            handleNextPermissionPrompt()
        }

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
            var isGranted = checkSelfPermission(
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED

            permissionsStatusList.add(
                PermissionStatus(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    isGranted,
                    !isGranted
                )
            )

            isGranted = checkSelfPermission(
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            permissionsStatusList.add(
                PermissionStatus(
                    Manifest.permission.POST_NOTIFICATIONS,
                    isGranted,
                    !isGranted
                )
            )
        } else {
            val isGranted = ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED

            permissionsStatusList.add(
                PermissionStatus(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    isGranted,
                    !isGranted
                )
            )
        }
    }

    private fun handleNextPermissionPrompt() {
        permissionsStatusList.forEach { permissionStatus ->
            if (permissionStatus.toRequest) {
                permissionHandled = permissionStatus
                permissionStatus.toRequest = false
                requestPermissionLauncher.launch(permissionStatus.permission)
                return
            }
        }
    }

    private fun startDetectNewImageInStorageService() {
        val intent = Intent(this, DetectNewImageInStorageService::class.java)
        this.startService(intent)
    }
}

data class PermissionStatus(
    var permission: String,
    var isGranted: Boolean,
    var toRequest: Boolean
)

