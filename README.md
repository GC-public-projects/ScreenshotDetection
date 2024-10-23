# ScreenshotDetection
detection of screenhots taken by observing image crations on the device storage


### Project status : Workable, documentation completed

## target audience
This project is for Jetpack Compose initiated user.

## Presentation
Previously i showed the way to take screenshot from a floating composable displayed over all the apps (see my project `FloatingControlBar`. The implemented mechanism to do the screenshot is quite complex and moreover requires the user accepts a "screen capture" permission and that even if nothing is recorded. This prompt can easily scare the user and convinces him to not use the app. But it remains for me a good simple demo of the use of `MediaProjection`.


So i made this more simple app that detect screenshot taken in a background Service. As Android doesn't have dedicated elements to detect screenshots (with android 14 yes but only from activity) we will use a little tip to detect them. 


By observing the new data stored in the device thanks to a `ContentObserver` object it is possible to detect the screenshot taken. Indeed, once the data stored is detected and contents `Screenshot` in its name, we can trigger some actions to do. In our case we will trigger a log and a Toast.


## Overview
- 1 : Alert dialog photos and videos
- 2 : Alert dialog notifications
- 3 : Toast permission refused
- 4 : Notification Service
- 5 : Toast screenshot detected


<img src="/screenshots/1.jpg" alt="Alert dialog photos and videos" height="500">&emsp;
<img src="/screenshots/2.jpg" alt="Alert dialog notifications" height="500">&emsp;
<img src="/screenshots/3.jpg" alt="Toast permission refused" height="500">&emsp;


<img src="/screenshots/4.jpg" alt="Notification Service" height="500">&emsp;
<img src="/screenshots/5.jpg" alt="Toast screenshot detected" height="500">&emsp;

## Warning
The screenshot trigger with virtual devices doesn't work with the availabe screenshot icon. It exist some ways to implent the screenshot correctly. I didn't use them i just plugged a real device (Galaxy S23) & with this one the app works fine.


# Init
## Permissions 
- Following the Android versions the permissions to require are not the same.
- for `FOREGROUND_SERVICE` & `SERVICE_DATA_SYNC`, AlertDialogs don't exist. the simple fact to declare them in the Manifest is enough to allow them.


In AndroidManifest
``` xml
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

### Receiver
The receiver to stop the service vie the button of the notification needs to be setup in a class but also in the Manifest
``` xml
...
<receiver android:name=".StopDetectNewImageReceiver" />
<activity
    ...
```
### Service 
DetectNewImageInStorageService needs to be setup in the Manifest
``` xml
    </activity>
    <service
        android:name=".DetectNewImageInStorageService"
        android:exported="false"
        android:foregroundServiceType="dataSync"
        android:permission="android.permission.FOREGROUND_SERVICE"/>
</application>
```

# Code

## ScreenshotObserver (class)

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

- `cursor` : it contains all the data (rows) of the query done on a provider via the `contentResolver`. As the uri passed target a specific file, it is compound of only one row. 
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

## StopDetectNewImageReceiver (class)

### Purpose
Setup of the receiver to stop the service via its notification

### Content
In Main package create Kotlin class named `StopDetectNewImageReceiver`
``` kotlin
class StopDetectNewImageReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Stop the ScreenCaptureService if it is running
        val serviceIntent = Intent(context, DetectNewImageInStorageService::class.java)
        context.stopService(serviceIntent)
    }
}
```

## DetectNewImageInStorageService (class)

### Purpose
holds the instance of the ScreenshotObserver, the actions to do when a screenshot is taken and the notification mechanism to stop the service.

### Content
in the main package create a Kotlin class named `DetectNewImageInStorageService`

``` kotlin
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
            Intent(
                this,
                StopDetectNewImageReceiver::class.java
            ) // Define a receiver to stop the service
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
        screenshotObserver = ScreenshotObserver(this@DetectNewImageInStorageService)

        CoroutineScope(Dispatchers.Main).launch {
            // URI for external images (where screenshots are usually saved)
            val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

            // Register the content observer to listen for changes in external images
            contentResolver.registerContentObserver(uri, true, screenshotObserver)

            Log.d("ScreenshotObserver", "ScreenshotObserver started and registered.")
        }
    }
}
```

### Components explanations

- `companion object` : hold the constants for the notification
- `screeshotObserver` : instance of our `ScreenshotObserver` class initialized in `onCreate`
- `onCreate` : starts the notification, calls `startScreenshotObserver()` and setup the action to do when a screenshot is taken via the function `screenshotObserver.setMyOnScreenshotDetectedListener`
- `onDestroy` : release the observer to avoid memory leaks on the service closure
- `createNotificationChannel` : function that creates the notification Channel
- `createNotification()` : function that builds the notification. the Intent of `StopDetectNewImageReceiver` is setup and passed as param of the pendingIntent in orer to be used by the notification.
- `showToast` : function used to display a Toast with the message "Screenshot detected !"
- `startScreenshotObserver()` : instantiate our `ScreenshotObserver` & register it in `contentResolver`
- `contentResolver` : already explained in the ScreenshotObserver. It is used it to register the Observer 

## MainActivity & PermissionStatus (classes)

### Purpose
- Hold the multiple permission mechanism (simplified, not the best exemple)
- starts `DetectNewImageInStorageService` only when all permission are accepted ()
 max)
 - Display the Mainscreen (just "Hello Wold" displayed on the middle)

 ### Content
 Modify MainActivity class
 ``` kotlin
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
                Manifest.permission.READ_EXTERNAL_STORAGE
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

 ```

 ### Components explanations
 No explanations here, the project is for initiated users and the multiple permission mechanism hence the start of a service are considered as known. Some conditions to handle some permissions need to be implemented as the permissions are not the same following the Android version.










