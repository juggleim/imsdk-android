# UIKit Sample - Customizations
Sendbird's UIKit supports customizations. In this sample app, you can refer to various customizations use cases and how they are implemented.

Each item in the menu matches to a Kotlin file. For example, the menu item 'Channel Customization - Message UI' matches the file [MessageUISample.kt]((channel/MessageUISample.kt)).

Each file contains instructions for the usage of the code, along with links to the Docs pages for reference.

You can refer customization codes including:
- An example of how you can create your own custom message type, for example, a demonstration of sending a message in highlight.
- MessageListParams provides various options for retrieving a list of messages with MessageListParams
- ChannelListQuery provides various options for retrieving a list of channels with ChannelListQuery
- User list provides various options for retrieving a list of users
Styles, colors, fonts
- An example of multilingual UI support. In the /res/values-ko-rKR/strings.xml, you can find an example written in Korean language.
- and more!

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
5. Click the "Customizations" button.
