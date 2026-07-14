# JuggleIM Android SDK

[English](README) | 简体中文

JuggleIM Android SDK 是 JuggleIM 官方开源的 Android 即时通讯 SDK，提供稳定的长连接、会话、消息、聊天室、用户资料、推送和音视频通话扩展能力。它适合需要在 Android 应用中集成单聊、群聊、直播聊天室、系统通知、自定义消息和多厂商推送的团队。

JuggleIM 也提供其他平台和服务端项目：

| 项目                                                  | 说明                   |
|-----------------------------------------------------|----------------------|
| [im-server](https://github.com/juggleim/im-server)  | JuggleIM 服务端         |
| [imsdk-web](https://github.com/juggleim/imsdk-web)  | Web / JavaScript SDK |
| im-android-sdk                                      | Android SDK 和 Demo   |
| [im-ios-sdk](https://github.com/juggleim/imsdk-ios) | iOS SDK 和 Demo       |

## 功能特性

- IM 基础能力：连接、断线重连、会话列表、消息收发、历史消息、未读数。
- 消息类型：文本、图片、文件、自定义消息，以及业务通知类消息。
- 场景支持：单聊、群聊、聊天室、直播聊天室。
- 扩展模块：用户资料、朋友圈/动态、音视频通话、消息上传。
- 推送插件：Google FCM、华为、小米、OPPO、VIVO、荣耀、极光推送。
- 音视频插件：Zego、Agora、LiveKit。
- 安全能力：支持端到端加密相关能力，详见最新版本说明和源码实现。
- Demo 应用：仓库内包含可运行的 Android Demo，便于验证登录、会话、消息和通话流程。

## 快速开始

### 1. 添加 Maven 仓库

在项目的 `settings.gradle` 或根 `build.gradle` 中添加 JuggleIM Maven 仓库：

```gradle
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url "https://repo.juggle.im/repository/maven-releases/" }
    }
}
```

### 2. 添加 SDK 依赖

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

### 3. 初始化 SDK

在 `Application` 中初始化 SDK：

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

如果需要指定服务地址、日志或推送配置，可使用 `InitConfig`：

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

### 4. 连接用户

客户端需要使用业务服务端签发的 IM token 连接：

```java
JIM.getInstance().getConnectionManager().connect("user_im_token");
```

> `appKey` 和 `token` 应由你的业务服务端管理，不要把生产环境密钥硬编码到客户端或公开仓库。

## 仓库结构

| 目录 | 说明 |
| --- | --- |
| `JuggleIM` | IM 核心 SDK |
| `JetIMKit` | UI Kit 相关模块 |
| `demo` | Android Demo 应用 |
| `GooglePlugin` | Google FCM 推送插件 |
| `HWPlugin` | 华为推送插件 |
| `XMPlugin` | 小米推送插件 |
| `OPPOPlugin` | OPPO 推送插件 |
| `VIVOPlugin` | VIVO 推送插件 |
| `HonorPlugin` | 荣耀推送插件 |
| `JGPlugin` | 极光推送插件 |
| `JZegoCall` | Zego 音视频插件 |
| `JAgoraCall` | Agora 音视频插件 |
| `JLiveKitCall` | LiveKit 音视频插件 |

## 运行 Demo

1. 使用 Android Studio 打开本仓库。
2. 确认 JDK 17、Android Gradle Plugin 和 Android SDK 环境可用。
3. 选择 `demo` 或 `app` 模块运行。
4. 根据实际环境配置 `appKey`、服务地址、推送厂商参数和音视频厂商参数。

Demo 中的初始化示例可参考：

- `demo/src/main/java/com/juggle/chat/BaseApplication.kt`
- `demo/src/main/java/com/juggle/chat/LoginActivity.kt`

## 文档与社区

- 官网与文档：[https://www.juggle.im/](https://www.juggle.im/)
- Telegram 中文群：[https://t.me/juggleim_zh](https://t.me/juggleim_zh)
- Telegram English：[https://t.me/juggleim_en](https://t.me/juggleim_en)
- 微信群：可通过 [添加好友](https://downloads.juggleim.com/xiaoshan.jpg) 邀请加入

## 贡献

欢迎提交 Issue 和 Pull Request。开始前请阅读 [CONTRIBUTING.md](CONTRIBUTING.md)。

## License

JuggleIM Android SDK 使用 [Apache License 2.0](LICENSE) 开源协议。
