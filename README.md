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
<img src="1.jpg" alt="Alert dialog photos and videos" height="500">&emsp;
<img src="2.jpg" alt="Alert dialog notifications" height="500">&emsp;
<img src="3.jpg" alt="Toast permission refused" height="500">&emsp;


<img src="4.jpg" alt="Notification Service" height="50">&emsp;
<img src="5.jpg" alt="Toast screenshot detected" height="500">&emsp;



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
