# AppSpace Android project publisher
# Run this script from the FULL Android Studio project root.
# It excludes local machine configuration, signing secrets and build artifacts.
$ErrorActionPreference = "Stop"

$repoUrl = "https://github.com/SteveYuanlong9321/AppSpace.git"
$sourceRoot = (Get-Location).Path

$requiredFiles = @(
    "settings.gradle.kts",
    "build.gradle.kts",
    "gradle/libs.versions.toml",
    "gradle/wrapper/gradle-wrapper.jar",
    "gradle/wrapper/gradle-wrapper.properties",
    "app/build.gradle.kts",
    "app/src/main/AndroidManifest.xml"
)

$missing = @($requiredFiles | Where-Object { -not (Test-Path (Join-Path $sourceRoot $_)) })
if ($missing.Count -gt 0) {
    Write-Host "This does not look like a complete Android Studio project root." -ForegroundColor Red
    Write-Host "Missing required files:"
    $missing | ForEach-Object { Write-Host "  - $_" }
    Write-Host "Open PowerShell in the folder that contains settings.gradle.kts and app/."
    exit 2
}

$git = Get-Command git -ErrorAction SilentlyContinue
if (-not $git) {
    throw "Git was not found. Install Git for Windows first."
}

$tempRoot = Join-Path $env:TEMP ("AppSpace-publish-" + (Get-Date -Format "yyyyMMdd-HHmmss"))
$targetRoot = Join-Path $tempRoot "AppSpace"

Write-Host "Cloning the AppSpace GitHub repository into a temporary folder..." -ForegroundColor Cyan
git clone --branch main --single-branch $repoUrl $targetRoot
if ($LASTEXITCODE -ne 0) { throw "git clone failed. Check network access and GitHub authentication." }

Write-Host "Copying project source while preserving the website and repository workflow..." -ForegroundColor Cyan
$excludeDirs = @(
    ".git", ".gradle", ".idea", ".kotlin", "build", "captures",
    ".externalNativeBuild", ".cxx", "docs", ".github", "node_modules"
)
$excludeFiles = @(
    "local.properties", "key.properties", "secrets.properties",
    "README.md", "*.jks", "*.keystore", "*.apk", "*.aab", "*.apks",
    "*.pem", "*.p12", "*.pfx", "*.mobileprovision", "*.iml"
)

$robocopyArgs = @($sourceRoot, $targetRoot, "/E", "/R:1", "/W:1", "/NFL", "/NDL", "/NP", "/XD") + $excludeDirs + @("/XF") + $excludeFiles
& robocopy @robocopyArgs
$copyExit = $LASTEXITCODE
if ($copyExit -ge 8) { throw "Robocopy failed with exit code $copyExit." }

git -C $targetRoot add -A
if ($LASTEXITCODE -ne 0) { throw "git add failed." }

$changes = git -C $targetRoot status --short
if (-not $changes) {
    Write-Host "No source changes detected; nothing to upload." -ForegroundColor Yellow
    Write-Host "Temporary copy: $targetRoot"
    exit 0
}

Write-Host ""
Write-Host "Files staged for upload:" -ForegroundColor Cyan
$changes | ForEach-Object { Write-Host $_ }
Write-Host ""
Write-Host "Security exclusions applied: local.properties, key.properties, secrets.properties, signing keys, APK/AAB, and build folders."
$confirm = Read-Host "Commit these project files to AppSpace main and push to GitHub? Type YES to continue"
if ($confirm -cne "YES") {
    Write-Host "Cancelled. Temporary repository retained at: $targetRoot" -ForegroundColor Yellow
    exit 0
}

git -C $targetRoot -c user.name="AppSpace Project Upload" -c user.email="noreply@users.noreply.github.com" commit -m "feat: upload AppSpace Android Studio project for 1.0"
if ($LASTEXITCODE -ne 0) { throw "git commit failed." }

git -C $targetRoot push origin main
if ($LASTEXITCODE -ne 0) {
    throw "git push failed. Check gh auth login / gh auth setup-git and repository write permission."
}

Write-Host "AppSpace project source pushed successfully." -ForegroundColor Green
Write-Host "Temporary checkout: $targetRoot"
