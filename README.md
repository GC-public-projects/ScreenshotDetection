# ScreenshotDetection
detection of screenhots taken by observing images addition on the device storage


### Project status : Workable, documentation in progress...

## target audience
This project is for Jetpack Compose initiated user

## Presentation
Previously i showed the way to take screenshot from a floating composable displayed over all the apps (see my project `FloatingControlBar`. The implemented mechanism to do the screenshot is quite complex and moreover requires the user accepts a "screen capture" permission and that even if nothing is recorded. This prompt can easily scare the user and convinces him to not use the app. But it remains for me a good simple demo of the use of `MediaProjection`.


So i made this more simple app that detect screenshot taken in a background Service. As Android doesn't have dedicated elements to detect screenshots (with android 14 yes but only from activity) we will use a little tip to detect them. 


By observing the new data stored in the device thanks to a `ContentObserver` object it is possible to detect the screenshot taken. Indeed, once the data stored is detected and contents `Screenshot` in its name, we can trigger some actions to do. In our case we will trigger a log and a Toast.


## Overview
<img src="/screenshots/1.jpg" alt="Alert dialog photos and videos" height="500">&emsp;
<img src="/screenshots/2.jpg" alt="Alert dialog notifications" height="500">&emsp;
<img src="/screenshots/3.jpg" alt="Toast permission refused" height="500">&emsp;


<img src="/screenshots/4.jpg" alt="Notification Service" height="500">&emsp;
<img src="/screenshots/5.jpg" alt="Toast screenshot detected" height="500">&emsp;



# Init
## Permissions 
- Following the Android versions the permissions to require are not the same.
- for `FOREGROUND_SERVICE` & `SERVICE_DATA_SYNC`, AlertDialogs don't exist. the simple fact to declare them in the Manifest is enough to allow them.


In AndroidManifest
```
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

<uses-permission
        android:name="android.permission.READ_EXTERNAL_STORAGE"
        android:maxSdkVersion="32" />
    <uses-permission
        android:name="android.permission.READ_MEDIA_IMAGES"
        android:minSdkVersion="33"
        />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission
        android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC"
        android:minSdkVersion="34" />

    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<application
	...
```

# Code

## ScreenshotObserver

### Purpose
Detects when a modification is made on the storage in a specific place & triggers some actions if the name contains "Screenshot". Works fine with last Samsungs Galaxy but can be adapted following the devices.

### Content
In main package create kotlin class named `ScreenshotObserver`
``` kotlin
class ScreenshotObserver(
    handler: Handler,
    private val context: Context
) : ContentObserver(handler) {
    private lateinit var onScreenshotDetected: () -> Unit
    private var lastScreenshotUri = ""

    fun setMyOnScreenshotDetectedListener(myListener: () -> Unit) {
        onScreenshotDetected = myListener
    }

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
                            if(lastScreenshotUri != fileName) {
                                // as onChange is triggered many times by screenshot we want execute the content 1 time.
                                Log.d("ScreenshotObserver", "Screenshot detected: $fileName")
                                onScreenshotDetected()
                                lastScreenshotUri = fileName
                            }
                        }
                    }
                }
            }
        }
    }
}
```

### Components explanations
