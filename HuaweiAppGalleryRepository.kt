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
import org.json.JSONTokener
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.SecureRandom
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * Anonymous Huawei AppGallery adapter. It follows the public-client protocol used by
 * Obtainium, but keeps the result model compatible with AppSpace's existing catalog.
 * AppGallery's client endpoints are undocumented and may change without notice.
 */
data class HuaweiSearchResponse(val apps: List<FdroidApp>, val error: String? = null)

object HuaweiAppGalleryRepository {
    private const val API_PATH = "/hwmarket/api/clientApi"
    private const val DEFAULT_CONNECT_TIMEOUT_MS = 15000
    private const val DEFAULT_READ_TIMEOUT_MS = 20000
    private const val USER_AGENT = "HiSpace##16.5.1.301##google##Pixel 8 Pro"
    private const val CLIENT_VERSION = "16.5.1"
    private const val CLIENT_VERSION_CODE = "160501301"
    private const val HOST_CN = "store-drcn.hispace.dbankcloud.com"
    private const val HOST_ASIA = "store-dra.hispace.dbankcloud.com"
    private const val HOST_EU = "store-dre.hispace.dbankcloud.com"
    private const val HOST_RU = "store-drru.hispace.dbankcloud.ru"
    private const val SESSION_MAX_AGE_MS = 24L * 60L * 60L * 1000L
    private const val SEARCH_CACHE_MS = 5L * 60L * 1000L
    // Empty successful responses are cached briefly only; an API hiccup or a
    // transiently empty index must not hide newly available results for minutes.
    private const val EMPTY_SEARCH_CACHE_MS = 20L * 1000L
    private const val SEARCH_CONNECT_TIMEOUT_MS = 3_500
    private const val SEARCH_READ_TIMEOUT_MS = 5_000

    private data class Session(val host: String, val sign: String, val deviceId: String, val createdAt: Long)
    private data class Cached<T>(val at: Long, val value: T)

    @Volatile private var session: Session? = null
    private val searchCache = ConcurrentHashMap<String, Cached<HuaweiSearchResponse>>()
    private val appLocaleById = ConcurrentHashMap<String, String>()
    private val detailCache = ConcurrentHashMap<String, Cached<Result<StoreAppDetails>>>()
    private val dr2Zones = ("AE AF AG AI AM AO AQ AR AS AW AZ BB BD BF BI BJ BM BN BO BR BS BT BV BW BY BZ CC CD CF CG CI CK CL CM CO CR CU CV CX DJ DM DO DZ EC EG EH ER ES ET FJ FK FM GA GD GE GF GH GM GN GP GQ GS GT GU GW GY HK HM HN HT ID IO IQ JM JO KE KG KH KI KW KZ LA LB LC LK LR LS LT LU LY MA MG MH ML MM MN MO MP MQ MR MS MU MV MW MX MY MZ NA NC NE NF NG NI NP NR NU OM PA PE PF PG PH PK PN PR PS PW PY QA RE RW SA SB SC SD SH SN SO SR SS SV SY SZ TC TD TF TG TH TJ TL TM TN TO TT TV TW TZ UG UY UZ VE VG VI VN VU WF WS YE YT ZA ZM ZW").split(' ').toSet()

    suspend fun search(query: String): HuaweiSearchResponse = withContext(Dispatchers.IO) {
        val term = query.trim()
        if (term.isBlank() || term.startsWith("http://", true) || term.startsWith("https://", true)) {
            return@withContext HuaweiSearchResponse(emptyList())
        }
        val requestLocale = localeForQuery(term)
        val key = "${requestLocale}:${term.lowercase(Locale.ROOT)}"
        searchCache[key]?.takeIf {
            val age = System.currentTimeMillis() - it.at
            age < if (it.value.apps.isEmpty()) EMPTY_SEARCH_CACHE_MS else SEARCH_CACHE_MS
        }?.let { cached ->
            cached.value.apps.forEach { app -> appLocaleById[app.sourceId] = requestLocale }
            return@withContext cached.value
        }
        try {
            var lastProblem = "未知错误"
            repeat(2) { attempt ->
                val active = ensureSession(
                    forceRefresh = attempt > 0,
                    connectTimeoutMs = SEARCH_CONNECT_TIMEOUT_MS,
                    readTimeoutMs = SEARCH_READ_TIMEOUT_MS
                )
                val response = clientApiPost(
                    active.host,
                    commonParams(active.deviceId, requestLocale) + mapOf(
                        "method" to "client.getTabDetail",
                        "sign" to active.sign,
                        "uri" to "searchApp|$term",
                        "maxResults" to "20",
                        "reqPageNum" to "1",
                        "isSupportPage" to "1"
                    ),
                    connectTimeoutMs = SEARCH_CONNECT_TIMEOUT_MS,
                    readTimeoutMs = SEARCH_READ_TIMEOUT_MS
                )
                if (response.optString("rtnCode") == "0") {
                    val found = mutableListOf<FdroidApp>()
                    val layouts = response.optJSONArray("layoutData") ?: JSONArray()
                    for (layoutIndex in 0 until layouts.length()) {
                        val layout = layouts.optJSONObject(layoutIndex) ?: continue
                        val items = layout.optJSONArray("dataList") ?: continue
                        for (itemIndex in 0 until items.length()) {
                            val item = items.optJSONObject(itemIndex) ?: continue
                            val app = item.optJSONObject("appInfo") ?: item
                            val appId = app.optString("appid").ifBlank { app.optString("appId") }.trim()
                            val name = firstNonBlank(app, "localizedName", "localName", "displayName", "appName", "name", "title", "label").trim()
                            if (appId.isBlank() || name.isBlank()) continue
                            appLocaleById[appId] = requestLocale
                            val packageName = firstNonBlank(app, "package", "packageName", "pkgName").ifBlank { "huawei:$appId" }
                            val realPackage = normalizedAndroidPackageName(packageName)
                            val icon = normalizeIconUrl(firstNonBlank(app, "iconUrl", "icon", "iconPath", "iconURL", "logo"))
                            val summary = firstNonBlank(app, "brief", "description", "summary", "intro", "appDesc")
                            val developer = htmlText(firstNonBlank(app, "developer", "developerName", "author", "publisher"))
                            val sizeBytes = app.catalogSizeBytes(
                                "apkSizeBytes", "apk_size_bytes", "fileSizeBytes", "file_size_bytes",
                                "apkSize", "apk_size", "appSize", "app_size", "fileSize", "file_size", "size"
                            )
                            found += FdroidApp(
                                packageName = packageName,
                                name = htmlText(name),
                                summary = htmlText(summary).ifBlank { "来自华为应用市场" },
                                iconUrl = icon,
                                source = AppCatalogSource.HUAWEI_APPGALLERY,
                                sourceId = appId,
                                sourceUrl = "https://appgallery.huawei.com/app/$appId",
                                appPackageName = realPackage,
                                developer = developer,
                                apkSizeBytes = sizeBytes
                            )
                        }
                    }
                    val unique = found.distinctBy { it.sourceId.lowercase() }
                    val ranked = AppSearchRanking.rankApps(term, unique)
                    return@withContext HuaweiSearchResponse(ranked).also {
                        searchCache[key] = Cached(System.currentTimeMillis(), it)
                    }
                }
                lastProblem = "${response.optString("rtnCode")} ${response.optString("rtnDesc")}".trim()
            }
            HuaweiSearchResponse(emptyList(), "华为应用市场暂时无法搜索（$lastProblem）。可稍后重试。")
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            HuaweiSearchResponse(emptyList(), "华为应用市场请求失败：${error.message ?: error.javaClass.simpleName}")
        }
    }

    suspend fun getAppDetails(appId: String): Result<StoreAppDetails> = withContext(Dispatchers.IO) {
        detailCache[appId]?.takeIf { System.currentTimeMillis() - it.at < SEARCH_CACHE_MS }?.let { return@withContext it.value }
        val result = runCatching {
            var lastProblem = "应用详情不可用"
            repeat(2) { attempt ->
                val active = ensureSession(forceRefresh = attempt > 0)
                val requestLocale = appLocaleById[appId] ?: defaultLocale()
                val response = clientApiPost(active.host, commonParams(active.deviceId, requestLocale) + mapOf(
                    "method" to "client.appDetailById",
                    "sign" to active.sign,
                    "id" to appId
                ))
                if (response.optString("rtnCode") == "0") {
                    val info = extractDetailInfo(response)
                        ?: throw IllegalStateException("华为应用市场未返回可识别的应用详情。")
                    val version = firstNonBlank(info, "versionName", "version").trim()
                    val rawDownload = firstNonBlank(info, "url", "downloadUrl").trim()
                    if (version.isBlank() || rawDownload.isBlank()) {
                        throw IllegalStateException("华为应用市场没有提供可用版本或 APK 下载链接。")
                    }
                    val downloadUrl = validateHuaweiDownloadUrl(rawDownload)
                    val name = firstNonBlank(info, "localizedName", "localName", "displayName", "appName", "name", "title").ifBlank { appId }
                    val packageName = firstNonBlank(info, "package", "packageName", "pkgName")
                    val developer = firstNonBlank(info, "developer", "developerName", "author", "developer_name")
                    val description = firstNonBlank(info, "appDesc", "description", "brief", "intro", "summary", "introduction")
                    val iconUrl = normalizeIconUrl(firstNonBlank(info, "iconUrl", "icon", "iconPath", "iconURL", "logo"))
                    val releaseDate = firstNonBlank(info, "releaseDate", "updateTime", "publishDate", "updatetime")
                    // The API response can include other app cards and recommendation media.
                    // Only take screenshot assets from the app detail payload, then supplement
                    // a short list with narrowly-scoped screenshot fields from the official page.
                    val apiScreenshots = extractScreenshotUrls(response, info, iconUrl)
                    val screenshotUrls = if (apiScreenshots.size < 4) {
                        dedupeScreenshotUrls(
                            apiScreenshots + fetchScreenshotUrlsFromOfficialPage(appId, requestLocale, iconUrl),
                            iconUrl
                        ).take(8)
                    } else apiScreenshots.take(8)
                    return@runCatching StoreAppDetails(
                        name = htmlText(name),
                        packageName = packageName,
                        developer = htmlText(developer),
                        versionName = htmlText(version),
                        description = htmlText(description),
                        iconUrl = iconUrl,
                        downloadUrl = downloadUrl,
                        sourceUrl = "https://appgallery.huawei.com/app/$appId",
                        releaseDate = releaseDate,
                        screenshotUrls = screenshotUrls,
                        apkSizeBytes = info.catalogSizeBytes(
                            "apkSizeBytes", "apk_size_bytes", "fileSizeBytes", "file_size_bytes",
                            "apkSize", "apk_size", "appSize", "app_size", "fileSize", "file_size", "downloadSize", "download_size", "size"
                        )
                    )
                }
                lastProblem = "${response.optString("rtnCode")} ${response.optString("rtnDesc")}".trim()
            }
            throw IllegalStateException("华为应用市场无法读取此应用（$lastProblem）。")
        }
        // Cache successful details only. A temporary region/CDN failure must not poison
        // this app's detail page for the full cache window, particularly during retries.
        if (result.isSuccess) {
            detailCache[appId] = Cached(System.currentTimeMillis(), result)
        } else {
            detailCache.remove(appId)
        }
        result
    }

    /**
     * Refresh only the signed download URL immediately before download. Search-result enrichment
     * also loads details and may cache a short-lived CDN URL; it must not be reused blindly when
     * the user later presses Install. This lightweight path intentionally skips screenshot/page
     * scraping so it does not delay starting the download.
     */
    suspend fun refreshDownloadDetails(appId: String, current: StoreAppDetails): Result<StoreAppDetails> =
        withContext(Dispatchers.IO) {
            val result = runCatching {
                var lastProblem = "下载地址不可用"
                repeat(2) { attempt ->
                    val active = ensureSession(forceRefresh = attempt > 0)
                    val requestLocale = appLocaleById[appId] ?: defaultLocale()
                    val response = clientApiPost(
                        active.host,
                        commonParams(active.deviceId, requestLocale) + mapOf(
                            "method" to "client.appDetailById",
                            "sign" to active.sign,
                            "id" to appId
                        )
                    )
                    if (response.optString("rtnCode") == "0") {
                        val info = extractDetailInfo(response)
                            ?: throw IllegalStateException("华为应用市场未返回可识别的应用详情。")
                        val version = firstNonBlank(info, "versionName", "version").trim()
                        val rawDownload = firstNonBlank(info, "url", "downloadUrl").trim()
                        if (version.isBlank() || rawDownload.isBlank()) {
                            throw IllegalStateException("华为应用市场没有提供可用版本或 APK 下载链接。")
                        }
                        val refreshed = current.copy(
                            name = htmlText(firstNonBlank(info, "localizedName", "localName", "displayName", "appName", "name", "title"))
                                .ifBlank { current.name },
                            packageName = firstNonBlank(info, "package", "packageName", "pkgName").ifBlank { current.packageName },
                            developer = htmlText(firstNonBlank(info, "developer", "developerName", "author", "developer_name"))
                                .ifBlank { current.developer },
                            versionName = htmlText(version),
                            iconUrl = normalizeIconUrl(firstNonBlank(info, "iconUrl", "icon", "iconPath", "iconURL", "logo"))
                                .ifBlank { current.iconUrl },
                            downloadUrl = validateHuaweiDownloadUrl(rawDownload),
                            releaseDate = firstNonBlank(info, "releaseDate", "updateTime", "publishDate", "updatetime")
                                .ifBlank { current.releaseDate },
                            apkSizeBytes = info.catalogSizeBytes(
                                "apkSizeBytes", "apk_size_bytes", "fileSizeBytes", "file_size_bytes",
                                "apkSize", "apk_size", "appSize", "app_size", "fileSize", "file_size",
                                "downloadSize", "download_size", "size"
                            ) ?: current.apkSizeBytes
                        )
                        return@runCatching refreshed
                    }
                    lastProblem = "${response.optString("rtnCode")} ${response.optString("rtnDesc")}".trim()
                }
                throw IllegalStateException("华为应用市场无法刷新下载地址（$lastProblem）。")
            }
            if (result.isSuccess) {
                detailCache[appId] = Cached(System.currentTimeMillis(), result)
            } else {
                detailCache.remove(appId)
            }
            result
        }

    private fun extractDetailInfo(response: JSONObject): JSONObject? {
        fun parse(value: Any?): JSONObject? = when (value) {
            is JSONObject -> value
            is JSONArray -> value.optJSONObject(0)
            is String -> runCatching {
                when (val decoded = JSONTokener(value).nextValue()) {
                    is JSONObject -> decoded
                    is JSONArray -> decoded.optJSONObject(0)
                    else -> null
                }
            }.getOrNull()
            else -> null
        }
        parse(response.opt("detailInfo"))?.let { return it }
        val data = response.optJSONObject("data") ?: return null
        return parse(data.opt("detailInfo")) ?: data.takeIf {
            firstNonBlank(it, "versionName", "version").isNotBlank() &&
                    firstNonBlank(it, "url", "downloadUrl").isNotBlank()
        }
    }

    /** Fetch details for a few top search results in the background to obtain real package names and APK sizes. */
    suspend fun enrichSearchApps(apps: List<FdroidApp>, limit: Int = 3): List<FdroidApp> = coroutineScope {
        val semaphore = Semaphore(2)
        val leading = apps.take(limit)
        val enriched = leading.map { app ->
            async(Dispatchers.IO) {
                semaphore.withPermit {
                    val hasPackage = normalizedAndroidPackageName(app.appPackageName).isNotBlank()
                    if (hasPackage && app.apkSizeBytes != null && app.developer.isNotBlank()) return@withPermit app
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

    @Synchronized
    private fun ensureSession(
        forceRefresh: Boolean,
        connectTimeoutMs: Int = DEFAULT_CONNECT_TIMEOUT_MS,
        readTimeoutMs: Int = DEFAULT_READ_TIMEOUT_MS
    ): Session {
        val now = System.currentTimeMillis()
        val current = session
        if (!forceRefresh && current != null && now - current.createdAt <= SESSION_MAX_AGE_MS) return current

        val random = SecureRandom()
        val deviceId = buildString(64) { repeat(64) { append("0123456789abcdef"[random.nextInt(16)]) } }
        val probe = front2(HOST_EU, 1, deviceId, connectTimeoutMs, readTimeoutMs)
        val serviceZone = probe.optString("serviceZone")
        val host = when {
            serviceZone == "CN" -> HOST_CN
            serviceZone == "RU" -> HOST_RU
            serviceZone in dr2Zones -> HOST_ASIA
            else -> HOST_EU
        }
        var sign = probe.optString("sign")
        if (forceRefresh || sign.isBlank()) {
            sign = front2(host, 0, deviceId, connectTimeoutMs, readTimeoutMs).optString("sign")
        }
        if (sign.isBlank()) throw IllegalStateException("华为应用市场会话初始化失败。")
        return Session(host, sign, deviceId, now).also { session = it }
    }

    private fun front2(
        host: String,
        needServiceZone: Int,
        deviceId: String,
        connectTimeoutMs: Int = DEFAULT_CONNECT_TIMEOUT_MS,
        readTimeoutMs: Int = DEFAULT_READ_TIMEOUT_MS
    ): JSONObject = clientApiPost(
        host,
        commonParams(deviceId) + mapOf(
            "method" to "client.front2",
            "version" to CLIENT_VERSION,
            "versionCode" to CLIENT_VERSION_CODE,
            "packageName" to "com.huawei.appmarket",
            "zone" to "1",
            "phoneType" to "Pixel 8 Pro",
            "firmwareVersion" to "16",
            "isFirstLaunch" to "1",
            "oobe" to "0",
            "needServiceZone" to needServiceZone.toString()
        ),
        connectTimeoutMs = connectTimeoutMs,
        readTimeoutMs = readTimeoutMs
    )

    private fun commonParams(deviceId: String, locale: String = defaultLocale()): Map<String, String> = mapOf(
        "ver" to "1.1",
        "locale" to locale,
        "serviceType" to "0",
        "ts" to System.currentTimeMillis().toString(),
        "net" to "1",
        "brand" to "google",
        "manufacturer" to "Google",
        "subBrand" to "0",
        "deviceId" to deviceId,
        "deviceIdType" to "9"
    )

    private fun clientApiPost(
        host: String,
        params: Map<String, String>,
        connectTimeoutMs: Int = DEFAULT_CONNECT_TIMEOUT_MS,
        readTimeoutMs: Int = DEFAULT_READ_TIMEOUT_MS
    ): JSONObject {
        val body = params.toSortedMap().entries.joinToString("&") { (key, value) ->
            "${URLEncoder.encode(key, "UTF-8")}=${URLEncoder.encode(value, "UTF-8").replace("%20", "+")}"
        }
        val connection = (URL("https://$host$API_PATH").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = connectTimeoutMs
            readTimeout = readTimeoutMs
            doOutput = true
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", USER_AGENT)
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        }
        try {
            connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val payload = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (status !in 200..299) throw IllegalStateException("华为接口 HTTP $status")
            return JSONObject(payload)
        } finally {
            connection.disconnect()
        }
    }

    private fun validateHuaweiDownloadUrl(raw: String): String {
        val url = URL(raw)
        val host = url.host.lowercase()
        val trusted = listOf("dbankcloud.com", "dbankcloud.ru").any { host == it || host.endsWith(".$it") }
        require(url.protocol.equals("https", true) && trusted) { "华为返回了非受信任的下载域名，已阻止下载。" }
        return url.toString()
    }

    /** Chinese queries should ask AppGallery for Chinese-localized names; otherwise follow device language. */
    private fun localeForQuery(query: String): String =
        if (query.any { it in '\u4E00'..'\u9FFF' }) "zh_CN" else defaultLocale()

    private fun defaultLocale(): String {
        val locale = Locale.getDefault()
        val language = locale.language.ifBlank { "en" }
        val country = locale.country
        return if (country.isBlank()) {
            if (language.equals("zh", ignoreCase = true)) "zh_CN" else language
        } else {
            "${language}_${country}"
        }
    }

    /**
     * Extracts only media containers belonging to the requested app detail record.
     * Earlier versions recursively scanned the entire response (including recommendation
     * cards) and accepted almost any image URL; that caused unrelated pictures and duplicates.
     */
    private fun extractScreenshotUrls(root: JSONObject, detail: JSONObject, iconUrl: String): List<String> {
        val found = linkedMapOf<String, String>()
        val explicitScreenshotKey = Regex("(?i)(screenshot|screen.?capture|app.?screen|show.?pic|picture.?list|picture.?urls|pic.?list|app.?img.?list|app.?image.?list|show.?picture.?list|gallery.?list|detail.?image.?list|display.?images|phone.?screenshots?|mobile.?screenshots?|app.?images|mobile.?images)")
        val mediaContainerKey = Regex("(?i)^(mediaData|mediaList|pictureList|pictureUrls|picList|appImgList|appImageList|showPicList|showPictureList|imageList|imageUrls|images|galleryList|previewList|detailImageList|displayImages|appImages|mobileImages)$")
        val urlKeys = setOf("url", "src", "image", "imageurl", "img", "imgurl", "picture", "pictureurl", "mediaurl", "path", "originalurl", "bigurl", "fullurl", "fileurl", "screenshoturl")
        val iconIdentity = screenshotIdentity(normalizeIconUrl(iconUrl))

        fun isExcludedField(key: String): Boolean {
            val lower = key.lowercase(Locale.ROOT).replace("_", "").replace("-", "")
            return listOf("icon", "logo", "avatar", "badge", "qrcode", "banner", "cover", "poster", "background", "advert", "promotion", "promobanner", "video", "watermark", "developerphoto", "publisherlogo").any { lower.contains(it) }
        }

        fun isScreenshotField(key: String): Boolean = explicitScreenshotKey.containsMatchIn(key)
        fun isMediaField(key: String): Boolean = mediaContainerKey.matches(key)
        fun looksLikeUrl(raw: String): Boolean = raw.trim().startsWith("https://", true) || raw.trim().startsWith("http://", true) || raw.trim().startsWith("//") || raw.trim().startsWith("/")

        fun addUrl(raw: String, explicitScreenshot: Boolean, mediaContext: Boolean) {
            var candidate = raw.trim().trim('"', '\'', '`', ',', ';', ']', '}', ')')
                .replace("\\/", "/")
                .replace("\\u002F", "/", ignoreCase = true)
                .replace("\\u003A", ":", ignoreCase = true)
                .replace("\\u0026", "&", ignoreCase = true)
                .replace("\\u003D", "=", ignoreCase = true)
                .replace("\\u003F", "?", ignoreCase = true)
                .replace("&amp;", "&", ignoreCase = true)
                .trim()
            if (candidate.isBlank() || candidate.equals("null", true)) return
            if (candidate.startsWith("%3A%2F%2F", true) || candidate.startsWith("https%3A", true)) {
                candidate = runCatching { java.net.URLDecoder.decode(candidate, "UTF-8") }.getOrDefault(candidate)
            }
            if (candidate.startsWith("//")) candidate = "https:$candidate"
            if (candidate.startsWith("http://", true)) candidate = "https://${candidate.substringAfter("://")}"
            if (!candidate.startsWith("https://", true) && candidate.startsWith("/")) {
                candidate = "https://appgallery.huawei.com$candidate"
            }
            if (!candidate.startsWith("https://", true)) return
            val normalized = normalizeIconUrl(candidate)
            val parsed = runCatching { URL(normalized) }.getOrNull() ?: return
            if (!parsed.protocol.equals("https", true) || parsed.host.isBlank()) return
            val identity = screenshotIdentity(normalized)
            if (identity.isBlank() || identity == iconIdentity) return
            // Explicit screenshot arrays may point to extensionless CDN URLs. Generic media
            // records must still look like image assets, to avoid accepting video/banner URLs.
            if (explicitScreenshot || (mediaContext && looksLikeImageResource(parsed))) {
                found.putIfAbsent(identity, normalized)
            }
        }

        lateinit var visit: (Any?, String, Boolean, Boolean, Int) -> Unit

        fun parseEmbeddedJson(raw: String, key: String, screenshotContext: Boolean, mediaContext: Boolean, depth: Int) {
            val value = raw.trim()
            if (!(value.startsWith("[") || value.startsWith("{"))) return
            val parsed = runCatching { JSONTokener(value).nextValue() }.getOrNull() ?: return
            visit(parsed, key, screenshotContext, mediaContext, depth + 1)
        }

        visit = fun(value: Any?, key: String, screenshotContext: Boolean, mediaContext: Boolean, depth: Int) {
            if (depth > 16 || value == null || found.size >= 32) return
            if (isExcludedField(key) && !isScreenshotField(key)) return
            val thisScreenshot = screenshotContext || isScreenshotField(key)
            val thisMedia = mediaContext || isMediaField(key) || thisScreenshot
            when (value) {
                is JSONObject -> {
                    val type = firstNonBlank(value, "type", "mediaType", "contentType", "imageType", "fileType", "kind", "scene").lowercase(Locale.ROOT)
                    if (listOf("icon", "logo", "avatar", "banner", "cover", "poster", "video", "advert", "promotion").any { type.contains(it) }) return
                    val typedScreenshot = type.contains("screenshot") || type.contains("screen_shot") || type.contains("screen shot") || type.contains("screen capture")
                    val typedImage = typedScreenshot || type.contains("image") || type.contains("picture") || type == "pic" || type == "photo" || type == "img"
                    val objectScreenshot = thisScreenshot || typedScreenshot
                    val objectMedia = thisMedia || typedImage
                    val keys = value.keys()
                    while (keys.hasNext() && found.size < 32) {
                        val childKey = keys.next()
                        if (isExcludedField(childKey) && !isScreenshotField(childKey)) continue
                        val child = value.opt(childKey)
                        val childScreenshot = objectScreenshot || isScreenshotField(childKey)
                        val childMedia = objectMedia || isMediaField(childKey) || childScreenshot
                        when (child) {
                            is JSONObject, is JSONArray -> {
                                if (childScreenshot || childMedia || isMediaField(childKey)) {
                                    visit(child, childKey, childScreenshot, childMedia, depth + 1)
                                }
                            }
                            is String -> {
                                val trimmed = child.trim()
                                when {
                                    isScreenshotField(childKey) -> {
                                        if (looksLikeUrl(trimmed)) addUrl(trimmed, true, true)
                                        else parseEmbeddedJson(child, childKey, true, true, depth + 1)
                                    }
                                    isMediaField(childKey) -> {
                                        if (looksLikeUrl(trimmed)) addUrl(trimmed, false, true)
                                        else parseEmbeddedJson(child, childKey, childScreenshot, true, depth + 1)
                                    }
                                    childKey.lowercase(Locale.ROOT) in urlKeys && (objectScreenshot || objectMedia) -> {
                                        if (looksLikeUrl(trimmed)) addUrl(trimmed, objectScreenshot, objectMedia)
                                        else parseEmbeddedJson(child, childKey, objectScreenshot, objectMedia, depth + 1)
                                    }
                                    (objectScreenshot || objectMedia) && (trimmed.startsWith("[") || trimmed.startsWith("{")) ->
                                        parseEmbeddedJson(child, childKey, objectScreenshot, objectMedia, depth + 1)
                                    // Do not accept arbitrary URL strings outside a known screenshot/media container.
                                }
                            }
                        }
                    }
                    if ((objectScreenshot || objectMedia) && found.size < 32) {
                        for (urlKey in urlKeys) {
                            if (isExcludedField(urlKey)) continue
                            val candidate = value.optString(urlKey)
                            if (candidate.isNotBlank() && candidate != "null") addUrl(candidate, objectScreenshot, objectMedia)
                        }
                    }
                }
                is JSONArray -> {
                    for (index in 0 until value.length()) {
                        if (found.size >= 32) break
                        when (val child = value.opt(index)) {
                            is JSONObject, is JSONArray -> visit(child, key, thisScreenshot, thisMedia, depth + 1)
                            is String -> {
                                val trimmed = child.trim()
                                when {
                                    thisScreenshot && looksLikeUrl(trimmed) -> addUrl(trimmed, true, true)
                                    thisMedia && looksLikeUrl(trimmed) -> addUrl(trimmed, false, true)
                                    thisScreenshot || thisMedia -> parseEmbeddedJson(child, key, thisScreenshot, thisMedia, depth + 1)
                                }
                            }
                        }
                    }
                }
                is String -> {
                    val trimmed = value.trim()
                    when {
                        thisScreenshot && looksLikeUrl(trimmed) -> addUrl(trimmed, true, true)
                        thisMedia && looksLikeUrl(trimmed) -> addUrl(trimmed, false, true)
                        thisScreenshot || thisMedia -> parseEmbeddedJson(trimmed, key, thisScreenshot, thisMedia, depth + 1)
                    }
                }
            }
        }

        // Scan the single-app detail record. The full response also contains layoutData and
        // recommendation cards, so never recursively crawl the entire response tree.
        visit(detail, "detailInfo", false, false, 0)
        // A few AppGallery builds put named media arrays beside detailInfo; accept only known
        // screenshot/media container names at the response root, never arbitrary layoutData.
        val rootKeys = root.keys()
        while (rootKeys.hasNext() && found.size < 32) {
            val key = rootKeys.next()
            if (isScreenshotField(key) || isMediaField(key)) {
                visit(root.opt(key), key, isScreenshotField(key), isMediaField(key), 0)
            }
        }
        return found.values.take(12)
    }

    private fun screenshotIdentity(raw: String): String {
        if (raw.isBlank()) return ""
        val parsed = runCatching { URL(raw) }.getOrNull() ?: return raw.substringBefore('#').substringBefore('?').lowercase(Locale.ROOT)
        // Screenshot CDNs often expose the same image with different resize/signature queries.
        // Ignore query/fragment for identity only; retain the original URL for the actual request.
        return "${parsed.host.lowercase(Locale.ROOT)}${parsed.path.trimEnd('/')}".ifBlank { "" }
    }

    private fun dedupeScreenshotUrls(urls: List<String>, iconUrl: String): List<String> {
        val iconIdentity = screenshotIdentity(normalizeIconUrl(iconUrl))
        val unique = linkedMapOf<String, String>()
        for (raw in urls) {
            val normalized = normalizeIconUrl(raw.trim())
            if (!normalized.startsWith("https://", true)) continue
            val identity = screenshotIdentity(normalized)
            if (identity.isBlank() || identity == iconIdentity) continue
            unique.putIfAbsent(identity, normalized)
            if (unique.size >= 16) break
        }
        return unique.values.toList()
    }

    private fun looksLikeImageResource(url: URL): Boolean {
        val pathAndQuery = (url.path + "?" + (url.query ?: "")).lowercase(Locale.ROOT)
        val imageExtension = Regex("\\.(png|jpe?g|webp|avif|gif|bmp)(?:$|[?&])").containsMatchIn(pathAndQuery)
        val imageHost = listOf("appimg", "dbankcdn", "hicloud", "hispace", "hwcloud", "image", "img", "vmall", "huaweicloud").any {
            url.host.lowercase(Locale.ROOT).contains(it)
        }
        val imagePathHint = Regex("(?i)(screen.?shot|screen.?capture|preview|gallery|image|picture|/pic|media|detail.?img)").containsMatchIn(url.path)
        return imageExtension || imageHost || imagePathHint
    }

    private data class EmbeddedJsonValue(val value: Any?, val raw: String)

    /** Read one balanced JSON value at [start] from the page's embedded state. */
    private fun readEmbeddedJsonValue(text: String, start: Int): EmbeddedJsonValue? {
        var i = start
        while (i < text.length && text[i].isWhitespace()) i++
        if (i >= text.length) return null
        if (text[i] == '"') {
            var escaped = false
            var end = i + 1
            while (end < text.length) {
                val c = text[end]
                if (c == '"' && !escaped) break
                if (c == '\\' && !escaped) escaped = true else escaped = false
                end++
            }
            if (end >= text.length) return null
            val raw = text.substring(i, end + 1)
            val parsed = runCatching { JSONTokener(raw).nextValue() }.getOrNull()
            val stringValue = parsed as? String
            val inner = stringValue?.trim()
            if (inner != null && (inner.startsWith("{") || inner.startsWith("["))) {
                val nested = runCatching { JSONTokener(inner).nextValue() }.getOrNull()
                if (nested != null) return EmbeddedJsonValue(nested, inner)
            }
            return EmbeddedJsonValue(parsed, raw)
        }
        if (text[i] != '{' && text[i] != '[') return null
        var depth = 0
        var inString = false
        var escaped = false
        var end = i
        while (end < text.length && end - i < 180_000) {
            val c = text[end]
            if (inString) {
                if (c == '"' && !escaped) inString = false
                if (c == '\\' && !escaped) escaped = true else escaped = false
            } else {
                when (c) {
                    '"' -> inString = true
                    '{', '[' -> depth++
                    '}', ']' -> {
                        depth--
                        if (depth == 0) {
                            end++
                            break
                        }
                    }
                }
            }
            end++
        }
        if (depth != 0 || end <= i) return null
        val raw = text.substring(i, end)
        val parsed = runCatching { JSONTokener(raw).nextValue() }.getOrNull()
        return EmbeddedJsonValue(parsed, raw)
    }

    /**
     * Narrow fallback: inspect only explicit screenshot/media fields in the official page state.
     * The previous broad before/after windows crossed into neighbouring cards and produced fake
     * screenshots from recommendations and other images on the page.
     */
    private fun fetchScreenshotUrlsFromOfficialPage(appId: String, requestLocale: String, iconUrl: String): List<String> {
        val connection = runCatching {
            (URL("https://appgallery.huawei.com/app/${URLEncoder.encode(appId, "UTF-8")}").openConnection() as HttpURLConnection).apply {
                connectTimeout = 2200
                readTimeout = 2600
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android ${android.os.Build.VERSION.RELEASE}) AppleWebKit/537.36 Chrome/131.0 Mobile Safari/537.36")
                setRequestProperty("Accept", "text/html,application/xhtml+xml,application/json;q=0.9,*/*;q=0.8")
                setRequestProperty("Accept-Language", "$requestLocale,en;q=0.7")
            }
        }.getOrNull() ?: return emptyList()
        val page = try {
            val status = connection.responseCode
            if (status !in 200..299) return emptyList()
            connection.inputStream.use { input ->
                val output = java.io.ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                var total = 0
                while (total < 1_200_000) {
                    val count = input.read(buffer, 0, minOf(buffer.size, 1_200_000 - total))
                    if (count <= 0) break
                    output.write(buffer, 0, count)
                    total += count
                }
                output.toString("UTF-8")
            }
        } catch (_: Exception) {
            ""
        } finally {
            connection.disconnect()
        }
        if (page.isBlank()) return emptyList()
        val html = page.replace("\\/", "/")
            .replace("\\u002F", "/", ignoreCase = true)
            .replace("\\u003A", ":", ignoreCase = true)
            .replace("\\u0026", "&", ignoreCase = true)
            .replace("&amp;", "&", ignoreCase = true)
        val fieldRegex = Regex("""(?i)["'](screenshots?|screen.?shot.?list|screen.?shot.?urls|phone.?screenshots?|mobile.?screenshots?|app.?screen.?shot|showPicList|showPictureList|pictureList|pictureUrls|picList|mediaData|mediaList|appImgList|appImageList|detailImageList|displayImages)["']\s*:""")
        val unique = linkedMapOf<String, String>()
        for (match in fieldRegex.findAll(html).take(40)) {
            val fieldName = match.groupValues[1]
            val parsed = readEmbeddedJsonValue(html, match.range.last + 1) ?: continue
            val value = parsed.value
            if (value != null) {
                val holder = JSONObject().put(fieldName, value)
                extractScreenshotUrls(JSONObject(), holder, iconUrl).forEach { candidate ->
                    val identity = screenshotIdentity(candidate)
                    if (identity.isNotBlank()) unique.putIfAbsent(identity, candidate)
                }
            } else if (isExplicitScreenshotField(fieldName)) {
                // If the page uses JSON-like rather than strict JSON for an explicit screenshot
                // array, inspect only that balanced value, never a wide page window.
                val rawUrl = Regex("(?i)(?:https?:)?//[^\\s\\\"'<>\\\\]+")
                for (candidateMatch in rawUrl.findAll(parsed.raw)) {
                    var candidate = candidateMatch.value.trimEnd(',', ';', ']', '}', ')')
                    if (candidate.startsWith("//")) candidate = "https:$candidate"
                    if (candidate.startsWith("http://", true)) candidate = "https://${candidate.substringAfter("://")}"
                    val parsedUrl = runCatching { URL(candidate) }.getOrNull() ?: continue
                    if (!looksLikeImageResource(parsedUrl)) continue
                    val safe = normalizeIconUrl(candidate)
                    val identity = screenshotIdentity(safe)
                    if (safe != normalizeIconUrl(iconUrl) && identity.isNotBlank()) unique.putIfAbsent(identity, safe)
                }
            }
            if (unique.size >= 12) break
        }
        return dedupeScreenshotUrls(unique.values.toList(), iconUrl).take(8)
    }

    private fun isExplicitScreenshotField(key: String): Boolean =
        Regex("(?i)(screenshot|screen.?capture|app.?screen|show.?pic|picture.?list|picture.?urls|pic.?list|app.?img.?list|app.?image.?list|show.?picture.?list|gallery.?list|detail.?image.?list|display.?images|phone.?screenshots?|mobile.?screenshots?|app.?images|mobile.?images)").containsMatchIn(key)

    private fun normalizeIconUrl(raw: String): String = when {
        raw.isBlank() -> ""
        raw.startsWith("https://", true) -> raw
        raw.startsWith("http://", true) -> "https://${raw.substringAfter("://")}"
        raw.startsWith("//") -> "https:$raw"
        raw.startsWith("/") -> "https://appgallery.huawei.com$raw"
        else -> "https://appgallery.huawei.com/$raw"
    }

    private fun firstNonBlank(json: JSONObject, vararg keys: String): String = keys.firstNotNullOfOrNull { key ->
        json.optString(key).takeIf { it.isNotBlank() && it != "null" }
    }.orEmpty()

    private fun htmlText(value: String): String = Html.fromHtml(value, Html.FROM_HTML_MODE_LEGACY).toString().trim()
}