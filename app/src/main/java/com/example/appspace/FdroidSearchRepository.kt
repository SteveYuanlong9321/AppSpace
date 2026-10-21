package com.example.appspace

import android.text.Html
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/** Data-source identity for one installable copy of an application. */
enum class AppCatalogSource {
    F_DROID, GITHUB, HUAWEI_APPGALLERY, VIVO_APPSTORE, UPTODOWN;

    val displayName: String get() = when (this) {
        F_DROID -> "F-Droid"
        GITHUB -> "GitHub"
        HUAWEI_APPGALLERY -> "华为应用市场"
        VIVO_APPSTORE -> "vivo 应用商店"
        UPTODOWN -> "Uptodown"
    }
}

data class AppSourceVariant(
    val packageName: String,
    val name: String,
    val summary: String,
    val source: AppCatalogSource,
    val sourceId: String,
    val sourceUrl: String = "",
    val iconUrl: String = "",
    val sourceDefaultBranch: String = "",
    val sourceIconDownloadUrl: String = "",
    val sourceIconVersionKey: String = "",
    val sourceIconAssetSizeBytes: Long = 0L,
    /** Real package name learned from this catalog; blank if the search endpoint did not expose it. */
    val appPackageName: String = "",
    val developer: String = "",
    /** Current APK size for this source, in bytes; null when not reported. */
    val apkSizeBytes: Long? = null
) {
    val key: String get() = "${source.name}:${sourceId.trim().lowercase()}"
    val label: String get() = source.displayName
}

data class FdroidApp(
    val packageName: String,
    val name: String,
    val summary: String,
    val iconUrl: String = "",
    val source: AppCatalogSource = AppCatalogSource.F_DROID,
    val sourceId: String = packageName,
    val sourceUrl: String = "",
    /** GitHub default branch used only as a fallback when an APK icon cannot be extracted. */
    val sourceDefaultBranch: String = "",
    /** Preferred APK asset to inspect for the real application icon; empty for F-Droid apps. */
    val sourceIconDownloadUrl: String = "",
    /** Changes when the app's selected icon APK/release changes; used for per-app disk cache invalidation. */
    val sourceIconVersionKey: String = "",
    /** APK size selected for icon extraction, to avoid choosing oversized assets when alternatives exist. */
    val sourceIconAssetSizeBytes: Long = 0L,
    /** Real package name from the catalog; packageName remains the source-routing identity. */
    val appPackageName: String = "",
    val developer: String = "",
    /** Current APK size for this source, in bytes; null when not reported. */
    val apkSizeBytes: Long? = null,
    /** Other installable sources for this app, when search identifies the same application in multiple catalogs. */
    val alternateSources: List<AppSourceVariant> = emptyList()
)

fun FdroidApp.asSourceVariant(): AppSourceVariant = AppSourceVariant(
    packageName = packageName,
    name = name,
    summary = summary,
    source = source,
    sourceId = sourceId,
    sourceUrl = sourceUrl,
    iconUrl = iconUrl,
    sourceDefaultBranch = sourceDefaultBranch,
    sourceIconDownloadUrl = sourceIconDownloadUrl,
    sourceIconVersionKey = sourceIconVersionKey,
    sourceIconAssetSizeBytes = sourceIconAssetSizeBytes,
    appPackageName = appPackageName.ifBlank { packageName.takeIf { normalizedAndroidPackageName(it).isNotBlank() }.orEmpty() },
    developer = developer,
    apkSizeBytes = apkSizeBytes
)

fun FdroidApp.availableSourceVariants(): List<AppSourceVariant> =
    (listOf(asSourceVariant()) + alternateSources).distinctBy { it.key }

fun FdroidApp.forSourceVariant(variant: AppSourceVariant): FdroidApp = copy(
    packageName = variant.packageName,
    name = variant.name.ifBlank { name },
    summary = variant.summary,
    iconUrl = variant.iconUrl,
    source = variant.source,
    sourceId = variant.sourceId,
    sourceUrl = variant.sourceUrl,
    sourceDefaultBranch = variant.sourceDefaultBranch,
    sourceIconDownloadUrl = variant.sourceIconDownloadUrl,
    sourceIconVersionKey = variant.sourceIconVersionKey,
    sourceIconAssetSizeBytes = variant.sourceIconAssetSizeBytes,
    appPackageName = variant.appPackageName.ifBlank { variant.packageName.takeIf { normalizedAndroidPackageName(it).isNotBlank() }.orEmpty() },
    developer = variant.developer,
    apkSizeBytes = variant.apkSizeBytes,
    alternateSources = availableSourceVariants().filterNot { it.key == variant.key }
)

/** Real Android package name across any currently known catalog variant. */
fun FdroidApp.bestRealPackageName(): String = availableSourceVariants()
    .asSequence()
    .mapNotNull { variant ->
        normalizedAndroidPackageName(variant.appPackageName).takeIf { it.isNotBlank() }
            ?: normalizedAndroidPackageName(variant.packageName).takeIf { it.isNotBlank() }
    }
    .firstOrNull()
    .orEmpty()

/** Best available publisher name across all merged catalog variants. */
fun FdroidApp.bestDeveloperName(): String = availableSourceVariants()
    .asSequence().map { it.developer.trim() }.firstOrNull { it.isNotBlank() }.orEmpty()

/** First known size from the preferred source and its alternatives. */
fun FdroidApp.bestApkSizeBytes(): Long? = availableSourceVariants()
    .asSequence().mapNotNull { it.apkSizeBytes?.takeIf { bytes -> bytes > 0L } }.firstOrNull()

private fun realPackages(variant: AppSourceVariant): Set<String> = setOfNotNull(
    normalizedAndroidPackageName(variant.appPackageName).takeIf { it.isNotBlank() },
    normalizedAndroidPackageName(variant.packageName).takeIf { it.isNotBlank() }
).map { it.lowercase(Locale.ROOT) }.toSet()

/**
 * Merge duplicate results across stores.
 *
 * Matching order:
 * 1) same catalog identity;
 * 2) same real Android package name;
 * 3) curated cross-language aliases (e.g. 小红书 / Xiaohongshu / RedNote);
 * 4) conservative normalized-name matches for longer, distinctive names.
 *
 * Search results may use a synthetic source ID (huawei:<id>, vivo:<id>,
 * uptodown:<slug>) instead of the real package name. We must not treat those
 * synthetic IDs as package identities, and must not reject short Chinese names
 * solely because they contain fewer than five characters.
 */
fun mergeCatalogApps(input: List<FdroidApp>): List<FdroidApp> {
    // Normalize presentation names once at the shared boundary so every catalog uses
    // the same rules and each alternate source is clean when users switch channels.
    val cleanedInput = input.map { app ->
        val cleanedName = cleanAppDisplayName(app.name).ifBlank { app.name.trim() }
        app.copy(
            name = cleanedName,
            alternateSources = app.alternateSources.map { variant ->
                variant.copy(name = cleanAppDisplayName(variant.name).ifBlank { variant.name.trim() })
            }
        )
    }
    fun normalize(value: String): String = value.lowercase(Locale.ROOT)
        .replace(Regex("[^\\p{L}\\p{N}]"), "")

    val genericNames = setOf(
        "android", "app", "application", "browser", "calculator", "camera",
        "clock", "contacts", "files", "gallery", "launcher", "manager",
        "music", "notes", "photos", "player", "reader", "tools", "weather"
    )

    // These are product-name aliases, not query-ranking aliases. Only add
    // groups where the translated/store names identify the same app product.
    val crossStoreAliasGroups = listOf(
        setOf("小红书", "小紅書", "xiaohongshu", "rednote"),
        setOf("微信", "wechat", "weixin"),
        setOf("淘宝", "taobao"),
        setOf("支付宝", "alipay"),
        setOf("哔哩哔哩", "bilibili"),
        setOf("微博", "weibo"),
        setOf("美团", "meituan"),
        setOf("百度", "baidu"),
        setOf("京东", "jingdong"),
        setOf("快手", "kuaishou"),
        setOf("知乎", "zhihu"),
        setOf("酷安", "coolapk"),
        setOf("网易云音乐", "neteasecloudmusic", "neteasemusic", "netease music"),
        setOf("腾讯视频", "tencentvideo", "tencent video"),
        setOf("爱奇艺", "iqiyi"),
        setOf("优酷", "youku"),
        setOf("高德地图", "gaodemap", "amap"),
        setOf("拼多多", "pinduoduo"),
        setOf("饿了么", "eleme")
    )
    val aliasToCanonical = buildMap {
        crossStoreAliasGroups.forEachIndexed { index, group ->
            val canonical = "product-alias-$index"
            group.forEach { alias -> put(normalize(alias), canonical) }
        }
    }

    val suffixes = listOf("android", "application", "app", "mobile", "client", "fdroid", "official")

    fun identityAliases(app: FdroidApp): Set<String> {
        val variants = app.availableSourceVariants()
        val rawValues = buildList {
            variants.forEach { variant ->
                add(variant.name)
                if (variant.source == AppCatalogSource.GITHUB) {
                    add(variant.sourceId.substringAfterLast('/'))
                }
            }
        }
        val aliases = mutableSetOf<String>()
        rawValues.forEach { raw ->
            val value = normalize(raw)
            if (value.isBlank()) return@forEach

            // Known cross-language aliases can be short (notably Chinese names).
            aliasToCanonical[value]?.let { aliases += it }

            // Unknown names are only matched if long and distinctive enough.
            if (value.length >= 5 && value !in genericNames) aliases += "name:$value"

            suffixes.forEach { suffix ->
                if (value.endsWith(suffix) && value.length - suffix.length >= 5) {
                    val stem = value.removeSuffix(suffix)
                    if (stem !in genericNames) aliases += "name:$stem"
                    aliasToCanonical[stem]?.let { aliases += it }
                }
            }
        }
        return aliases
    }

    fun sameEntry(left: FdroidApp, right: FdroidApp): Boolean {
        val leftVariants = left.availableSourceVariants()
        val rightVariants = right.availableSourceVariants()
        if (leftVariants.any { lv -> rightVariants.any { rv ->
                lv.source == rv.source && lv.sourceId.equals(rv.sourceId, ignoreCase = true)
            } }) return true

        // Real Android package names are the strongest cross-store identity, even when
        // catalogs use different numeric IDs for the same app.
        val leftPackages = leftVariants.flatMap { realPackages(it) }.toSet()
        val rightPackages = rightVariants.flatMap { realPackages(it) }.toSet()
        if (leftPackages.intersect(rightPackages).isNotEmpty()) return true

        if (left.source != right.source) {
            val leftNames = identityAliases(left)
            return leftNames.isNotEmpty() && leftNames.intersect(identityAliases(right)).isNotEmpty()
        }
        return false
    }

    val merged = mutableListOf<FdroidApp>()
    cleanedInput.forEach { candidate ->
        val index = merged.indexOfFirst { current -> sameEntry(current, candidate) }
        if (index < 0) {
            merged += candidate.copy(alternateSources = candidate.availableSourceVariants().drop(1))
        } else {
            val current = merged[index]
            val allVariants = (current.availableSourceVariants() + candidate.availableSourceVariants())
                .distinctBy { it.key }
            // Prefer F-Droid as the display record if it is present; the user can
            // still switch to all other sources from the unified detail page.
            val fdroidVariant = allVariants.firstOrNull { it.source == AppCatalogSource.F_DROID }
            val preferred = when {
                current.source == AppCatalogSource.F_DROID -> current
                candidate.source == AppCatalogSource.F_DROID -> candidate
                fdroidVariant != null -> current.forSourceVariant(fdroidVariant)
                else -> current
            }
            val preferredKey = preferred.asSourceVariant().key
            // Preserve each source's own metadata on its own selector entry. The UI's best-field
            // helpers read across variants for the common card, while changing source shows that
            // source's own package/size/developer rather than copying another store's values.
            merged[index] = preferred.copy(
                alternateSources = allVariants.filterNot { it.key == preferredKey }
            )
        }
    }
    return merged
}

data class FdroidSearchResponse(
    val apps: List<FdroidApp>,
    val error: String? = null
)

data class FdroidRelease(
    val versionName: String,
    val versionCode: Long,
    val isSuggested: Boolean = false
)

data class FdroidAppDetails(
    val packageName: String,
    val suggestedVersionCode: Long?,
    val releases: List<FdroidRelease>
)

/** Public presentation metadata scraped from the app's F-Droid package page. Missing fields stay empty. */
data class FdroidAppPageMetadata(
    val description: String = "",
    val whatsNew: String = "",
    val authorName: String = "",
    val license: String = "",
    val websiteUrl: String = "",
    val sourceCodeUrl: String = "",
    val issueTrackerUrl: String = "",
    val changelogUrl: String = "",
    val addedOn: String = "",
    val screenshotUrls: List<String> = emptyList()
)

object FdroidSearchRepository {
    private const val SEARCH_ENDPOINT = "https://search.f-droid.org/api/search_apps?q="
    private const val SEARCH_CACHE_MS = 5 * 60 * 1000L
    private data class CachedSearch(val createdAt: Long, val response: FdroidSearchResponse)
    private val searchCache = ConcurrentHashMap<String, CachedSearch>()

    suspend fun search(query: String): FdroidSearchResponse = withContext(Dispatchers.IO) {
        val term = query.trim()
        if (term.isEmpty()) return@withContext FdroidSearchResponse(emptyList())
        val cacheKey = term.lowercase(Locale.ROOT)
        searchCache[cacheKey]?.takeIf { System.currentTimeMillis() - it.createdAt < SEARCH_CACHE_MS }
            ?.let { return@withContext it.response }

        var connection: HttpURLConnection? = null
        try {
            val encoded = URLEncoder.encode(term, Charsets.UTF_8.name())
            connection = (URL(SEARCH_ENDPOINT + encoded).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 4_000
                readTimeout = 6_000
                instanceFollowRedirects = true
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "AppSpace/1.0 (Android app catalog)")
            }

            val status = connection.responseCode
            val contentType = connection.contentType ?: "(未提供 Content-Type)"
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()

            if (status !in 200..299) {
                return@withContext FdroidSearchResponse(
                    emptyList(),
                    "F-Droid 接口返回 HTTP $status；Content-Type=$contentType；响应片段：${preview(body)}"
                )
            }

            val parsed = try {
                parseApps(body)
            } catch (e: Exception) {
                return@withContext FdroidSearchResponse(
                    emptyList(),
                    "接口请求成功（HTTP $status），但响应无法按 JSON 解析；Content-Type=$contentType；原因=${e.message ?: e.javaClass.simpleName}；响应片段：${preview(body)}"
                )
            }

            if (parsed.isEmpty()) {
                val shape = describeJsonShape(body)
                return@withContext FdroidSearchResponse(
                    emptyList(),
                    "接口请求成功（HTTP $status），但解析出 0 条应用。Content-Type=$contentType；JSON结构=$shape；响应片段：${preview(body)}"
                )
            }

            FdroidSearchResponse(parsed).also { response ->
                // Cache only positive search results. Transient errors and empty/parse
                // anomalies must be allowed to retry after the service recovers.
                searchCache[cacheKey] = CachedSearch(System.currentTimeMillis(), response)
                if (searchCache.size > 128) {
                    val cutoff = System.currentTimeMillis() - SEARCH_CACHE_MS
                    searchCache.entries.toList()
                        .filter { it.value.createdAt < cutoff }
                        .forEach { entry -> searchCache.remove(entry.key, entry.value) }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            FdroidSearchResponse(
                emptyList(),
                "请求异常：${e.javaClass.simpleName}: ${e.localizedMessage ?: "无详细信息"}"
            )
        } finally {
            connection?.disconnect()
        }
    }


    /** 获取 F-Droid 官方主仓库中单个包的展示用版本信息。该 API 不用于验证 APK 安全性。 */
    suspend fun getAppDetails(packageName: String): Result<FdroidAppDetails> = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val safePackage = packageName.trim()
            require(safePackage.matches(Regex("[A-Za-z0-9_.]+"))) { "无效的应用包名" }
            val url = URL("https://f-droid.org/api/v1/packages/" + URLEncoder.encode(safePackage, Charsets.UTF_8.name()))
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 12000
                readTimeout = 15000
                instanceFollowRedirects = true
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "AppSpace/1.0 (Android app catalog)")
            }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (status !in 200..299) return@withContext Result.failure(IllegalStateException("F-Droid 详情接口返回 HTTP $status"))
            val json = JSONObject(body)
            val returnedPackage = json.optString("packageName", safePackage)
            val suggested = if (json.has("suggestedVersionCode") && !json.isNull("suggestedVersionCode")) json.optLong("suggestedVersionCode") else null
            val versions = json.optJSONArray("packages") ?: JSONArray()
            val releases = buildList {
                for (i in 0 until versions.length()) {
                    val item = versions.optJSONObject(i) ?: continue
                    val versionName = item.optString("versionName", "").ifBlank { "未知版本" }
                    val versionCode = item.optLong("versionCode", -1L)
                    if (versionCode >= 0L) add(FdroidRelease(versionName, versionCode, versionCode == suggested))
                }
            }.sortedWith(compareByDescending<FdroidRelease> { it.isSuggested }.thenByDescending { it.versionCode })
            Result.success(FdroidAppDetails(returnedPackage, suggested, releases))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * Load optional visual/details metadata from the public F-Droid package page.
     * This is presentation-only: APK availability/version information still comes from the API above.
     */
    suspend fun getAppPageMetadata(packageName: String): Result<FdroidAppPageMetadata> = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val safePackage = packageName.trim()
            require(safePackage.matches(Regex("[A-Za-z0-9_.]+"))) { "无效的应用包名" }
            val pageUrl = "https://f-droid.org/packages/$safePackage/"
            connection = (URL(pageUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 12000
                instanceFollowRedirects = true
                setRequestProperty("Accept", "text/html")
                setRequestProperty("User-Agent", "AppSpace/1.0 (Android app catalog)")
            }
            val status = connection.responseCode
            if (status !in 200..299) {
                return@withContext Result.failure(IllegalStateException("F-Droid 页面返回 HTTP $status"))
            }
            val html = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            fun block(regex: Regex): String = regex.find(html)?.groupValues?.getOrNull(1).orEmpty()
            fun plain(raw: String): String = Html.fromHtml(raw, Html.FROM_HTML_MODE_COMPACT)
                .toString().replace('\u00a0', ' ').replace(Regex("[\\t ]+"), " ")
                .replace(Regex("\\n{3,}"), "\\n\\n").trim()
            fun linkFrom(id: String): String {
                val item = block(Regex("(?is)<li\\b[^>]*id=[\\\"']$id[\\\"'][^>]*>(.*?)</li>"))
                val href = Regex("(?is)<a\\b[^>]*href=[\\\"']([^\\\"']+)[\\\"']").find(item)?.groupValues?.getOrNull(1).orEmpty()
                return href.trim().let { raw -> if (raw.startsWith("//")) "https:$raw" else raw }
            }
            fun textFrom(id: String): String {
                val item = block(Regex("(?is)<li\\b[^>]*id=[\\\"']$id[\\\"'][^>]*>(.*?)</li>"))
                return plain(item).substringAfter(':', plain(item)).trim()
            }
            val descriptionRaw = block(Regex("(?is)<div\\b[^>]*class=[\\\"'][^\\\"']*\\bpackage-description\\b[^\\\"']*[\\\"'][^>]*>(.*?)</div>"))
            val whatsNewRaw = block(Regex("(?is)<div\\b[^>]*class=[\\\"'][^\\\"']*\\bpackage-whats-new\\b[^\\\"']*[\\\"'][^>]*id=[\\\"']whats_new[\\\"'][^>]*>(.*?)</div>\\s*<div\\b[^>]*class=[\\\"'][^\\\"']*\\bpackage-description\\b"))
            val screenshotUrls = Regex("(?is)<li\\b[^>]*class=[\\\"'][^\\\"']*\\bscreenshot\\b[^\\\"']*[\\\"'][^>]*>\\s*<img\\b[^>]*src=[\\\"']([^\\\"']+)[\\\"']")
                .findAll(html)
                .map { it.groupValues[1].trim() }
                .map { raw -> when {
                    raw.startsWith("https://") || raw.startsWith("http://") -> raw
                    raw.startsWith("//") -> "https:$raw"
                    raw.startsWith("/") -> "https://f-droid.org$raw"
                    else -> "https://f-droid.org/$raw"
                } }
                .distinct().take(12).toList()
            val visibleText = plain(html)
            val addedOn = Regex("(?i)Added on\\s+([A-Za-z]{3,9}\\s+\\d{1,2},?\\s+\\d{4})")
                .find(visibleText)?.groupValues?.getOrNull(1).orEmpty()
            Result.success(
                FdroidAppPageMetadata(
                    description = plain(descriptionRaw),
                    whatsNew = plain(whatsNewRaw),
                    authorName = textFrom("author").ifBlank { textFrom("author_name") },
                    license = textFrom("license"),
                    websiteUrl = linkFrom("website"),
                    sourceCodeUrl = linkFrom("source_code"),
                    issueTrackerUrl = linkFrom("issue_tracker"),
                    changelogUrl = linkFrom("changelog"),
                    addedOn = addedOn,
                    screenshotUrls = screenshotUrls
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            connection?.disconnect()
        }
    }

    private fun parseApps(body: String): List<FdroidApp> {
        val found = linkedMapOf<String, FdroidApp>()
        val trimmed = body.trimStart().removePrefix("\uFEFF")
        val root: Any = when {
            trimmed.startsWith("[") -> JSONArray(trimmed)
            trimmed.startsWith("{") -> JSONObject(trimmed)
            else -> throw JSONException("响应正文不是 JSON 对象或数组")
        }
        collect(root, found)
        return found.values.take(50)
    }

    private fun collect(value: Any?, found: LinkedHashMap<String, FdroidApp>) {
        when (value) {
            is JSONObject -> {
                val name = firstString(value, "name", "localizedName", "title", "label")
                val summary = firstString(value, "summary", "description", "shortDescription", "short_description")
                val explicitPackageName = firstString(
                    value,
                    "packageName", "packagename", "package_name", "packageId", "package_id",
                    "applicationId", "application_id", "package", "id"
                )
                // F-Droid search API items can omit the package-name field. Its app page URL
                // contains /packages/<packageId>; extract that as a safe fallback.
                val packageName = explicitPackageName.ifBlank {
                    packageNameFromUrl(firstString(value, "url", "webUrl", "web_url", "link"))
                }
                if (packageName.isNotBlank() && name.isNotBlank()) {
                    val iconUrl = firstString(value, "icon", "iconUrl", "icon_url", "iconUrlLarge")
                    val developer = firstString(value, "developer", "authorName", "author", "publisher", "developerName")
                    val sizeBytes = value.catalogSizeBytes(
                        "apkSizeBytes", "apk_size_bytes", "fileSizeBytes", "file_size_bytes",
                        "apkSize", "apk_size", "fileSize", "file_size", "downloadSize", "download_size", "size"
                    )
                    found.putIfAbsent(packageName, FdroidApp(
                        packageName = packageName,
                        name = name,
                        summary = summary,
                        iconUrl = normalizeIconUrl(iconUrl),
                        appPackageName = normalizedAndroidPackageName(packageName),
                        developer = developer,
                        apkSizeBytes = sizeBytes
                    ))
                }
                val keys = value.keys()
                while (keys.hasNext()) collect(value.opt(keys.next()), found)
            }
            is JSONArray -> for (i in 0 until value.length()) collect(value.opt(i), found)
        }
    }

    private fun packageNameFromUrl(rawUrl: String): String {
        if (rawUrl.isBlank()) return ""
        return try {
            val path = URL(rawUrl).path
            val marker = "/packages/"
            val index = path.indexOf(marker)
            if (index >= 0) path.substring(index + marker.length).substringBefore('/').trim() else ""
        } catch (_: Exception) {
            ""
        }
    }

    private fun firstString(obj: JSONObject, vararg keys: String): String {
        for (key in keys) {
            val value = obj.opt(key)
            if (value is String && value.isNotBlank()) return value.trim()
            if (value is Number) return value.toString()
        }
        return ""
    }


    private fun normalizeIconUrl(raw: String): String {
        if (raw.isBlank()) return ""
        return try {
            when {
                raw.startsWith("https://") -> raw
                raw.startsWith("http://") -> "https://" + raw.removePrefix("http://")
                raw.startsWith("/") -> "https://f-droid.org$raw"
                else -> "https://f-droid.org/$raw"
            }
        } catch (_: Exception) { "" }
    }

    private fun describeJsonShape(body: String): String {
        return try {
            val trimmed = body.trimStart().removePrefix("\uFEFF")
            when {
                trimmed.startsWith("{") -> {
                    val obj = JSONObject(trimmed)
                    "顶层对象字段=${obj.keys().asSequence().take(25).toList()}"
                }
                trimmed.startsWith("[") -> {
                    val arr = JSONArray(trimmed)
                    val first = arr.optJSONObject(0)
                    "顶层数组长度=${arr.length()}；首项字段=${first?.keys()?.asSequence()?.take(25)?.toList() ?: "无对象首项"}"
                }
                else -> "正文开头不是 JSON"
            }
        } catch (e: Exception) {
            "无法读取 JSON 结构：${e.message ?: e.javaClass.simpleName}"
        }
    }

    private fun preview(body: String): String {
        val compact = body.replace(Regex("\\s+"), " ").trim()
        if (compact.isEmpty()) return "(空响应)"
        return compact.take(350).replace("<", "‹").replace(">", "›")
    }
}