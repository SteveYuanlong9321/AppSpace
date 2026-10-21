package com.example.appspace

import android.text.Html
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * Vivo App Store search and detail adapter based on the public H5 endpoints
 * used by Obtainium. The endpoint is undocumented and may change.
 *
 * Search and metadata requests stay HTTPS. The official download endpoint is
 * returned as the initial HTTPS URL; Android DownloadManager handles the
 * store's download redirect, if permitted by the store and the device.
 */
object VivoAppStoreRepository {
    private const val SEARCH_BASE = "https://h5-api.appstore.vivo.com.cn/h5appstore/search/result-list"
    private const val DETAIL_BASE = "https://h5-api.appstore.vivo.com.cn/detailInfo"
    private const val DOWNLOAD_BASE = "https://appstore.vivo.com.cn/appinfo/downloadApkFile"
    private const val DETAIL_PAGE = "https://detail-browser.vivo.com.cn/v115/index.html"
    private const val USER_AGENT = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0 Mobile Safari/537.36"
    private const val SEARCH_CACHE_MS = 5 * 60 * 1000L
    private const val EMPTY_CACHE_MS = 20 * 1000L
    private const val CONNECT_TIMEOUT_MS = 3_500
    private const val READ_TIMEOUT_MS = 4_500

    private data class Cached<T>(val at: Long, val value: T)
    private val searchCache = ConcurrentHashMap<String, Cached<StoreCatalogSearchResponse>>()
    private val detailCache = ConcurrentHashMap<String, Cached<Result<StoreAppDetails>>>()

    suspend fun search(query: String): StoreCatalogSearchResponse = withContext(Dispatchers.IO) {
        val term = query.trim()
        if (term.isBlank() || term.startsWith("http://", true) || term.startsWith("https://", true)) {
            return@withContext StoreCatalogSearchResponse(emptyList())
        }
        val cacheKey = term.lowercase(Locale.ROOT)
        searchCache[cacheKey]?.takeIf {
            System.currentTimeMillis() - it.at < if (it.value.apps.isEmpty()) EMPTY_CACHE_MS else SEARCH_CACHE_MS
        }?.let { return@withContext it.value }

        try {
            val params = "app_version=2100&page_index=1&apps_per_page=20&target=local&cfrom=2&key=${encode(term)}"
            val json = requestJson("$SEARCH_BASE?$params")
            val data = json.optJSONObject("data")
            val search = data?.optJSONObject("appSearchResponse")
            if (json.optInt("code", -1) != 0) {
                return@withContext StoreCatalogSearchResponse(emptyList(), "vivo 应用商店搜索暂时不可用（接口状态 ${json.optInt("code", -1)}）。")
            }
            if (search?.optBoolean("result", false) == false) {
                val response = StoreCatalogSearchResponse(emptyList())
                searchCache[cacheKey] = Cached(System.currentTimeMillis(), response)
                return@withContext response
            }

            val values = search?.optJSONArray("value") ?: JSONArray()
            val found = ArrayList<FdroidApp>(values.length())
            for (i in 0 until values.length()) {
                val item = values.optJSONObject(i) ?: continue
                val id = first(item, "id", "appId", "appid").trim()
                val name = cleanText(first(item, "title_zh", "title", "name", "appName"))
                if (id.isBlank() || name.isBlank()) continue
                val developer = cleanText(first(item, "developer", "author", "publisher"))
                val icon = normalizeImageUrl(first(item, "icon_url", "iconUrl", "icon", "logo", "appIcon", "imageUrl"))
                val summary = cleanText(first(item, "brief", "summary", "description", "intro", "subtitle"))
                    .ifBlank { "来自 vivo 应用商店" }
                val realPackage = normalizedAndroidPackageName(first(item, "package_name", "packageName", "pkgName", "package"))
                val sizeBytes = item.catalogSizeBytes(
                    "apkSizeBytes", "apk_size_bytes", "fileSizeBytes", "file_size_bytes",
                    "apkSize", "apk_size", "appSize", "app_size", "fileSize", "file_size", "size"
                )
                val detailUrl = detailUrl(id)
                found += FdroidApp(
                    packageName = "vivo:$id",
                    name = name,
                    summary = summary,
                    iconUrl = icon,
                    source = AppCatalogSource.VIVO_APPSTORE,
                    sourceId = id,
                    sourceUrl = detailUrl,
                    appPackageName = realPackage,
                    developer = developer,
                    apkSizeBytes = sizeBytes
                )
            }
            val response = StoreCatalogSearchResponse(
                apps = AppSearchRanking.rankApps(term, found.distinctBy { it.sourceId }).take(20)
            )
            searchCache[cacheKey] = Cached(System.currentTimeMillis(), response)
            response
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            StoreCatalogSearchResponse(emptyList(), "vivo 应用商店搜索失败：${error.message ?: error.javaClass.simpleName}")
        }
    }

    suspend fun getAppDetails(appId: String): Result<StoreAppDetails> = withContext(Dispatchers.IO) {
        val id = appId.trim()
        if (id.isBlank() || !id.matches(Regex("[A-Za-z0-9_.-]{1,128}"))) {
            return@withContext Result.failure(IllegalArgumentException("vivo 应用 ID 无效。"))
        }
        detailCache[id]?.takeIf { System.currentTimeMillis() - it.at < SEARCH_CACHE_MS }?.let { return@withContext it.value }
        val result = runCatching {
            val json = requestJson("$DETAIL_BASE?appId=${encode(id)}")
            val detail = json.optJSONObject("data")?.takeIf { it.has("id") } ?: json
            if (detail.optString("id").isBlank()) throw IllegalStateException("vivo 应用商店未返回应用详情。")
            val name = cleanText(first(detail, "title_zh", "title", "name", "appName")).ifBlank { id }
            val packageName = first(detail, "package_name", "packageName", "pkgName")
            val developer = cleanText(first(detail, "developer", "author", "publisher"))
            val version = first(detail, "version_name", "versionName", "version")
            val icon = normalizeImageUrl(first(detail, "icon_url", "iconUrl", "icon", "logo", "appIcon", "imageUrl"))
            val description = cleanText(first(detail, "description", "app_desc", "appDesc", "intro", "introduction", "brief", "brief_introduction", "summary"))
            val uploadTime = first(detail, "upload_time", "uploadTime", "update_time", "updateTime", "publish_time")
            val apkSizeBytes = detail.catalogSizeBytes(
                "apkSizeBytes", "apk_size_bytes", "fileSizeBytes", "file_size_bytes",
                "apkSize", "apk_size", "appSize", "app_size", "fileSize", "file_size", "downloadSize", "download_size", "size"
            )
            // Only use Vivo's official HTTPS download endpoint as the entry point.
            // It may redirect to a CDN selected by Vivo; we do not resolve arbitrary URLs here.
            val downloadUrl = "$DOWNLOAD_BASE?id=${encode(id)}"
            val screenshots = extractScreenshots(detail, icon)
            StoreAppDetails(
                name = name,
                packageName = packageName.ifBlank { "vivo:$id" },
                developer = developer,
                versionName = version,
                description = description,
                iconUrl = icon,
                downloadUrl = downloadUrl,
                sourceUrl = detailUrl(id),
                releaseDate = uploadTime,
                screenshotUrls = screenshots,
                apkSizeBytes = apkSizeBytes
            ).also {
                if (it.versionName.isBlank()) throw IllegalStateException("vivo 应用商店未提供版本信息。")
            }
        }
        if (result.isSuccess) detailCache[id] = Cached(System.currentTimeMillis(), result)
        result
    }

    /** Enrich only the leading visible results after search has returned, so metadata work never delays the search list. */
    suspend fun enrichSearchApps(apps: List<FdroidApp>, limit: Int = 6): List<FdroidApp> = coroutineScope {
        val semaphore = Semaphore(3)
        val leading = apps.take(limit)
        val enriched = leading.map { app ->
            async(Dispatchers.IO) {
                semaphore.withPermit {
                    val alreadyHasPackage = normalizedAndroidPackageName(app.appPackageName).isNotBlank()
                    if (alreadyHasPackage && app.apkSizeBytes != null && app.developer.isNotBlank()) return@withPermit app
                    val detail = getAppDetails(app.sourceId).getOrNull() ?: return@withPermit app
                    app.copy(
                        appPackageName = normalizedAndroidPackageName(detail.packageName).ifBlank { app.appPackageName },
                        developer = detail.developer.ifBlank { app.developer },
                        apkSizeBytes = detail.apkSizeBytes ?: app.apkSizeBytes,
                        iconUrl = app.iconUrl.ifBlank { detail.iconUrl }
                    )
                }
            }
        }.awaitAll()
        val byId = enriched.associateBy { it.sourceId }
        apps.map { byId[it.sourceId] ?: it }
    }

    private fun detailUrl(id: String): String = "$DETAIL_PAGE?appId=${encode(id)}"

    private fun requestJson(url: String): JSONObject {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            instanceFollowRedirects = true
            useCaches = true
            setRequestProperty("Accept", "application/json, text/plain, */*")
            setRequestProperty("User-Agent", USER_AGENT)
            setRequestProperty("Referer", "https://h5.appstore.vivo.com.cn/")
        }
        try {
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (status !in 200..299) throw IllegalStateException("HTTP $status")
            return JSONObject(body)
        } finally {
            connection.disconnect()
        }
    }

    private fun extractScreenshots(detail: JSONObject, iconUrl: String): List<String> {
        val keys = listOf("screenshots", "screenshotList", "screenshot_list", "screenShotList", "screen_shot_list", "appScreenshots", "screenList", "picList", "screenshotUrls", "screenshot_urls")
        val output = linkedSetOf<String>()
        for (key in keys) {
            val value = detail.opt(key) ?: continue
            when (value) {
                is JSONArray -> for (i in 0 until value.length()) {
                    val entry = value.opt(i)
                    val raw = when (entry) {
                        is String -> entry
                        is JSONObject -> first(entry, "url", "imageUrl", "image_url", "src", "imgUrl", "picUrl", "image", "pic")
                        else -> ""
                    }
                    normalizeImageUrl(raw).takeIf { it.isNotBlank() && it != iconUrl }?.let(output::add)
                }
                is String -> value.split(',', ';', '\n').map(::normalizeImageUrl)
                    .filter { it.isNotBlank() && it != iconUrl }.forEach(output::add)
            }
        }
        return output.take(8)
    }

    private fun first(json: JSONObject, vararg keys: String): String {
        for (key in keys) {
            val value = json.opt(key) ?: continue
            if (value == JSONObject.NULL || value is JSONObject || value is JSONArray) continue
            val text = value.toString().trim()
            if (text.isNotEmpty() && text != "null") return text
        }
        return ""
    }

    private fun cleanText(value: String): String =
        Html.fromHtml(value, Html.FROM_HTML_MODE_COMPACT).toString().replace('\u00a0', ' ').trim()

    private fun normalizeImageUrl(value: String): String {
        val raw = value.trim()
        if (raw.isBlank()) return ""
        val fixed = when {
            raw.startsWith("//") -> "https:$raw"
            raw.startsWith("http://", ignoreCase = true) -> "https://${raw.substring(7)}"
            raw.startsWith("https://", ignoreCase = true) -> raw
            else -> ""
        }
        return fixed.takeIf { runCatching { URL(it).protocol == "https" }.getOrDefault(false) }.orEmpty()
    }

    private fun encode(value: String): String = URLEncoder.encode(value, Charsets.UTF_8.name())
}