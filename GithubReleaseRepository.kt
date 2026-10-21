package com.example.appspace

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.Base64
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

/** A downloadable APK asset attached to a public GitHub Release. */
data class GithubReleaseAsset(
    val name: String,
    val downloadUrl: String,
    val sizeBytes: Long
)

data class GithubRelease(
    val tagName: String,
    val title: String,
    val body: String,
    val publishedAt: String,
    val htmlUrl: String,
    val isPrerelease: Boolean,
    val apkAssets: List<GithubReleaseAsset>
)

data class GithubAppDetails(
    val fullName: String,
    val htmlUrl: String,
    val description: String,
    val ownerLogin: String,
    val homepage: String,
    val license: String,
    val stars: Int,
    val topics: List<String>,
    val releases: List<GithubRelease>,
    /** Useful screenshots extracted from the repository README (badges are excluded). */
    val readmeScreenshotUrls: List<String> = emptyList()
)

data class GithubSearchResponse(
    val apps: List<FdroidApp>,
    val error: String? = null
)

/**
 * First-stage GitHub Releases source adapter.
 * Only public repositories with at least one Release APK are returned as installable apps.
 * Anonymous API limits apply; short in-memory caches avoid repeating the same requests.
 */
object GithubReleaseRepository {
    private const val API_ROOT = "https://api.github.com"
    private const val SEARCH_CACHE_MS = 5 * 60 * 1000L
    private const val RELEASE_CACHE_MS = 10 * 60 * 1000L
    private const val DETAILS_CACHE_MS = 10 * 60 * 1000L
    private const val ICON_CACHE_MS = 12 * 60 * 60 * 1000L
    private const val ICON_MISS_CACHE_MS = 10 * 60 * 1000L
    private const val MAX_ICON_APK_BYTES = 120L * 1024L * 1024L
    private const val MAX_REPOSITORIES_TO_CHECK = 4
    // Anonymous GitHub Search API is rate-limited, so space distinct live searches apart.
    private const val MIN_SEARCH_INTERVAL_MS = 6500L

    private data class TimedValue<T>(val createdAt: Long, val value: T)
    private val searchCache = ConcurrentHashMap<String, TimedValue<GithubSearchResponse>>()
    private val releaseCache = ConcurrentHashMap<String, TimedValue<List<GithubRelease>>>()
    private val detailsCache = ConcurrentHashMap<String, TimedValue<GithubAppDetails>>()
    // Icon discovery is lazy (only for visible rows) and cached separately, so it never delays search results.
    private val iconCache = ConcurrentHashMap<String, TimedValue<String>>()
    private val iconLocks = ConcurrentHashMap<String, Mutex>()
    // Persist icon PNGs internally. At most two APKs are downloaded concurrently for icon extraction.
    private val apkIconLocks = ConcurrentHashMap<String, Mutex>()
    private val apkIconSemaphore = Semaphore(2)
    private val searchMutex = Mutex()
    @Volatile private var lastSearchStartedAt = 0L

    suspend fun search(query: String): GithubSearchResponse = withContext(Dispatchers.IO) {
        val term = query.trim()
        if (term.isBlank()) return@withContext GithubSearchResponse(emptyList())

        val cacheKey = term.lowercase()
        searchCache[cacheKey]?.takeIf { System.currentTimeMillis() - it.createdAt < SEARCH_CACHE_MS }
            ?.let { return@withContext it.value }

        try {
            searchMutex.lock()
            try {
                searchCache[cacheKey]?.takeIf { System.currentTimeMillis() - it.createdAt < SEARCH_CACHE_MS }
                    ?.let { return@withContext it.value }
                val waitMs = MIN_SEARCH_INTERVAL_MS - (System.currentTimeMillis() - lastSearchStartedAt)
                if (lastSearchStartedAt > 0L && waitMs > 0L) delay(waitMs)
                lastSearchStartedAt = System.currentTimeMillis()
                return@withContext performSearch(term, cacheKey)
            } finally {
                searchMutex.unlock()
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            GithubSearchResponse(emptyList(), friendlyError(error))
        }
    }

    private suspend fun performSearch(term: String, cacheKey: String): GithubSearchResponse {
        return try {
            val encoded = URLEncoder.encode("$term in:name,description,topics", Charsets.UTF_8.name())
            val json = requestJson("$API_ROOT/search/repositories?q=$encoded&sort=stars&order=desc&per_page=12")
            val items = json.optJSONArray("items") ?: JSONArray()
            val candidates = buildList {
                for (i in 0 until items.length()) {
                    val repo = items.optJSONObject(i) ?: continue
                    if (repo.optBoolean("archived", false) || repo.optBoolean("disabled", false)) continue
                    val fullName = repo.optString("full_name").trim()
                    if (fullName.count { it == '/' } != 1) continue
                    add(repo)
                }
            }.take(MAX_REPOSITORIES_TO_CHECK)

            val releases = coroutineScope {
                candidates.map { repo ->
                    async(Dispatchers.IO) {
                        val fullName = repo.optString("full_name")
                        val found = try {
                            getReleasesCached(fullName, 5)
                        } catch (error: Exception) {
                            // A quota response should reach the caller; isolated repo errors may be skipped.
                            if (error.message.orEmpty().contains("匿名访问限额")) throw error
                            emptyList()
                        }
                        val releaseWithApk = found.firstOrNull { it.apkAssets.isNotEmpty() && !it.isPrerelease }
                            ?: found.firstOrNull { it.apkAssets.isNotEmpty() }
                        repo to releaseWithApk
                    }
                }.awaitAll()
            }

            val apps = releases.mapNotNull { (repo, latestWithApk) ->
                val release = latestWithApk ?: return@mapNotNull null
                val fullName = repo.optString("full_name").trim()
                if (fullName.isBlank()) return@mapNotNull null
                val repoName = repo.optString("name").ifBlank { fullName.substringAfter('/') }
                val description = repo.optString("description", "").trim()
                val iconAsset = chooseIconAsset(release)
                FdroidApp(
                    packageName = "github:${fullName.lowercase()}",
                    name = repoName,
                    summary = description.ifBlank { "GitHub 公开项目，提供 APK Release" },
                    // Never use the repository owner's avatar as an application icon.
                    // The actual icon is extracted from this release APK and persisted by app identity.
                    iconUrl = "",
                    source = AppCatalogSource.GITHUB,
                    sourceId = fullName,
                    sourceUrl = repo.optString("html_url", "https://github.com/$fullName"),
                    sourceDefaultBranch = repo.optString("default_branch", ""),
                    sourceIconDownloadUrl = iconAsset?.downloadUrl.orEmpty(),
                    sourceIconVersionKey = "${release.tagName}|${release.publishedAt}|${iconAsset?.downloadUrl.orEmpty()}|${iconAsset?.sizeBytes ?: 0L}",
                    sourceIconAssetSizeBytes = iconAsset?.sizeBytes ?: 0L,
                    developer = repo.optJSONObject("owner")?.optString("login").orEmpty(),
                    apkSizeBytes = release.apkAssets.maxOfOrNull { it.sizeBytes }
                )
            }.distinctBy { it.sourceId.lowercase() }

            GithubSearchResponse(apps).also {
                searchCache[cacheKey] = TimedValue(System.currentTimeMillis(), it)
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            GithubSearchResponse(emptyList(), friendlyError(error))
        }
    }

    private fun chooseIconAsset(release: GithubRelease): GithubReleaseAsset? {
        val eligible = release.apkAssets.filter {
            it.downloadUrl.startsWith("https://") &&
                    (it.sizeBytes <= 0L || it.sizeBytes <= MAX_ICON_APK_BYTES)
        }
        fun priority(asset: GithubReleaseAsset): Int {
            val name = asset.name.lowercase()
            return when {
                "universal" in name || "-all" in name || "_all" in name -> 0
                "arm64-v8a" in name || "arm64" in name -> 1
                "release" in name && "split" !in name && "config" !in name -> 2
                "split" in name || "config." in name || "config-" in name -> 8
                else -> 4
            }
        }
        // Icon resources are usually identical across variants; choose the smallest non-config APK
        // first to minimize one-time bandwidth. Asset naming is only a tie-breaker.
        return eligible.minWithOrNull(compareBy<GithubReleaseAsset>({ if (it.sizeBytes > 0L) it.sizeBytes else Long.MAX_VALUE }, { priority(it) }))
            ?: release.apkAssets.filter { it.downloadUrl.startsWith("https://") }
                .minByOrNull { if (it.sizeBytes > 0L) it.sizeBytes else Long.MAX_VALUE }
    }

    /**
     * Returns the previously cached PNG for a repository immediately, even if its latest APK changed.
     * The caller can show the old icon while [getCachedApkIconPath] refreshes it in the background.
     */
    fun getExistingCachedAppIconPath(context: Context, fullName: String): String {
        val safeName = fullName.trim()
        if (!safeName.matches(Regex("[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+"))) return ""
        val iconFile = File(File(context.applicationContext.filesDir, "github_app_icon_cache"), "${sha256(safeName.lowercase())}.png")
        return iconFile.absolutePath.takeIf { iconFile.isFile && iconFile.length() > 0L }.orEmpty()
    }

    /**
     * Downloads one selected APK only when its asset/version key is not cached, extracts the real
     * Android launcher icon with PackageManager, stores only the PNG, then deletes the temporary APK.
     * A release update invalidates only that repository's cache entry; unchanged apps are untouched.
     */
    suspend fun getCachedApkIconPath(
        context: Context,
        fullName: String,
        assetUrl: String,
        versionKey: String,
        expectedSizeBytes: Long = 0L
    ): String = withContext(Dispatchers.IO) {
        val safeName = fullName.trim()
        val safeUrl = assetUrl.trim()
        if (!safeName.matches(Regex("[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+")) ||
            !safeUrl.startsWith("https://") || versionKey.isBlank()) return@withContext ""
        if (expectedSizeBytes > MAX_ICON_APK_BYTES) {
            return@withContext getExistingCachedAppIconPath(context, safeName)
        }

        val appContext = context.applicationContext
        val root = File(appContext.filesDir, "github_app_icon_cache")
        if (!root.exists() && !root.mkdirs()) return@withContext getExistingCachedAppIconPath(appContext, safeName)
        val stem = sha256(safeName.lowercase())
        val iconFile = File(root, "$stem.png")
        val metadataFile = File(root, "$stem.meta")
        val expectedKey = "$versionKey\n$safeUrl"
        fun isCurrent(): Boolean = iconFile.isFile && iconFile.length() > 0L &&
                runCatching { metadataFile.readText(Charsets.UTF_8) == expectedKey }.getOrDefault(false)
        fun decoratedPath(): String = "${iconFile.absolutePath}#${sha256(expectedKey).take(12)}"
        if (isCurrent()) return@withContext decoratedPath()

        val lock = apkIconLocks.getOrPut(stem) { Mutex() }
        lock.lock()
        try {
            if (isCurrent()) return@withContext decoratedPath()
            try {
                apkIconSemaphore.acquire()
                val extracted = try {
                    extractIconFromApk(appContext, safeUrl, expectedSizeBytes, stem)
                } finally {
                    apkIconSemaphore.release()
                }
                if (extracted != null) {
                    val tempPng = File(root, "$stem.png.tmp")
                    FileOutputStream(tempPng).use { stream ->
                        if (!extracted.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
                            extracted.recycle()
                            throw IllegalStateException("无法保存 APK 应用图标")
                        }
                        stream.flush()
                    }
                    if (iconFile.exists() && !iconFile.delete()) {
                        extracted.recycle()
                        throw IllegalStateException("无法更新应用图标缓存")
                    }
                    if (!tempPng.renameTo(iconFile)) {
                        tempPng.copyTo(iconFile, overwrite = true)
                        tempPng.delete()
                    }
                    val metaTemp = File(root, "$stem.meta.tmp")
                    metaTemp.writeText(expectedKey, Charsets.UTF_8)
                    if (metadataFile.exists()) metadataFile.delete()
                    if (!metaTemp.renameTo(metadataFile)) {
                        metaTemp.copyTo(metadataFile, overwrite = true)
                        metaTemp.delete()
                    }
                    extracted.recycle()
                    return@withContext decoratedPath()
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                // The UI can fall back to a trusted launcher image or a neutral placeholder.
            }
            // If the new APK cannot be parsed, retain the previous cached icon rather than replacing it with a wrong image.
            return@withContext getExistingCachedAppIconPath(appContext, safeName)
        } finally {
            lock.unlock()
        }
    }

    private suspend fun extractIconFromApk(
        context: Context,
        assetUrl: String,
        expectedSizeBytes: Long,
        stem: String
    ): Bitmap? = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        val tempApk = File(context.cacheDir, "github-icon-$stem.apk.part")
        try {
            connection = (URL(assetUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 12000
                readTimeout = 60000
                instanceFollowRedirects = true
                setRequestProperty("Accept", "application/octet-stream")
                setRequestProperty("User-Agent", "AppSpace-Android-App-Catalog")
            }
            val status = connection.responseCode
            if (status !in 200..299) return@withContext null
            val contentLength = connection.contentLengthLong
            if (contentLength > MAX_ICON_APK_BYTES || expectedSizeBytes > MAX_ICON_APK_BYTES) return@withContext null
            var total = 0L
            connection.inputStream.use { input ->
                FileOutputStream(tempApk).use { output ->
                    val buffer = ByteArray(16 * 1024)
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        total += read
                        if (total > MAX_ICON_APK_BYTES) throw IllegalStateException("APK 太大，跳过图标解析")
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                }
            }
            if (total == 0L) return@withContext null
            val packageInfo = context.packageManager.getPackageArchiveInfo(
                tempApk.absolutePath,
                PackageManager.GET_ACTIVITIES or PackageManager.GET_META_DATA
            ) ?: return@withContext null
            val applicationInfo = packageInfo.applicationInfo ?: return@withContext null
            applicationInfo.sourceDir = tempApk.absolutePath
            applicationInfo.publicSourceDir = tempApk.absolutePath
            val drawable = runCatching { applicationInfo.loadIcon(context.packageManager) }.getOrNull() ?: return@withContext null
            drawableToBitmap(drawable, 256)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            null
        } finally {
            connection?.disconnect()
            tempApk.delete()
        }
    }

    private fun drawableToBitmap(drawable: Drawable, size: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: size
        val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: size
        val scale = minOf(size.toFloat() / width.toFloat(), size.toFloat() / height.toFloat())
        val drawWidth = (width * scale).toInt().coerceAtLeast(1)
        val drawHeight = (height * scale).toInt().coerceAtLeast(1)
        val left = (size - drawWidth) / 2
        val top = (size - drawHeight) / 2
        val canvas = Canvas(bitmap)
        drawable.setBounds(left, top, left + drawWidth, top + drawHeight)
        drawable.draw(canvas)
        return bitmap
    }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

    /** Downloads README metadata separately from the APK and exposes its illustrations as detail screenshots. */
    private suspend fun getReadmeScreenshotUrls(fullName: String, defaultBranch: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val readmeJson = requestJson("$API_ROOT/repos/$fullName/readme")
            val encoding = readmeJson.optString("encoding", "")
            val content = readmeJson.optString("content", "")
            val markdown = if (encoding == "base64" && content.isNotBlank()) {
                String(Base64.decode(content, Base64.DEFAULT), Charsets.UTF_8)
            } else {
                val rawUrl = readmeJson.optString("download_url", "")
                if (!rawUrl.startsWith("https://")) "" else requestText(rawUrl)
            }
            parseReadmeImageUrls(markdown, fullName, defaultBranch)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parseReadmeImageUrls(markdown: String, fullName: String, defaultBranch: String): List<String> {
        if (markdown.isBlank()) return emptyList()
        val candidates = LinkedHashSet<String>()
        val markdownImage = Regex("""!\[[^\]]*\]\((?:<([^>]+)>|([^\s)]+))(?:\s+[^)]*)?\)""")
        for (match in markdownImage.findAll(markdown)) {
            val target = match.groupValues.getOrNull(1).orEmpty().ifBlank { match.groupValues.getOrNull(2).orEmpty() }
            resolveReadmeImageUrl(target, fullName, defaultBranch)?.let(candidates::add)
        }
        val htmlImage = Regex("""(?is)<img\b[^>]*?\bsrc\s*=\s*["']([^"']+)["'][^>]*>""")
        for (match in htmlImage.findAll(markdown)) {
            resolveReadmeImageUrl(match.groupValues[1], fullName, defaultBranch)?.let(candidates::add)
        }
        return candidates.filter { isLikelyReadmeImage(it) }.take(8)
    }

    private fun resolveReadmeImageUrl(rawTarget: String, fullName: String, defaultBranch: String): String? {
        var target = rawTarget.trim().trim('<', '>', '\"', '\'').replace("&amp;", "&")
        if (target.isBlank() || target.startsWith("data:", true)) return null
        if (target.startsWith("//")) target = "https:$target"
        if (target.startsWith("https://", true) || target.startsWith("http://", true)) {
            if (!target.startsWith("https://", true)) return null
            val prefix = "https://github.com/$fullName/blob/"
            if (target.startsWith(prefix, true)) {
                val tail = target.removePrefix(prefix).substringBefore('?').substringBefore('#')
                val slash = tail.indexOf('/')
                if (slash > 0) {
                    val branchPart = tail.substring(0, slash)
                    val path = tail.substring(slash + 1)
                    return buildRawGithubUrl(fullName, branchPart.ifBlank { defaultBranch }, path)
                }
            }
            return target.substringBefore('#')
        }
        val cleaned = target.substringBefore('#').substringBefore('?').removePrefix("./").removePrefix("/")
        if (cleaned.isBlank() || cleaned.split('/').any { it == ".." }) return null
        return buildRawGithubUrl(fullName, defaultBranch.ifBlank { "main" }, cleaned)
    }

    private fun isLikelyReadmeImage(url: String): Boolean {
        val lower = url.lowercase()
        if (listOf("shields.io", "badgen.net", "badge.svg", "/badge/", "github.com/actions/workflows").any { it in lower }) return false
        val path = runCatching { java.net.URI(url).path.orEmpty().lowercase() }.getOrDefault("")
        val imageExtension = listOf(".png", ".jpg", ".jpeg", ".webp", ".gif", ".avif").any { path.endsWith(it) }
        val knownGithubImageHost = lower.contains("user-images.githubusercontent.com") ||
                lower.contains("githubusercontent.com") || lower.contains("github.com/user-attachments/assets/")
        return imageExtension || knownGithubImageHost
    }

    /**
     * Finds a real app icon in the repository when the project exposes one.
     * GitHub Releases do not have a standardized app-icon field. Only canonical Android
     * launcher resources are trusted first; a known Fastlane/F-Droid listing icon is a fallback.
     * Arbitrary icon.png/logo.png, README art, screenshots and owner avatars are never used.
     * If we cannot confidently identify an app icon, return blank and let the UI show its neutral placeholder.
     */
    suspend fun getRepositoryIconUrl(fullName: String, defaultBranch: String): String = withContext(Dispatchers.IO) {
        val safeName = fullName.trim()
        val branch = defaultBranch.trim()
        if (!safeName.matches(Regex("[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+")) || branch.isBlank()) {
            return@withContext ""
        }
        val cacheKey = "${safeName.lowercase()}|$branch"
        fun cachedValue(): String? {
            val entry = iconCache[cacheKey] ?: return null
            val maxAge = if (entry.value.isBlank()) ICON_MISS_CACHE_MS else ICON_CACHE_MS
            return entry.value.takeIf { System.currentTimeMillis() - entry.createdAt < maxAge }
        }
        cachedValue()?.let { return@withContext it }

        val lock = iconLocks.getOrPut(cacheKey) { Mutex() }
        lock.lock()
        try {
            cachedValue()?.let { return@withContext it }
            val iconUrl = try {
                val encodedBranch = URLEncoder.encode(branch, Charsets.UTF_8.name())
                val treeJson = requestJson("$API_ROOT/repos/$safeName/git/trees/$encodedBranch?recursive=1")
                val tree = treeJson.optJSONArray("tree") ?: JSONArray()
                val candidatePaths = buildList {
                    for (i in 0 until tree.length()) {
                        val item = tree.optJSONObject(i) ?: continue
                        if (item.optString("type") != "blob") continue
                        val path = item.optString("path", "").trim()
                        if (path.isBlank()) continue
                        val score = trustedIconPathScore(path)
                        if (score < 10000) add(score to path)
                    }
                }
                val chosenPath = candidatePaths.minByOrNull { it.first }?.second.orEmpty()
                if (chosenPath.isBlank()) "" else buildRawGithubUrl(safeName, branch, chosenPath)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                ""
            }
            iconCache[cacheKey] = TimedValue(System.currentTimeMillis(), iconUrl)
            return@withContext iconUrl
        } finally {
            lock.unlock()
        }
    }

    /** Lower score wins. Unrelated repository imagery is intentionally ignored. */
    private fun trustedIconPathScore(path: String): Int {
        val normalized = path.lowercase().replace('\\', '/')
        val parts = normalized.split('/').filter { it.isNotBlank() }
        val file = parts.lastOrNull() ?: return 10000
        val ext = file.substringAfterLast('.', "")
        if (ext !in setOf("png", "webp", "jpg", "jpeg")) return 10000

        val blockedSegments = setOf("test", "tests", "androidtest", "benchmark", "sample", "samples", "demo", "demos", "docs", "doc", "website", "web", "screenshots", "screenshot", "preview", "mockup", "banner", "badges", "fixtures")
        if (parts.any { it in blockedSegments }) return 10000
        if (listOf("screenshot", "feature_graphic", "feature-graphic", "walkthrough", "diagram", "badge", "mockup", "foreground", "background").any { it in normalized }) return 10000

        val baseName = file.substringBeforeLast('.')
        val isRound = baseName in setOf("ic_launcher_round", "launcher_round")
        val isLauncherName = baseName in setOf("ic_launcher", "launcher", "app_icon", "appicon", "launcher_icon", "application_icon") || isRound

        // Strongest signal: a launcher-named image inside Android res/mipmap* or res/drawable*.
        val resIndex = parts.indexOf("res")
        if (resIndex >= 0 && resIndex + 2 < parts.size) {
            val resourceDir = parts[resIndex + 1]
            val launcherResourceDir = resourceDir == "mipmap" || resourceDir.startsWith("mipmap-") || resourceDir == "drawable" || resourceDir.startsWith("drawable-")
            if (launcherResourceDir && isLauncherName) {
                val modulePrefix = parts.take(resIndex).joinToString("/")
                val moduleScore = when {
                    modulePrefix == "app/src/main" -> 0
                    modulePrefix == "android/app/src/main" -> 2
                    modulePrefix == "src/main" -> 4
                    modulePrefix == "composeApp/src/androidMain" -> 5
                    modulePrefix.endsWith("/src/main") -> 8
                    else -> 20
                }
                val densityScore = when {
                    "xxxhdpi" in resourceDir -> 0
                    "xxhdpi" in resourceDir -> 10
                    "xhdpi" in resourceDir -> 20
                    "hdpi" in resourceDir -> 30
                    "mdpi" in resourceDir -> 40
                    "nodpi" in resourceDir -> 50
                    resourceDir == "mipmap" || resourceDir == "drawable" -> 60
                    "anydpi" in resourceDir -> 70
                    else -> 80
                }
                val fileScore = when {
                    baseName == "ic_launcher" -> 0
                    baseName == "launcher" -> 4
                    baseName == "app_icon" || baseName == "appicon" -> 8
                    baseName == "launcher_icon" || baseName == "application_icon" -> 12
                    isRound -> 100
                    else -> 200
                }
                val resourceTypeScore = if (resourceDir.startsWith("mipmap")) 0 else 100
                // Keep every recognized Android launcher resource ahead of the listing-icon fallback.
                return moduleScore * 100 + resourceTypeScore + densityScore + fileScore
            }
        }

        // Secondary fallback: only a standard F-Droid/Fastlane metadata image path.
        // We deliberately reject random files such as assets/icon.png or docs/logo.png.
        val isKnownListingIcon = baseName == "icon" && ext in setOf("png", "webp") &&
                (parts.firstOrNull() == "fastlane" || parts.firstOrNull() == "metadata") &&
                "android" in parts && "images" in parts && parts.indexOf("images") == parts.size - 2
        if (isKnownListingIcon) return 5000

        return 10000
    }

    private fun buildRawGithubUrl(fullName: String, branch: String, path: String): String {
        fun encodePart(value: String): String = URLEncoder.encode(value, Charsets.UTF_8.name()).replace("+", "%20")
        val owner = fullName.substringBefore('/')
        val repository = fullName.substringAfter('/')
        val encodedBranch = branch.split('/').joinToString("/") { encodePart(it) }
        val encodedPath = path.split('/').joinToString("/") { encodePart(it) }
        return if ('/' in branch) {
            "https://github.com/$owner/$repository/raw/refs/heads/$encodedBranch/$encodedPath"
        } else {
            "https://raw.githubusercontent.com/$owner/$repository/$encodedBranch/$encodedPath"
        }
    }

    suspend fun getAppDetails(fullName: String): Result<GithubAppDetails> = withContext(Dispatchers.IO) {
        val safeFullName = fullName.trim()
        if (!safeFullName.matches(Regex("[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+"))) {
            return@withContext Result.failure(IllegalArgumentException("无效的 GitHub 仓库名称"))
        }
        detailsCache[safeFullName.lowercase()]
            ?.takeIf { System.currentTimeMillis() - it.createdAt < DETAILS_CACHE_MS }
            ?.let { return@withContext Result.success(it.value) }

        try {
            val repo = requestJson("$API_ROOT/repos/$safeFullName")
            val defaultBranch = repo.optString("default_branch", "main").ifBlank { "main" }
            val (releases, readmeImages) = coroutineScope {
                val releasesDeferred = async(Dispatchers.IO) { getReleasesCached(safeFullName, 20) }
                val readmeDeferred = async(Dispatchers.IO) { getReadmeScreenshotUrls(safeFullName, defaultBranch) }
                releasesDeferred.await() to readmeDeferred.await()
            }
            val owner = repo.optJSONObject("owner")
            val licenseObject = repo.optJSONObject("license")
            val details = GithubAppDetails(
                fullName = repo.optString("full_name", safeFullName),
                htmlUrl = repo.optString("html_url", "https://github.com/$safeFullName"),
                description = repo.optString("description", "").trim(),
                ownerLogin = owner?.optString("login", "").orEmpty(),
                homepage = repo.optString("homepage", "").trim(),
                license = licenseObject?.optString("spdx_id", "")?.takeUnless { it.isBlank() || it == "NOASSERTION" }.orEmpty(),
                stars = repo.optInt("stargazers_count", 0),
                topics = buildList {
                    val topicsArray = repo.optJSONArray("topics") ?: JSONArray()
                    for (i in 0 until topicsArray.length()) {
                        topicsArray.optString(i).takeIf { it.isNotBlank() }?.let(::add)
                    }
                },
                releases = releases.filter { it.apkAssets.isNotEmpty() },
                readmeScreenshotUrls = readmeImages
            )
            detailsCache[safeFullName.lowercase()] = TimedValue(System.currentTimeMillis(), details)
            Result.success(details)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Result.failure(IllegalStateException(friendlyError(error), error))
        }
    }

    private suspend fun getReleasesCached(fullName: String, limit: Int): List<GithubRelease> = withContext(Dispatchers.IO) {
        val key = "${fullName.lowercase()}|$limit"
        releaseCache[key]?.takeIf { System.currentTimeMillis() - it.createdAt < RELEASE_CACHE_MS }
            ?.let { return@withContext it.value }
        val jsonArray = requestArray("$API_ROOT/repos/$fullName/releases?per_page=$limit")
        val releases = buildList {
            for (i in 0 until jsonArray.length()) {
                val json = jsonArray.optJSONObject(i) ?: continue
                if (json.optBoolean("draft", false)) continue
                val assetsJson = json.optJSONArray("assets") ?: JSONArray()
                val assets = buildList {
                    for (j in 0 until assetsJson.length()) {
                        val asset = assetsJson.optJSONObject(j) ?: continue
                        val name = asset.optString("name", "").trim()
                        val downloadUrl = asset.optString("browser_download_url", "").trim()
                        // Only direct APK files can be installed with Android's package installer.
                        if (!name.endsWith(".apk", ignoreCase = true) || !downloadUrl.startsWith("https://")) continue
                        add(GithubReleaseAsset(name, downloadUrl, asset.optLong("size", 0L)))
                    }
                }
                add(
                    GithubRelease(
                        tagName = json.optString("tag_name", ""),
                        title = json.optString("name", "").ifBlank { json.optString("tag_name", "未命名版本") },
                        body = json.optString("body", "").trim(),
                        publishedAt = json.optString("published_at", ""),
                        htmlUrl = json.optString("html_url", ""),
                        isPrerelease = json.optBoolean("prerelease", false),
                        apkAssets = assets
                    )
                )
            }
        }.sortedByDescending { it.publishedAt }
        releaseCache[key] = TimedValue(System.currentTimeMillis(), releases)
        releases
    }

    private fun requestJson(url: String): JSONObject {
        val body = requestText(url)
        return JSONObject(body)
    }

    private fun requestArray(url: String): JSONArray {
        val body = requestText(url)
        return JSONArray(body)
    }

    private fun requestText(url: String): String {
        var connection: HttpURLConnection? = null
        try {
            connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 9000
                readTimeout = 12000
                instanceFollowRedirects = true
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
                setRequestProperty("User-Agent", "AppSpace-Android-App-Catalog")
            }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (status !in 200..299) {
                if (status == 403 || status == 429) {
                    throw IllegalStateException("GitHub API 请求达到匿名访问限额，请稍后重试。")
                }
                if (status == 404) throw IllegalStateException("GitHub 仓库或 Release 不存在或不可公开访问。")
                throw IllegalStateException("GitHub API 返回 HTTP $status")
            }
            return body
        } finally {
            connection?.disconnect()
        }
    }

    private fun friendlyError(error: Throwable): String {
        val message = error.message.orEmpty()
        return when {
            message.contains("匿名访问限额") -> message
            message.contains("HTTP 5") -> "GitHub 服务暂时不可用，请稍后重试。"
            error is java.net.SocketTimeoutException -> "连接 GitHub 超时，请检查网络后重试。"
            error is java.net.UnknownHostException -> "无法连接 GitHub，请检查网络。"
            message.isNotBlank() -> message.take(180)
            else -> "GitHub Release 暂时不可用，请稍后重试。"
        }
    }
}