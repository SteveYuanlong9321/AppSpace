# AppSpace · 应用空间

AppSpace 是一个面向 Android 的多来源应用发现与管理应用，目标是把应用搜索、详情、截图、来源选择和下载体验统一在一套干净的界面中。

**项目网站：** https://steveyuanlong9321.github.io/AppSpace/

## 当前状态

AppSpace 1.0 正在准备公开发布。当前已在开发版本中接入 F-Droid、GitHub Releases、华为 AppGallery、vivo 应用商店和 Uptodown；不同来源的搜索覆盖率、元数据与安装行为仍需要按设备和应用逐项验证。三星 Galaxy Store 已从项目中移除。

此仓库目前正在整理完整 Android Studio 工程、构建流程与首个正式 Release。网站与发布说明不会把尚未上传或验证的构建产物标记为已发布。

## 核心目标

- **统一搜索：** 在一个界面中搜索多个应用来源，按匹配程度自动排序。
- **统一详情：** 使用 AppSpace 自己的详情页展示应用信息、截图和来源。
- **来源可切换：** 同一 Android 包名的应用尽量合并，并保留每个可用下载来源。
- **隐私优先：** 当前设计不要求用户注册 AppSpace 账号；搜索和下载仍会连接对应的第三方来源。
- **谨慎安装：** APK 可用性、签名、系统兼容性与安装权限因来源和设备而异。

## 当前应用来源

- F-Droid
- GitHub Releases
- Huawei AppGallery
- Vivo App Store
- Uptodown

这些来源的匿名接口可能变化，某个来源的搜索或下载失败不代表其他来源不可用。搜索结果也不保证每个应用都有可用安装包。

## 安装与权限说明

AppSpace 的 Android 安装能力可能使用 Android 系统安装界面，也提供实验性的 Shizuku 辅助流程。Shizuku 需要用户在设备上主动配置与授权；项目不会保证它能绕过设备厂商或 Android 系统施加的所有限制。

首次公开发布之前，需要验证：
- Android Studio / Gradle 构建通过；
- 在目标 Android 版本上完成安装、搜索、下载、取消、安装失败及成功后的行为检查；
- 确认 APK 签名与 Release 校验信息；
- 为权限、隐私和第三方来源行为提供清楚说明。

## 隐私与安全

- 搜索会向所选数据源发送关键词或应用标识。
- 下载会连接应用来源或它提供的下载 CDN。
- 请只安装信任来源的 APK，并核对开发者、包名与签名。
- 请勿向 GitHub 提交 `local.properties`、密钥库、`key.properties`、访问令牌、密码或个人下载记录。
- 详细的使用说明与隐私信息请查看[项目网站](https://steveyuanlong9321.github.io/AppSpace/)。

## 反馈

请通过 [GitHub Issues](https://github.com/SteveYuanlong9321/AppSpace/issues) 报告问题。建议提供设备型号、Android 版本、AppSpace 版本、所用应用来源和复现步骤；分享日志前请移除令牌、账号及其他私人信息。

## English

AppSpace is an Android multi-source app discovery project. It aims to provide unified search, app details, screenshots, source selection and download flows across F-Droid, GitHub Releases, Huawei AppGallery, vivo App Store and Uptodown. The first public release is being prepared; source completeness, build success and device-level behavior must be verified before a release artifact is announced.
