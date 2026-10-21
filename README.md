# AppSpace · 应用空间

**AppSpace** 是一款面向 Android 的多来源应用发现与管理工具。它把多个第三方应用目录的搜索结果放到同一个界面中，帮助你查找应用、查看可用信息，并在不同来源之间选择下载渠道。

- **平台：** Android 8.0（API 26）及以上
- **当前版本：** 1.0
- **项目网站：** https://steveyuanlong9321.github.io/AppSpace/
- **源码仓库：** https://github.com/SteveYuanlong9321/AppSpace
- **发布下载：** https://github.com/SteveYuanlong9321/AppSpace/releases

## 功能

- **多来源搜索：** 从多个第三方目录查找 Android 应用。
- **相关性排序：** 尽量将与关键词最匹配的结果放在前面。
- **应用详情：** 汇总各来源可提供的应用信息、图标、版本与截图链接。
- **来源选择：** 在同一应用存在多个来源时，尽量保留并展示可用渠道。
- **下载与安装：** 使用来源提供的下载入口，并交由 Android 系统完成常规安装流程。
- **无需 AppSpace 账户：** 使用 AppSpace 不要求注册或登录本应用账户。

## 支持的应用来源

- F-Droid
- GitHub Releases
- Huawei AppGallery（华为应用市场）
- Vivo App Store（Vivo应用商店）
- Uptodown

来源接口、元数据和下载链接由第三方服务维护，可能随时变化。某个来源暂时无法搜索或下载，不代表其他来源也不可用。搜索结果不保证每个应用都提供可下载的 APK，实际可用性也可能因地区、设备、应用或来源政策而不同。

> **请注意：** AppSpace 内置 F-Droid 作为一个应用搜索来源，并不代表 AppSpace 本身已经上架 F-Droid。AppSpace 自身的 F-Droid 提交计划与审核状态会另行更新。

## 安装与权限

- AppSpace 需要网络连接来查询所选应用来源。
- 安装 APK 时，Android 可能要求你确认安装来源或授予“安装未知应用”权限。
- 项目还包含实验性的 Shizuku 辅助安装流程。该流程需要用户自行安装、启动并授权 Shizuku；它不是必需功能，也不保证能够绕过设备厂商或 Android 系统限制。
- 请仅安装你信任的来源所提供的应用。安装前请核对应用名称、开发者、包名和来源。

## 隐私说明

AppSpace 不要求用户登录 AppSpace 账户。搜索和详情请求会向相应的第三方应用来源发送查询词或应用标识；下载时，设备会连接所选来源或该来源使用的下载服务器。请同时查看相应第三方服务的隐私与使用条款。

## 从源码构建

项目使用 Gradle、Kotlin 和 Jetpack Compose。可以用 Android Studio 打开仓库根目录，也可以在已配置好 JDK 与 Android SDK 的环境中运行 Gradle Wrapper：

**Windows（PowerShell 或命令提示符）：**

```bat
gradlew.bat assembleDebug
```

**macOS / Linux：**

```bash
./gradlew assembleDebug
```

构建环境需要能够下载 Gradle、Android Gradle Plugin 和项目依赖。当前仓库使用 Android SDK 37 配置，最低支持版本为 Android 8.0（API 26）。上述构建命令是标准构建入口，并不代表本仓库已在所有干净环境中验证成功。

## 当前发布状态

AppSpace 1.0 的 GitHub Release 目前仍处于草稿状态，正式发布前仍需要完成构建环境验证、设备回归测试、APK 包名与签名核对，并核查第三方依赖与资源的许可证兼容性。GitHub Actions 之前在 Android SDK 初始化阶段失败，尚未成功运行到项目编译；自动构建暂时改为手动触发，修复环境后再验证。

## 开源许可证

AppSpace 项目代码采用 **Apache License 2.0（Apache-2.0）**，完整许可证条款见仓库根目录的 [`LICENSE`](LICENSE) 文件。

该许可证仅适用于 AppSpace 有权授权的项目内容；第三方依赖、图标及其他资源仍可能受各自的许可证和版权声明约束。提交 F-Droid 前，仍需逐项核查依赖、资源和构建方式是否符合 F-Droid 的政策。

## 反馈与问题报告

请通过 [GitHub Issues](https://github.com/SteveYuanlong9321/AppSpace/issues) 报告问题。建议附上设备型号、Android 版本、AppSpace 版本、所用来源和复现步骤。分享日志前，请先移除账户信息、令牌及其他私人数据。

---

## English

**AppSpace** is an Android app discovery and management app that combines search results from several third-party app catalogs in one interface.

### Features

- Search across F-Droid, GitHub Releases, Huawei AppGallery, vivo App Store, and Uptodown.
- Rank results by relevance where the available metadata allows it.
- View app details and source-provided metadata, icons, versions, and screenshot links.
- Choose between available sources for an app where matching is possible.
- Download from the selected source and use Android's installation flow.
- No AppSpace account or sign-in is required.

### Important notes

Third-party catalog interfaces, metadata, and download links can change. Availability may vary by region, device, app, and source policy. AppSpace currently integrates F-Droid as a search source; this does **not** mean AppSpace itself is listed on F-Droid.

An optional experimental Shizuku-assisted installation flow is included. It requires the user to set up and authorize Shizuku and may still be limited by Android or device-vendor restrictions.

Searches and detail requests are sent to the selected third-party source. Downloads connect to that source or its download infrastructure.

### Build from source

Open the repository root in Android Studio, or use the Gradle Wrapper in an environment with a configured JDK and Android SDK:

```bash
./gradlew assembleDebug
```

On Windows, use `gradlew.bat assembleDebug`. The project is configured for Android SDK 37 and supports Android 8.0 (API 26) and later. A successful clean build has not yet been confirmed by the current CI workflow.

### License and release status

The AppSpace 1.0 GitHub Release is still a draft. Build validation and device testing remain to be completed. AppSpace project code is licensed under Apache License 2.0 (Apache-2.0); see [`LICENSE`](LICENSE). Third-party dependency and asset licenses, as well as F-Droid build requirements, still need to be reviewed before submission.
