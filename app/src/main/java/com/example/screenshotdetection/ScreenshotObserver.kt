package com.example.screenshotdetection

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScreenshotObserver(
    private val context: Context
) : ContentObserver(null) {
    private lateinit var onScreenshotDetected: () -> Unit
    private var lastScreenshotUri = ""

    fun setMyOnScreenshotDetectedListener(myListener: () -> Unit) {
        onScreenshotDetected = myListener
    }

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)

        CoroutineScope(Dispatchers.IO).launch {
            Log.d("ScreenshotObserver", "onChangeTriggered")

            // Check if the change is in the Screenshots directory
            uri?.let {
                if (it.toString().contains("content://media/external/images/media")) {
                    val cursor = context.contentResolver.query(
                        uri,
                        arrayOf(MediaStore.Images.Media.DISPLAY_NAME),
                        null,
                        null,
                        null
                    )
                    cursor?.use { c ->
                        if(c.moveToFirst()) {
                            val columnIndex =
                                c.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                            if (columnIndex >= 0) {
                                val fileName =
                                    c.getString(columnIndex)

                                if (fileName.contains("Screenshot") && lastScreenshotUri != fileName) {
                                    lastScreenshotUri = fileName
                                    withContext(Dispatchers.Main) {
                                        Log.d("ScreenshotObserver", "Screenshot detected: $fileName")
                                        onScreenshotDetected()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}