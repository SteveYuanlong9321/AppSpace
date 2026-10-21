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
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Uptodown search/detail adapter ported from the public Obtainium source.
 * Search uses its Android search endpoint; obtaining a direct download URL
 * requires an anonymous short-lived client token. Both mechanisms are
 * undocumented and can change without notice.
 */
object UptodownRepository {
    private const val SEARCH_URL = "https://en.uptodown.com/android/en/s"
    private const val API_HOST = "www.uptodown.app"
    private const val AUTH_PATH = "/eapi/auth/token"
    private const val DOWNLOAD_PATH_PREFIX = "/eapi/apps/"
    private const val CLIENT_VERSION = "739"
    private const val USER_AGENT = "Dalvik/2.1.0 (Linux; U; Android 14; AppSpace)"
    // Same anonymous-client request signing scheme used by Obtainium's public adapter.
    private const val HMAC_KEY = "MDGMXUMdvHJBG/vjdFgmqX6LUdy7ecfwvYNd0gyfOCs="
    private const val SEARCH_CACHE_MS = 5 * 60 * 1000L
    private const val EMPTY_CACHE_MS = 20 * 1000L
    private const val CONNECT_TIMEOUT_MS = 3_000
    private const val READ_TIMEOUT_MS = 4_000

    private data class Cached<T>(val at: Long, val value: T)
    private data class Session(val token: String, val expiresAtSeconds: Long)
    private data class SearchMetadata(
        val name: String,
        val developer: String,
        val iconUrl: String,
        val summary: String,
        val packageName: String = "",
        val apkSizeBytes: Long? = null
    )
    private data class DownloadIds(val appId: String, val fileId: String)

    @Volatile private var session: Session? = null
    private val searchCache = ConcurrentHashMap<String, Cached<StoreCatalogSearchResponse>>()
    private val detailCache = ConcurrentHashMap<String, Cached<Result<StoreAppDetails>>>()
    private val metadataByUrl = ConcurrentHashMap<String, SearchMetadata>()
    private val downloadIdsByUrl = ConcurrentHashMap<String, DownloadIds>()

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
            val body = "queryString=${encode(term)}"
            val response = request(SEARCH_URL, method = "POST", body = body, headers = mapOf(
                "Content-Type" to "application/x-www-form-urlencoded",
                "Accept" to "application/json, text/plain, */*",
                "User-Agent" to USER_AGENT,
                "Origin" to "https://en.uptodown.com",
                "Referer" to SEARCH_URL
            ))
            if (response.first !in 200..299) {
                return@withContext StoreCatalogSearchResponse(emptyList(), "Uptodown 搜索暂时不可用（HTTP ${response.first}）。")
            }
            val json = JSONObject(response.second)
            if (json.optInt("success", 0) != 1) {
                return@withContext StoreCatalogSearchResponse(emptyList(), "Uptodown 未返回可用的搜索结果。")
            }
            val apps = json.optJSONObject("data")?.optJSONArray("apps") ?: JSONArray()
            val found = ArrayList<FdroidApp>(apps.length())
            for (i in 0 until apps.length()) {
                val item = apps.optJSONObject(i) ?: continue
                if (item.optString("platformURL") != "/android") continue
                val url = normalizeAppUrl(item.optString("url")) ?: continue
                val name = cleanText(first(item, "name", "title", "appName"))
                if (name.isBlank()) continue
                val developer = cleanText(first(item, "author", "developer", "publisher"))
                val icon = normalizeImageUrl(first(item, "icon", "iconUrl", "icon_url", "image", "imageUrl", "logo"))
                val summary = cleanText(first(item, "description", "summary", "subtitle"))
                    .ifBlank { "来自 Uptodown" }
                val packageName = normalizedAndroidPackageName(first(item, "packageName", "package_name", "pkgName", "package"))
                val sizeBytes = item.catalogSizeBytes(
                    "apkSizeBytes", "apk_size_bytes", "fileSizeBytes", "file_size_bytes",
                    "apkSize", "apk_size", "appSize", "app_size", "fileSize", "file_size", "size"
                )
                metadataByUrl[url] = SearchMetadata(name, developer, icon, summary, packageName, sizeBytes)
                val slug = runCatching { URL(url).host.substringBefore(".en.uptodown.com").ifBlank { URL(url).host.substringBefore(".uptodown.com") } }.getOrDefault(url.hashCode().toString())
                found += FdroidApp(
                    packageName = "uptodown:$slug",
                    name = name,
                    summary = summary,
                    iconUrl = icon,
                    source = AppCatalogSource.UPTODOWN,
                    // Use the canonical store URL as the source ID: each Uptodown app has its own subdomain.
                    sourceId = url,
                    sourceUrl = url,
                    appPackageName = packageName,
                    developer = developer,
                    apkSizeBytes = sizeBytes
                )
            }
            val result = StoreCatalogSearchResponse(
                apps = AppSearchRanking.rankApps(term, found.distinctBy { it.sourceId.lowercase(Locale.ROOT) }).take(20)
            )
            searchCache[cacheKey] = Cached(System.currentTimeMillis(), result)
            result
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            StoreCatalogSearchResponse(emptyList(), "Uptodown 搜索失败：${error.message ?: error.javaClass.simpleName}")
        }
    }

    suspend fun getAppDetails(sourceUrl: String): Result<StoreAppDetails> = withContext(Dispatchers.IO) {
        val normalizedUrl = normalizeAppUrl(sourceUrl)
            ?: return@withContext Result.failure(IllegalArgumentException("Uptodown 应用链接无效。"))
        detailCache[normalizedUrl]?.takeIf { System.currentTimeMillis() - it.at < SEARCH_CACHE_MS }?.let { return@withContext it.value }

        val result = runCatching {
            val response = request(normalizedUrl, headers = mapOf(
                "Accept" to "text/html,application/xhtml+xml",
                "User-Agent" to "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0 Mobile Safari/537.36"
            ))
            if (response.first !in 200..299) throw IllegalStateException("Uptodown 详情页返回 HTTP ${response.first}。")
            val html = response.second
            val cached = metadataByUrl[normalizedUrl]
            val titleTag = elementById(html, "detail-app-name")
            val downloadTag = elementById(html, "detail-download-button")
            val name = cleanText(innerHtml(titleTag).orEmpty()).ifBlank { cached?.name.orEmpty() }
            val author = cleanText(innerHtml(elementById(html, "author-link")).orEmpty()).ifBlank { cached?.developer.orEmpty() }
            val appId = attribute(downloadTag, "data-app-id")
                .ifBlank { attribute(titleTag, "data-code") }
            val fileId = attribute(downloadTag, "data-file-id")
                .ifBlank { attribute(titleTag, "data-file-id") }
            val fileType = extractTechnicalValue(html, "file type").lowercase(Locale.ROOT)
            val supportsDirectApk = fileType.isBlank() || (fileType.contains("apk") && !fileType.contains("xapk"))
            val canDownload = appId.isNotBlank() && fileId.isNotBlank() && supportsDirectApk
            if (canDownload) downloadIdsByUrl[normalizedUrl] = DownloadIds(appId, fileId)
            val version = extractVersion(html)
            val packageName = extractPackageName(html).ifBlank { cached?.packageName.orEmpty() }
            val icon = metaContent(html, "og:image").let(::normalizeImageUrl).ifBlank { cached?.iconUrl.orEmpty() }
            val description = cleanText(metaContent(html, "description")).ifBlank { cached?.summary.orEmpty() }
            val releaseDate = extractTechnicalValue(html, "date")
            val sizeText = extractFileSizeText(html).ifBlank { cached?.apkSizeBytes?.let { formatCatalogSize(it).orEmpty() }.orEmpty() }
            val apkSizeBytes = parseCatalogSizeBytes(sizeText, "fileSize") ?: cached?.apkSizeBytes
            // Resolve the tokenized CDN URL only after the user taps Install; opening details stays fast.
            StoreAppDetails(
                name = name.ifBlank { URL(normalizedUrl).host.substringBefore(".en.uptodown.com") },
                packageName = packageName,
                developer = author.ifBlank { cached?.developer.orEmpty() },
                versionName = version,
                description = description,
                iconUrl = icon,
                downloadUrl = "",
                sourceUrl = normalizedUrl,
                releaseDate = releaseDate,
                screenshotUrls = extractScreenshotUrls(html, icon),
                apkSizeBytes = apkSizeBytes,
                downloadAvailable = canDownload
            )
        }
        if (result.isSuccess) detailCache[normalizedUrl] = Cached(System.currentTimeMillis(), result)
        result
    }

    /** Enrich leading results in the background; package and file-size lookups must not hold up initial search results. */
    suspend fun enrichSearchApps(apps: List<FdroidApp>, limit: Int = 5): List<FdroidApp> = coroutineScope {
        val semaphore = Semaphore(2)
        val leading = apps.take(limit)
        val enriched = leading.map { app ->
            async(Dispatchers.IO) {
                semaphore.withPermit {
                    val alreadyHasPackage = normalizedAndroidPackageName(app.appPackageName).isNotBlank()
                    if (alreadyHasPackage && app.apkSizeBytes != null && app.developer.isNotBlank()) return@withPermit app
                    val detail = getAppDetails(app.sourceUrl.ifBlank { app.sourceId }).getOrNull() ?: return@withPermit app
                    app.copy(
                        appPackageName = normalizedAndroidPackageName(detail.packageName).ifBlank { app.appPackageName },
                        developer = detail.developer.ifBlank { app.developer },
                        apkSizeBytes = detail.apkSizeBytes ?: app.apkSizeBytes,
                        iconUrl = app.iconUrl.ifBlank { detail.iconUrl }
                    )
                }
            }
        }.awaitAll()
        val byUrl = enriched.associateBy { it.sourceUrl.ifBlank { it.sourceId } }
        apps.map { byUrl[it.sourceUrl.ifBlank { it.sourceId }] ?: it }
    }

    suspend fun resolveDownloadUrlForApp(sourceUrl: String): Result<String> = withContext(Dispatchers.IO) {
        val normalizedUrl = normalizeAppUrl(sourceUrl)
            ?: return@withContext Result.failure(IllegalArgumentException("Uptodown 应用链接无效。"))
        try {
            var ids = downloadIdsByUrl[normalizedUrl]
            if (ids == null) {
                val response = request(normalizedUrl, headers = mapOf(
                    "Accept" to "text/html,application/xhtml+xml",
                    "User-Agent" to "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0 Mobile Safari/537.36"
                ))
                if (response.first !in 200..299) throw IllegalStateException("Uptodown 详情页返回 HTTP ${response.first}。")
                ids = parseDownloadIds(normalizedUrl, response.second)
                    ?: throw IllegalStateException("Uptodown 未提供可直接安装的 APK；XAPK 等格式暂不支持。")
                downloadIdsByUrl[normalizedUrl] = ids
            }
            Result.success(resolveDownloadUrl(ids.appId, ids.fileId))
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    private fun parseDownloadIds(normalizedUrl: String, html: String): DownloadIds? {
        val titleTag = elementById(html, "detail-app-name")
        val downloadTag = elementById(html, "detail-download-button")
        val appId = attribute(downloadTag, "data-app-id").ifBlank { attribute(titleTag, "data-code") }
        val fileId = attribute(downloadTag, "data-file-id").ifBlank { attribute(titleTag, "data-file-id") }
        val fileType = extractTechnicalValue(html, "file type").lowercase(Locale.ROOT)
        val supported = fileType.isBlank() || (fileType.contains("apk") && !fileType.contains("xapk"))
        if (appId.isBlank() || fileId.isBlank() || !supported) return null
        return DownloadIds(appId, fileId)
    }

    private fun resolveDownloadUrl(appId: String, fileId: String): String {
        repeat(2) { attempt ->
            val active = getSession(forceRefresh = attempt > 0)
            val url = "https://$API_HOST$DOWNLOAD_PATH_PREFIX${encodePath(appId)}/file/${encodePath(fileId)}/downloadUrl"
            val response = request(url, headers = mapOf(
                "Accept" to "application/json",
                "User-Agent" to USER_AGENT,
                "Identificador" to "Uptodown_Android",
                "Identificador-Version" to CLIENT_VERSION,
                "Authorization" to "Bearer ${active.token}"
            ))
            if (response.first == 401) return@repeat
            if (response.first !in 200..299) throw IllegalStateException("Uptodown 下载接口返回 HTTP ${response.first}。")
            val json = JSONObject(response.second)
            if (json.optInt("success", 0) != 1) throw IllegalStateException("Uptodown 未返回可用下载地址。")
            val downloadUrl = json.optJSONObject("data")?.optString("downloadURL").orEmpty()
            val parsed = runCatching { URL(downloadUrl) }.getOrNull()
            if (parsed?.protocol != "https" || parsed.host.isBlank()) {
                throw IllegalStateException("Uptodown 返回的下载地址不是有效的 HTTPS 链接，已阻止下载。")
            }
            return downloadUrl
        }
        throw IllegalStateException("Uptodown 下载授权失效，请稍后重试。")
    }

    private fun getSession(forceRefresh: Boolean = false): Session {
        val now = System.currentTimeMillis() / 1000L
        session?.takeIf { !forceRefresh && now < it.expiresAtSeconds - 60 }?.let { return it }
        val randomBytes = ByteArray(8).also { SecureRandom().nextBytes(it) }
        val identifier = randomBytes.joinToString("") { "%02x".format(it) }
        val timestamp = now.toString()
        val signature = hmacHex(timestamp)
        val authUrl = "https://$API_HOST$AUTH_PATH?identifier=${encode(identifier)}"
        val body = listOf(
            "identifier" to identifier,
            "id_plataforma" to "13",
            "lang" to "en",
            "unixtime" to timestamp,
            "hmac" to signature
        ).joinToString("&") { "${encode(it.first)}=${encode(it.second)}" }
        val response = request(authUrl, method = "POST", body = body, headers = mapOf(
            "Accept" to "application/json",
            "Content-Type" to "application/x-www-form-urlencoded",
            "User-Agent" to USER_AGENT,
            "Identificador" to "Uptodown_Android",
            "Identificador-Version" to CLIENT_VERSION
        ))
        if (response.first !in 200..299) throw IllegalStateException("Uptodown 匿名授权返回 HTTP ${response.first}。")
        val token = JSONObject(response.second).optString("token")
        val parts = token.split('.')
        if (parts.size != 3) throw IllegalStateException("Uptodown 未返回有效授权令牌。")
        val payloadBytes = android.util.Base64.decode(
            parts[1].replace('-', '+').replace('_', '/').let { value -> value + "=".repeat((4 - value.length % 4) % 4) },
            android.util.Base64.DEFAULT
        )
        val exp = JSONObject(String(payloadBytes, StandardCharsets.UTF_8)).optLong("exp", 0L)
        if (exp <= now) throw IllegalStateException("Uptodown 授权令牌已过期。")
        return Session(token, exp).also { session = it }
    }

    private fun hmacHex(value: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(HMAC_KEY.toByteArray(StandardCharsets.UTF_8), "HmacSHA256"))
        return mac.doFinal(value.toByteArray(StandardCharsets.UTF_8)).joinToString("") { "%02x".format(it) }
    }

    private fun request(
        url: String,
        method: String = "GET",
        body: String? = null,
        headers: Map<String, String> = emptyMap()
    ): Pair<Int, String> {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            instanceFollowRedirects = true
            useCaches = true
            headers.forEach { (key, value) -> setRequestProperty(key, value) }
            if (body != null) {
                doOutput = true
                outputStream.use { it.write(body.toByteArray(StandardCharsets.UTF_8)) }
            }
        }
        try {
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            return status to (stream?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() }.orEmpty())
        } finally {
            connection.disconnect()
        }
    }

    private fun normalizeAppUrl(raw: String): String? {
        val value = raw.trim()
        if (value.isBlank()) return null
        val candidate = runCatching {
            val initial = URL(value)
            if (initial.protocol !in setOf("http", "https")) return null
            val host = initial.host.lowercase(Locale.ROOT)
            if (host != "uptodown.com" && !host.endsWith(".uptodown.com")) return null
            URL("https", host, initial.port.takeIf { it != 80 && it != 443 } ?: -1, initial.file).toString()
        }.getOrNull() ?: return null
        return candidate
    }

    private fun extractVersion(html: String): String {
        val versionElement = Regex("(?is)<([a-z][a-z0-9]*)\\b[^>]*class=[\"'][^\"']*\\bversion\\b[^\"']*[\"'][^>]*>(.*?)</\\1>")
            .find(html)?.groupValues?.getOrNull(2)?.let(::cleanText).orEmpty()
        if (versionElement.isNotBlank()) return versionElement.take(80)
        val bodyText = cleanText(html)
        return Regex("(?i)\\bversion\\s*:?\\s*([0-9][0-9A-Za-z.+_-]{0,40})").find(bodyText)?.groupValues?.getOrNull(1).orEmpty()
    }

    private fun extractPackageName(html: String): String {
        Regex("(?is)<tr\\b[^>]*>(.*?)</tr>").findAll(html).forEach { match ->
            val row = match.groupValues[1]
            val text = cleanText(row).lowercase(Locale.ROOT)
            val iconMatch = row.contains("icon-40-package", ignoreCase = true)
            if (iconMatch || text.contains("package name")) {
                val cells = Regex("(?is)<td\\b[^>]*>(.*?)</td>").findAll(row).map { cleanText(it.groupValues[1]) }.toList()
                cells.lastOrNull { it.contains('.') && it.matches(Regex("[A-Za-z0-9_.]+")) }?.let { return it }
            }
        }
        val meta = metaContent(html, "application-id")
        return meta.takeIf { it.matches(Regex("[A-Za-z0-9_.]+")) }.orEmpty()
    }

    private fun extractFileSizeText(html: String): String {
        Regex("(?is)<tr\\b[^>]*>(.*?)</tr>").findAll(html).forEach { match ->
            val row = match.groupValues[1]
            val rowText = cleanText(row)
            val isSizeRow = rowText.contains("file size", ignoreCase = true) ||
                    rowText.contains("apk size", ignoreCase = true) ||
                    row.contains("icon-40-size", ignoreCase = true) ||
                    row.contains("icon-40-file-size", ignoreCase = true)
            if (isSizeRow) {
                val cells = Regex("(?is)<td\\b[^>]*>(.*?)</td>").findAll(row)
                    .map { cleanText(it.groupValues[1]) }.toList()
                cells.lastOrNull { it.isNotBlank() }?.let { return it }
            }
        }
        return extractTechnicalValue(html, "file size")
    }

    private fun extractTechnicalValue(html: String, label: String): String {
        Regex("(?is)<tr\\b[^>]*>(.*?)</tr>").findAll(html).forEach { match ->
            val row = match.groupValues[1]
            if (cleanText(row).contains(label, ignoreCase = true)) {
                val cells = Regex("(?is)<td\\b[^>]*>(.*?)</td>").findAll(row).map { cleanText(it.groupValues[1]) }.toList()
                cells.lastOrNull()?.takeIf { it.isNotBlank() }?.let { return it }
            }
        }
        return ""
    }

    private fun extractScreenshotUrls(html: String, iconUrl: String): List<String> {
        val urls = linkedSetOf<String>()
        Regex("(?is)<img\\b[^>]*>").findAll(html).forEach { match ->
            val tag = match.value
            val lower = tag.lowercase(Locale.ROOT)
            if (!(lower.contains("screenshot") || lower.contains("screen-shot") || lower.contains("gallery") || lower.contains("slider"))) return@forEach
            val src = Regex("(?i)(?:src|data-src|data-original)\\s*=\\s*[\"']([^\"']+)[\"']").find(tag)?.groupValues?.getOrNull(1).orEmpty()
            val normalized = normalizeImageUrl(src)
            if (normalized.isNotBlank() && normalized != iconUrl) urls += normalized
        }
        return urls.take(8)
    }

    private fun elementById(html: String, id: String): String? {
        val escapedId = Regex.escape(id)
        return Regex("(?is)<([a-z][a-z0-9]*)\\b(?=[^>]*\\bid=[\"']$escapedId[\"'])[^>]*>.*?</\\1>")
            .find(html)?.value
    }

    private fun innerHtml(tag: String?): String? {
        if (tag == null) return null
        val opening = Regex("(?is)^<([a-z][a-z0-9]*)\\b[^>]*>").find(tag) ?: return null
        val close = "</${opening.groupValues[1]}"
        val start = opening.range.last + 1
        val end = tag.lastIndexOf(close, ignoreCase = true)
        return if (end >= start) tag.substring(start, end) else ""
    }

    private fun attribute(tag: String?, name: String): String {
        if (tag == null) return ""
        return Regex("(?i)\\b${Regex.escape(name)}\\s*=\\s*[\"']([^\"']*)[\"']").find(tag)?.groupValues?.getOrNull(1).orEmpty()
    }

    private fun metaContent(html: String, key: String): String {
        Regex("(?is)<meta\\b[^>]*>").findAll(html).forEach { match ->
            val tag = match.value
            val name = attribute(tag, "property").ifBlank { attribute(tag, "name") }
            if (name.equals(key, true)) return attribute(tag, "content")
        }
        return ""
    }

    private fun normalizeImageUrl(raw: String): String {
        val value = raw.trim()
        val fixed = when {
            value.startsWith("//") -> "https:$value"
            value.startsWith("https://", true) -> value
            else -> ""
        }
        return fixed.takeIf { runCatching { URL(it).protocol == "https" }.getOrDefault(false) }.orEmpty()
    }

    private fun first(json: JSONObject, vararg keys: String): String {
        for (key in keys) {
            val value = json.opt(key) ?: continue
            if (value == JSONObject.NULL || value is JSONObject || value is JSONArray) continue
            val text = value.toString().trim()
            if (text.isNotBlank() && text != "null") return text
        }
        return ""
    }

    private fun cleanText(value: String): String =
        Html.fromHtml(value, Html.FROM_HTML_MODE_COMPACT).toString().replace('\u00a0', ' ').trim()

    private fun encode(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8.name())
    private fun encodePath(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8.name()).replace("+", "%20")
}