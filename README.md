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
```

### Components explanations

- `context` : the context of a service or an activity is needed to use `contentResolver` that's why it is use as param of tha class.
- `contentResolver` : It is the bridge between the app and the content providers (like the system's media store, contacts, calendar, or any other app's exposed content), it provides a unified API for reading, inserting, updating, and deleting data across different content providers. 
It is used here to query the `Mediastore` but is also used in `DetectNewImageInStorageService` to register `ScreenshotObserver` and make it workable.

- `MediaStore` : DB of Sqlite format that stores some data of the files in the storage (like \_ID, DISPLAY\_NAME, SIZE, RELATIVE_PATH, etc...)

- `onScreenshotDetected` : custom high order function called when a screenshot is detected. This function is affected thanks to the public `setMyOnScreenshotDetectedListener` function. the onCreate function from `DetectNewImageInStorageService` will call `setMyOnScreenshotDetectedListener` after instanciated `screenshotObserver` and provides as param a lambda expression that shows a Toast on the screen ("Screenshot detected !").

- `onChange` : method implemented by the abstract class `ContentObserver`. The function is called each time a change is done on `MediaStore.Images.Media.EXTERNAL_CONTENT_URI` as this uri has been registered with the observer in the contentResolver from our `DetectNewImageIthe nStorageService`

- `uri` : once a change is done on the Main uri `MediaStore.Images.Media.EXTERNAL_CONTENT_URI` an other uri that targets the change is created. Here is an exemple of screenshot uri from a Galaxy s23 : `content://media/external/images/media/1000009452`. The last number represent the \_ID column of the `MediaStre`

- `cursor` : it contains all the data (rows) of the query done on a provider via the contentResolver. As the uri passed target a specific file, it is compound of only one row. 
So no need to specify a selection & selectionArgs if the uri already target a file.

Here is the structure of the query :
``` kotlin
val cursor: Cursor? = contentResolver.query(
    uri,                   // The content URI (target dataset)
    projection,            // The columns to return (null returns all columns)
    selection,             // The selection criteria (where clause)
    selectionArgs,         // The arguments for the where clause
    sortOrder              // Sorting order (null returns unsorted)
)
```

- `filename` : the data of the cursor (the display name) is extracted in our val `filename` and checked. If the filename contains "Screenshot" a log is taken and `onScreenshotDetected()` is called (the Toast generation). 

- `lastScreenshotUri` : it contains the last screenShot uri taken. We need to remember it because in our case, by screenshot, `onChange` is triggered three times, so the same uri is handled 3 times too. As we just want to trigger once some actions we need to trigger them only if a new uri is handled. So `lastScreenshotUri` is part of the condition to trigger our actions.










