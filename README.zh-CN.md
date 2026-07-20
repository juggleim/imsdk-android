<div align="center">
  <img src="demo/src/main/res/logo.png" alt="JuggleIM" width="120" />

  <h1>JuggleIM Android SDK</h1>

  <p><strong>面向 Android 的高性能开源实时消息 SDK。</strong><br/>
  基于 WebSocket 之上的自定义二进制协议构建，开箱即支持聊天、群组、直播聊天室、RTC 信令和朋友圈/动态能力。</p>

  <p>
    <a href="#-功能特性">功能特性</a> ·
    <a href="#-快速开始">快速开始</a> ·
    <a href="#-文档">文档</a> ·
    <a href="#-生态">生态</a> ·
    <a href="#-社区">社区</a>
  </p>

  <p>
    <a href="./README.md">English</a> | 简体中文
  </p>
</div>

---

## 为什么选择 JuggleIM？

从零构建可靠、可扩展的实时消息系统并不容易。**JuggleIM** 提供完整的生产级 IM 平台，包含服务端、管理后台和多端客户端。本仓库提供 **官方 Android SDK**，让你无需放弃协议、数据和基础设施控制权，就能在几分钟内接入聊天能力。

- 自定义二进制协议 - 基于 WebSocket 的 Protobuf 传输，针对低延迟和小体积优化。
- 自动重连 - 在网络不稳定时仍能通过离线消息、ACK 和重传机制保持连接可靠。
- 可组合架构 - 统一的客户端 API，覆盖聊天、群组、聊天室、朋友圈/动态和 RTC 信令。

## ✨ 功能特性

- 核心 IM 能力：连接管理、重连、会话、消息收发、历史消息、未读数。
- 消息类型：文本、图片、文件、自定义消息，以及业务通知消息。
- 聊天场景：单聊、群聊、聊天室、直播聊天室。
- 扩展能力：用户资料、朋友圈/动态、音视频通话、消息上传。
- 推送插件：Google FCM、华为、小米、OPPO、VIVO、荣耀和极光推送。
- 通话插件：Zego、Agora 和 LiveKit。
- 安全能力：近期版本已提供端到端加密相关能力。
- Demo 应用：仓库内包含可运行的 Android Demo，可直接验证登录、会话、消息和通话流程。

## 🚀 快速开始

### 安装

在 `settings.gradle` 或根 Gradle 文件中添加 JuggleIM Maven 仓库：

```gradle
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url "https://repo.juggle.im/repository/maven-releases/" }
    }
}
```

在应用的 `build.gradle` 中添加 Juggle 依赖：

```gradle
dependencies {
    implementation "com.juggle.im:juggle:1.9.0"
}
```

按需添加插件：

```gradle
dependencies {
    implementation "com.juggle.push.jg:juggle:1.9.0"
    implementation "com.juggle.call.zego:juggle:1.9.0"
    implementation "com.juggle.call.agora:juggle:1.9.0"
    implementation "com.juggle.call.livekit:juggle:1.9.0"
}
```

### 初始化

在 `Application` 中初始化 SDK：

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

### 连接并发送消息

使用业务服务端签发的 IM token 连接：

```java
JIM.getInstance().getConnectionManager().connect("user_im_token");
```

> 请在服务端管理用户 token。不要把生产环境密钥硬编码到客户端应用或公开仓库中。

发送文本消息：

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

## 仓库结构

| 目录 | 说明 |
| --- | --- |
| `JuggleIM` | 核心 IM SDK |
| `JetIMKit` | UI Kit 模块 |
| `demo` | Android Demo 应用 |
| `GooglePlugin` | Google FCM 推送插件 |
| `HWPlugin` | 华为推送插件 |
| `XMPlugin` | 小米推送插件 |
| `OPPOPlugin` | OPPO 推送插件 |
| `VIVOPlugin` | VIVO 推送插件 |
| `HonorPlugin` | 荣耀推送插件 |
| `JGPlugin` | 极光推送插件 |
| `JZegoCall` | Zego 通话插件 |
| `JAgoraCall` | Agora 通话插件 |
| `JLiveKitCall` | LiveKit 通话插件 |

## 运行 Demo

1. 使用 Android Studio 打开本仓库。
2. 确保 JDK 17、Android Gradle Plugin 和 Android SDK 可用。
3. 运行 `demo` 或 `app` 模块。
4. 根据你的环境配置 `appKey`、服务地址、推送厂商凭证和通话厂商凭证。

Demo 初始化示例：

- `demo/src/main/java/com/juggle/chat/BaseApplication.kt`
- `demo/src/main/java/com/juggle/chat/LoginActivity.kt`

## 📚 文档

- 官方文档：<https://www.juggle.im/>

## 🌱 生态

| 项目 | 说明 |
| --- | --- |
| [im-web-sdk](https://github.com/Juggleim/im-web-sdk) | Web SDK |
| [im-server](https://github.com/Juggleim/im-server) | 自托管 IM 后端 |
| [im-admin](https://github.com/Juggleim/im-admin) | 管理控制台 |
| [im-android-sdk](https://github.com/Juggleim/im-android-sdk) | **本仓库** - Android SDK |
| [im-ios-sdk](https://github.com/Juggleim/im-ios-sdk) | iOS SDK |
| [web-im-demo](https://github.com/Juggleim/web-im-demo) | React/Vue 集成示例 |

## 🤝 贡献

欢迎贡献！你可以通过以下方式参与：

- 通过 [Issues](https://github.com/Juggleim/im-android-sdk/issues) 报告问题
- 改进文档或示例
- 提交 [Pull Request](https://github.com/Juggleim/im-android-sdk/pulls)

请先阅读 **[CONTRIBUTING.md](./CONTRIBUTING.md)**。

## 💬 社区

有问题或想和其他 JuggleIM 开发者交流？欢迎加入：

- Telegram Group: <https://t.me/juggleim_zh>

## 📄 许可证

Copyright © JuggleIM。基于 **[Apache License 2.0](./LICENSE)** 许可发布。

---

<sub>由 JuggleIM 团队和贡献者共同打造。</sub>
