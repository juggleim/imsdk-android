<div align="center">
  <img src="demo/src/main/res/logo.png" alt="JuggleIM" width="120" />

  <h1>JuggleIM Android SDK</h1>

  <p><strong>A high-performance, open-source real-time messaging SDK for Android.</strong><br/>
  Built on a custom binary protocol over WebSocket. Powers chat, group, live chatroom, RTC signaling and moments out of the box.</p>

  <p>
    <a href="https://github.com/Juggleim/im-android-sdk"><img src="https://img.shields.io/github/stars/Juggleim/im-android-sdk?style=social" alt="GitHub Stars"/></a>
    <a href="https://github.com/Juggleim/im-android-sdk/blob/master/LICENSE"><img src="https://img.shields.io/github/license/Juggleim/im-android-sdk?cacheSeconds=3600" alt="License"/></a>
  </p>

  <p>
    <a href="#-features">Features</a> ·
    <a href="#-quick-start">Quick Start</a> ·
    <a href="#-documentation">Docs</a> ·
    <a href="#-ecosystem">Ecosystem</a> ·
    <a href="#-community">Community</a>
  </p>

  <p>
    English | <a href="./README.zh-CN.md">简体中文</a>
  </p>
</div>

---

## Why JuggleIM?

Building reliable, large-scale real-time messaging from scratch is hard. **JuggleIM** is a complete, production-grade IM platform with server, admin and multi-platform clients. This repository hosts the **official Android SDK** that lets you ship chat features in minutes — without giving up control of your protocol, your data or your infrastructure.

- Custom binary protocol — Protobuf over WebSocket, optimized for low latency and small payload.
- Auto-reconnect — Survives flaky networks with offline messages, ACK and retransmission.
- Composable architecture — One unified client API for chat, group, chatroom, moments and RTC signaling.

## ✨ Features

- Core IM capabilities: connection management, reconnect, conversations, message send/receive, history, unread counts.
- Message types: text, image, file, custom messages, and business notification messages.
- Chat scenarios: one-to-one chat, group chat, chatroom, and live chatroom.
- Extensions: user profile, moments, audio/video calls, and message upload.
- Push plugins: Google FCM, Huawei, Xiaomi, OPPO, VIVO, Honor, and JPush.
- Call plugins: Zego, Agora, and LiveKit.
- Security: end-to-end encryption related capabilities are available in recent versions.
- Demo app: a runnable Android demo is included for login, conversation, messaging, and call flows.

## 🚀 Quick Start

### Install

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

Add Juggle dependency in your app's build.gradle:

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

### Initialize

Initialize the SDK in your `Application`:

```java
import com.juggle.im.JIM;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        List<String> serverUrls = new ArrayList<>();
        serverUrls.add("wss://your-im-server");
        JIM.getInstance().setServerUrls(serverUrls);
        JIM.getInstance().init(this, "your_app_key");
    }
}
```

### Connect & send a message

Connect with an IM token issued by your application server:

```java
JIM.getInstance().getConnectionManager().connect("user_im_token");
```

> Manage user tokens on your server side. Do not hard-code production secrets in client applications or public repositories.

Send a text message:

```java
TextMessage text = new TextMessage("Hello from JuggleIM 👋");
Conversation conversation = new Conversation(Conversation.ConversationType.PRIVATE, "TARGET_USER_ID");
IMessageManager.ISendMessageCallback callback = new IMessageManager.ISendMessageCallback() {
    @Override
    public void onSuccess(Message message) {
    }

    @Override
    public void onError(Message message, int errorCode) {
    }
};
Message message = JIM.getInstance().getMessageManager().sendMessage(text, conversation, callback);
```

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

## 📚 Documentation

- Official docs: <https://www.juggle.im/>

## 🌱 Ecosystem

| Project                                                      | Description                 |
|--------------------------------------------------------------|-----------------------------|
| [im-web-sdk](https://github.com/Juggleim/im-web-sdk)         | Web SDK                     |
| [im-server](https://github.com/Juggleim/im-server)           | Self-hosted IM backend      |
| [im-admin](https://github.com/Juggleim/im-admin)             | Admin console               |
| [im-android-sdk](https://github.com/Juggleim/im-android-sdk) | **This repo** — Android SDK |
| [im-ios-sdk](https://github.com/Juggleim/im-ios-sdk)         | iOS SDK                     |
| [web-im-demo](https://github.com/Juggleim/web-im-demo)       | React/Vue integration demo  |

## 🤝 Contributing

We love contributions! Whether it's:

- Reporting a bug via [Issues](https://github.com/Juggleim/im-android-sdk/issues)
- Improving docs or examples
- Sending a [Pull Request](https://github.com/Juggleim/im-android-sdk/pulls)

Please read **[CONTRIBUTING.md](./CONTRIBUTING.md)** first.

## 💬 Community

Have questions or want to chat with other JuggleIM developers? Join us:

- Telegram Group: <https://t.me/juggleim_zh>


## 📄 License

Copyright © JuggleIM. Licensed under the **[Apache License 2.0](./LICENSE)**.

---

<sub>Built with ❤️ by the JuggleIM team and contributors.</sub>