package com.example.screenshotdetection

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import android.util.Log

class ScreenshotObserver(
    handler: Handler,
    private val context: Context
) : ContentObserver(handler) {

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)

        Log.d("ScreenshotObserver", "onChangeTriggered")

        // Check if the change is in the Screenshots directory
        if (uri != null && uri.toString().contains("content://media/external/images/media")) {
            // A screenshot is likely taken
            val cursor = context.contentResolver.query(
                uri,
                arrayOf(MediaStore.Images.Media.DISPLAY_NAME),
                null,
                null,
                null
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                    if(columnIndex >=0 ) {
                        val fileName = it.getString(it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))
                        if (fileName.contains("Screenshot")) {
                            // Detected a screenshot
                            Log.d("ScreenshotObserver", "Screenshot detected: $fileName")
                        }
                    }

                }
            }
        }
    }
}