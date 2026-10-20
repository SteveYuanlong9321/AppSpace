# AppSpace 1.0 发布步骤

## 1. 启用官网（只需一次）

1. 打开仓库设置：https://github.com/SteveYuanlong9321/AppSpace/settings/pages
2. 在 **Build and deployment** 中将 **Source** 选择为 **GitHub Actions**。
3. 到 Actions 页面，打开 **Deploy AppSpace website**，点击 **Run workflow**。
4. 成功后网站地址为 https://steveyuanlong9321.github.io/AppSpace/。首次部署可能需要几分钟。

## 2. 把完整 Android Studio 工程上传到仓库

仓库目前的历史起点只有 README；官网与部署工作流已上传，但 Android Studio 完整工程还没在仓库里。请先确认本机项目可以打开并包含：

- settings.gradle.kts
- build.gradle.kts
- gradle/libs.versions.toml
- gradle/wrapper/gradle-wrapper.jar
- gradle/wrapper/gradle-wrapper.properties
- app/build.gradle.kts
- app/src/main/AndroidManifest.xml
- app/src/main/java/com/example/appspace/ 下的 Kotlin 源码
- app/src/main/res/ 下的图标、主题和 XML 资源

### 推荐上传方式

1. 在 Windows 安装 Git for Windows，并安装 GitHub CLI（gh）。
2. 打开 PowerShell，先完成一次登录：
   ```powershell
   gh auth login
   gh auth setup-git
   ```
3. 进入**完整的 AppSpace 项目根目录**（能够看到 settings.gradle.kts 和 app 文件夹）。
4. 下载仓库中的 `tools/publish-android-project.ps1` 到项目根目录。
5. 在 PowerShell 执行：
   ```powershell
   Set-ExecutionPolicy -Scope Process Bypass
   .\publish-android-project.ps1
   ```
6. 脚本会把工程复制到临时的仓库检出目录、排除本机配置/密钥/构建产物、显示待提交差异，并在确认后推送到 main。脚本不会上传 local.properties、key.properties、密钥库、APK/AAB 或本地构建目录。

如需更谨慎，先在 Android Studio 执行 **Build > Make Project**，确认没有编译错误，再运行脚本。

## 3. 1.0 正式发布前检查

- 从干净检出目录构建成功；检查 GitHub Actions 的构建日志。
- 在至少一台目标 Android 设备上验证启动、搜索、详情、截图、来源切换、下载和安装。
- 确认包名、版本号、签名证书与 Release 文件一致。
- 确认安装权限、Shizuku 授权说明、隐私说明及第三方来源限制。
- 准备正式 APK 与 SHA-256；只有实际产物经过验证后，再创建正式的 v1.0.0 Release。

**当前状态：** 官网文件和网站部署工作流已经提交；GitHub Pages 需要仓库所有者启用；完整 Android 工程与可下载 APK 尚未上传/发布。此文档刻意区分了这些状态，不把准备工作描述为已发布。
