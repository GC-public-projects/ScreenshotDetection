package com.example.screenshotdetection

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class StopDetectNewImageReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Stop the ScreenCaptureService if it is running
        val serviceIntent = Intent(context, DetectNewImageInStorageService::class.java)
        context.stopService(serviceIntent)
    }
}