# UIKit Sample - Notification
Caution: This sample is not related to push notifications. It is a demonstration of Sendbird's notification product. For push notification, You can check codes handles FCM Push notification in [MyFirebaseMessagingService.kt](../common/fcm/MyFirebaseMessagingService.kt)

This sample demonstrates Sendbird notification using UIKit. Using a UIKit, you can build in-app notification easility. Please refer this [page](https://sendbird.com/docs/notifications/guide/v1/overview) for details.

Sendbird Notification is disabled by default, because it has some prerequisites to run properly.
1. Please follow this [Get Started](https://sendbird.com/docs/notifications/guide/v1/overview#getting-started) section and send a notification.
2. If you've done all prerequisites, set the value as true of this line in [BaseApplication.kt](../BaseApplication.kt)
```kotlin
internal const val enableNotificationSample = true
```


## Prerequisites
- Android Studio Giraffe
- JDK 11

## How to run
The following series of steps illustrate how to run the sample app.
1. Clone the repository
```
git@github.com:sendbird/sendbird-uikit-android.git
```
2. Open a directory in Android Studio.
3. Change the run configuration to "uikit-samples"  .
<img src="../../../../../../../../images/Screenshot 2023-11-07 at 8.31.20 PM.png" alt="Run configuration">
4. Run the sample app.
5. Click the "Sendbird Notification" button.

