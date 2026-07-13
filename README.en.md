# JuggleIM Android SDK

English | [简体中文](README.md)

JuggleIM Android SDK is the official open-source Android SDK for JuggleIM. It provides persistent connections, conversations, messaging, chatrooms, user profiles, push notification plugins, and real-time audio/video call extensions for Android applications.

The JuggleIM ecosystem also includes:

| Project                                             | Description              |
|-----------------------------------------------------|--------------------------|
| [im-server](https://github.com/juggleim/im-server)  | JuggleIM server          |
| [imsdk-web](https://github.com/juggleim/imsdk-web)  | Web / JavaScript SDK     |
| im-android-sdk                                      | Android SDK and demo app |
| [im-ios-sdk](https://github.com/juggleim/imsdk-ios) | iOS SDK and demo app     |

## Features

- Core IM capabilities: connection management, reconnect, conversations, message send/receive, history, unread counts.
- Message types: text, image, file, custom messages, and business notification messages.
- Chat scenarios: one-to-one chat, group chat, chatroom, and live chatroom.
- Extensions: user profile, moments, audio/video calls, and message upload.
- Push plugins: Google FCM, Huawei, Xiaomi, OPPO, VIVO, Honor, and JPush.
- Call plugins: Zego, Agora, and LiveKit.
- Security: end-to-end encryption related capabilities are available in recent versions.
- Demo app: a runnable Android demo is included for login, conversation, messaging, and call flows.

## Quick Start

### 1. Add the Maven repository

Add the JuggleIM Maven repository to `settings.gradle` or your root Gradle file:

```gradle
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url "https://repo.juggle.im/repository/maven-releases/" }
    }
}
```

### 2. Add the SDK dependency

```gradle
dependencies {
    implementation "com.juggle.im:juggle:1.9.0"
}
```

Add optional plugins as needed:

```gradle
dependencies {
    implementation "com.juggle.push.jg:juggle:1.9.0"
    implementation "com.juggle.call.zego:juggle:1.9.0"
    implementation "com.juggle.call.agora:juggle:1.9.0"
    implementation "com.juggle.call.livekit:juggle:1.9.0"
}
```

### 3. Initialize the SDK

Initialize the SDK in your `Application`:

```java
import com.juggle.im.JIM;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        JIM.getInstance().init(this, "your_app_key");
    }
}
```

Use `InitConfig` when you need custom server URLs, logging, or push configuration:

```java
List<String> serverUrls = new ArrayList<>();
serverUrls.add("wss://your-im-server");
JIM.getInstance().setServerUrls(serverUrls);

JIM.InitConfig initConfig = new JIM.InitConfig.Builder()
        .setJLogConfig(logConfig)
        .setPushConfig(pushConfig)
        .build();

JIM.getInstance().init(this, "your_app_key", initConfig);
```

### 4. Connect a user

Connect with an IM token issued by your application server:

```java
JIM.getInstance().getConnectionManager().connect("user_im_token");
```

> Manage `appKey` and user tokens on your server side. Do not hard-code production secrets in client applications or public repositories.

## Repository Layout

| Directory | Description |
| --- | --- |
| `JuggleIM` | Core IM SDK |
| `JetIMKit` | UI Kit module |
| `demo` | Android demo app |
| `GooglePlugin` | Google FCM push plugin |
| `HWPlugin` | Huawei push plugin |
| `XMPlugin` | Xiaomi push plugin |
| `OPPOPlugin` | OPPO push plugin |
| `VIVOPlugin` | VIVO push plugin |
| `HonorPlugin` | Honor push plugin |
| `JGPlugin` | JPush plugin |
| `JZegoCall` | Zego call plugin |
| `JAgoraCall` | Agora call plugin |
| `JLiveKitCall` | LiveKit call plugin |

## Run the Demo

1. Open this repository with Android Studio.
2. Make sure JDK 17, Android Gradle Plugin, and Android SDK are available.
3. Run the `demo` or `app` module.
4. Configure your `appKey`, server URL, push vendor credentials, and call vendor credentials for your environment.

Demo initialization examples:

- `demo/src/main/java/com/juggle/chat/BaseApplication.kt`
- `demo/src/main/java/com/juggle/chat/LoginActivity.kt`

## Documentation and Community

- Website and docs: [https://www.juggle.im/](https://www.juggle.im/)
- Telegram Chinese group: [https://t.me/juggleim_zh](https://t.me/juggleim_zh)
- Telegram English group: [https://t.me/juggleim_en](https://t.me/juggleim_en)
- WeChat group: add the contact from [this QR code](https://downloads.juggleim.com/xiaoshan.jpg) to join

## Contributing

Issues and pull requests are welcome. Please read [CONTRIBUTING.md](CONTRIBUTING.md) before contributing.

## License

JuggleIM Android SDK is licensed under the [Apache License 2.0](LICENSE).
