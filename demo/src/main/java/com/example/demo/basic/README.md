# UIKit Sample - Basic Usage
This sample is a chat app with UIKit’s core [features](https://sendbird.com/docs/chat/uikit/v3/android/features/overview) in which you can see items such as push notifications, total unread message count and auto sign-in are demonstrated. 

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
<img src="../../../../../../../../images/run.png" alt="Run configuration">
4. Run the sample app.
5. Click the "Basic Usage" button.

## How it works

### `GroupChannelMainActivity.kt`
It consists of two tabs, `ChannelListFragment` and `SampleSettingsFragment`. They are configured by the MainAdapter class.
- `ChannelListFragment` is provided by UIKit using the function `FragmentProviders.channelList.provide(Bundle())`. It shows channel lists of the user.
- `SampleSettingsFragment`: This is an example implementation that demonstates the setting screen related to the configuration of UIKit. You can edit user's profile and change global configuration like enabling dark theme.

### `Push notification`
You can check codes handles FCM Push notification in [MyFirebaseMessagingService.kt](../common/fcm/MyFirebaseMessagingService.kt) 
