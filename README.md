# ScreenshotDetection
detection of screenhots taken by observing images addition on the device storage


### Project status : Workable, documentation in progress...

## target audience
This project is for Jetpack Compose initiated user

## Presentation
Previously i showed the way to take screenshot from a floating composable displayed over all the apps (see my project `FloatingControlBar`. The implemented mechanism to do the screenshot is quite complex and moreover requires the user accepts a "screen capture" permission and that even if nothing is recorded. This prompt can easily scare the user and convinces him to not use the app. But it remains for me a good simple demo of the use of `MediaProjection`.

So i made this more simple app that detect screenshot taken in a background Service. As Android doesn't have dedicated elements to detect screenshots (with android 14 yes but only from activity) we will use a little tip to detect them. 


By observing the new data stored in tehe device tahnks to a `ContentObserver` object it is possible to detect the screenshot taken. Indeed, once the data stored is detected and contents Screenshot in its name we can trigger some actions to do. In our case we will trigger a log and a Toast.








## Overview

# Init

# Code
