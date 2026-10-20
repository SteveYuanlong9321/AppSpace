AppSpace
一个统一发现、搜索和管理 Android 应用的多来源应用空间

将多个应用来源整合在同一套发现、搜索、详情和下载体验中，而不是为每个商店重复创建一套界面。

<sub>Android · Kotlin · Jetpack Compose · 持续开发中</sub>
</div>

项目简介
AppSpace 是一个面向 Android 的多来源应用发现与安装项目。目标是让用户在一个统一的界面中搜索应用、浏览分类、查看应用详情，并根据应用可用来源选择版本和下载方式。

AppSpace 目前仍处于开发阶段。功能、界面和数据源接入会持续调整；目前尚未发布正式 GitHub Release，部分功能仍需在不同 Android 设备和系统版本上验证。
设计目标
- 一个发现页、一个搜索页：所有接入的数据源共用同一套界面。
- 统一的应用详情：尽可能整合应用介绍、图标、截图、版本历史、更新说明和来源信息。
- 来源清晰可见：同一应用存在多个来源时，计划在一个详情页中查看并切换可用来源。
- 复用下载和安装流程：避免为每个应用源单独开发一套下载中心。
- 独立的数据源适配：一个来源发生故障时，尽可能不影响其他来源。
- 蓝色主题优先：默认使用 AppSpace 蓝色主题；提供可选的系统动态取色（Monet）方向。
- 谨慎处理 APK：安装失败或取消时应保留安装包；自动清理只针对 AppSpace 自己下载且确认安装成功的文件。
项目状态
模块	当前状态
F-Droid 搜索与应用目录	已接入，持续完善
分类浏览与子分类	已实现基础界面，结果匹配仍在完善
统一应用详情页	正在完善与验证
GitHub Releases 数据源	实验性接入，持续调试
多来源合并与来源切换	开发中，匹配准确度仍需验证
GitHub APK 图标提取与缓存	开发中，需在真实 APK 上验证
README 图片作为应用截图	开发中，图片可用性与过滤规则仍需完善
Shizuku / ADB 安装	实验性功能，依设备和系统环境验证
安装成功后自动清理下载的 APK	需验证安装结果判断与文件清理边界
关于页的 GitHub 更新检查	待正式 Release 发布后进行端到端验证
自定义应用仓库	暂不在当前阶段实现


表中的“已接入”不代表所有设备和所有应用均已通过完整测试。正式发布前会继续进行构建、安装和回归验证。

数据来源
F-Droid
AppSpace 已接入 F-Droid 搜索与应用目录数据。部分分类暂时使用关键词匹配来获取结果，因此分类结果可能与 F-Droid 官方元数据分类不完全一致。
GitHub Releases
AppSpace 正在接入 GitHub 公开仓库与 Releases，目标是展示具备可安装 APK 资产的项目，而不是把源码压缩包当成安装包。

GitHub 并不是专门的应用商店：部分仓库没有 Release、没有 APK，或只提供源代码。此类项目可能无法作为可安装应用显示。匿名 API 也存在速率限制，AppSpace 会逐步完善缓存、请求合并和错误提示。
安装与权限
正式 APK 尚未发布。项目完成公开源码整理并通过基本验证后，会在 GitHub Releases 发布版本和对应说明。

AppSpace 正在实验 Shizuku / ADB 风格的安装路径。该功能需要用户在设备上自行配置并授权 Shizuku；它不要求设备一定 Root，但并不保证能绕过所有 Android 系统、厂商 ROM、签名、兼容性或未来平台政策的限制。
隐私与安全说明
- 搜索、应用元数据和下载请求会连接相应的数据来源，例如 F-Droid 和 GitHub。
- 仅应从你信任的来源下载 APK，并确认应用签名、来源与版本信息。
- Shizuku 授权会授予 AppSpace 对应的高权限执行能力。请只在理解授权范围后启用。
- 请勿向仓库提交签名密钥、keystore / .jks 文件、key.properties、访问令牌、密码或其他私密配置。
- 详细隐私政策与权限说明会在正式发布前继续完善。
开发路线
1. 稳定统一详情页和 F-Droid 现有功能。
2. 完善 GitHub Releases 搜索、图标提取缓存、版本历史与 APK 资产筛选。
3. 提高跨来源应用匹配准确度，并验证来源切换和统一下载流程。
4. 完成 Shizuku 安装、安装后 APK 清理和应用内更新检查的端到端测试。
5. 整理公开源码、构建说明与发布流程，制作第一个 GitHub Release。
6. 后续再考虑自定义仓库管理。
路线会根据实际测试结果调整。
反馈与问题报告
遇到问题时，建议提供：
- Android 版本与设备型号；
- AppSpace 版本或所用提交；
- 涉及的数据源（F-Droid / GitHub）；
- 复现步骤、预期结果和实际结果；
- 如果有构建错误或崩溃，请提供第一条错误信息或相关日志。
提交日志前请删除访问令牌、账号信息、下载目录中的私人文件名等敏感信息。
当前仓库说明
此仓库正在建立中。目前只有项目说明文档，尚未包含完整 Android Studio 工程，也尚无正式 Release，因此暂时不能直接从此仓库构建或下载正式 APK。源码、构建步骤、版本签名与发布流程会随项目整理逐步补齐。请勿将开发中的功能描述视为稳定版承诺。
English
AppSpace is an Android project aiming to bring multiple app sources into one discovery, search, app-details, and download experience.
- F-Droid integration is connected and still being refined.
- GitHub Releases integration, cross-source matching, APK icon extraction/caching, and Shizuku-based installation are experimental and require further testing.
- The app defaults to a blue theme; optional Monet dynamic colors are being developed.
- No public GitHub Release is available yet. Source and build/release instructions are being prepared.
The project is under active development. Feature descriptions above are a roadmap and status overview, not a guarantee that every feature is production-ready.
