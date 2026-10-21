package com.example.appspace

import org.json.JSONObject
import java.util.Locale

/** Store metadata normalized for AppSpace's source-independent detail page. */
data class StoreAppDetails(
    val name: String,
    val packageName: String = "",
    val developer: String = "",
    val versionName: String = "",
    val description: String = "",
    val iconUrl: String = "",
    val downloadUrl: String = "",
    val sourceUrl: String = "",
    val releaseDate: String = "",
    val screenshotUrls: List<String> = emptyList(),
    /** APK size reported by the catalog, in bytes. Null means the store did not report it. */
    val apkSizeBytes: Long? = null,
    /** True when a source can provide a download link lazily (e.g. Uptodown tokenized links). */
    val downloadAvailable: Boolean = true
)

/** Common search result shape shared by non-F-Droid catalogs. */
data class StoreCatalogSearchResponse(
    val apps: List<FdroidApp>,
    val error: String? = null
)

private val androidPackageNamePattern = Regex("[A-Za-z][A-Za-z0-9_]*(?:\\.[A-Za-z0-9_]+)+")

/** Only accept a syntactically plausible Android package name, never source IDs such as `vivo:123`. */
fun normalizedAndroidPackageName(value: String?): String = value.orEmpty().trim()
    .takeIf { it.length <= 255 && androidPackageNamePattern.matches(it) }
    .orEmpty()

/**
 * Clean storefront marketing suffixes without destroying real product variants.
 * - Truncates descriptive suffixes after a spaced dash ("App - official download").
 * - Removes low-value parentheticals such as "(Official)" or "(Android App)".
 * - Retains meaningful variants, regions, architectures, versions, and common aliases.
 */
fun cleanAppDisplayName(rawValue: String): String {
    val raw = rawValue
        .replace('\u00a0', ' ')
        .replace(Regex("[\\t\\r\\n]+"), " ")
        .replace(Regex("\\s{2,}"), " ")
        .trim()
    if (raw.isBlank()) return ""

    val meaningfulParenthetical = Regex(
        "(?i)(?:\\b(?:lite|pro|plus|max|mini|beta|alpha|preview|nightly|stable|canary|dev|global|international|intl|wechat|weixin|rednote|xiaohongshu|douyin|tiktok|arm64|armv7|armeabi(?:-v7a)?|x86_64|x86|64[- ]?bit|32[- ]?bit)\\b|\\bv?\\d+(?:\\.\\d+){1,4}(?:[-+][a-z0-9._-]+)?\\b|国际|海外|轻量|专业|测试版|预览版|稳定版|极速版|国际版|专业版|轻量版|海外版|正式版|开发版|平板版|折叠屏版)"
    )
    val parenthetical = Regex("[（(]([^()（）]*)[）)]")
    var name = parenthetical.replace(raw) { match ->
        val content = match.groupValues[1].trim()
        if (content.isNotBlank() && meaningfulParenthetical.containsMatchIn(content)) " ($content)" else ""
    }

    // Only split on a spaced dash. Internal hyphens in names such as F-Droid,
    // Droid-ify, and app identifiers are therefore preserved.
    name = name.split(Regex("\\s+[-–—]\\s+"), limit = 2).firstOrNull().orEmpty()
    name = name
        .replace(Regex("\\s{2,}"), " ")
        .trim()
        .trim('-', '–', '—', '|', ':', '·', ' ')
    return name.ifBlank { raw }
}

/** Best-effort conversion of store size values into bytes. Explicit units always win. */
fun parseCatalogSizeBytes(rawValue: Any?, fieldName: String = ""): Long? {
    if (rawValue == null || rawValue == JSONObject.NULL) return null
    val raw = rawValue.toString().trim()
    if (raw.isBlank() || raw.equals("null", true) || raw == "-" || raw == "N/A") return null
    val match = Regex("(?i)([0-9]+(?:[.,][0-9]+)?)\\s*(bytes?|b|kib|kb|k|mib|mb|m|gib|gb|g|tib|tb|t)?\\b")
        .find(raw) ?: return null
    val number = match.groupValues[1].replace(',', '.').toDoubleOrNull() ?: return null
    if (number <= 0.0) return null
    val unit = match.groupValues.getOrNull(2).orEmpty().lowercase(Locale.ROOT)
    val key = fieldName.lowercase(Locale.ROOT)
    val multiplier = when {
        unit in setOf("b", "byte", "bytes") || key.contains("bytes") -> 1.0
        unit in setOf("kb", "kib", "k") || key.contains("_kb") || key.endsWith("kb") -> 1024.0
        unit in setOf("gb", "gib", "g") || key.contains("_gb") || key.endsWith("gb") -> 1024.0 * 1024.0 * 1024.0
        unit in setOf("tb", "tib", "t") -> 1024.0 * 1024.0 * 1024.0 * 1024.0
        unit in setOf("mb", "mib", "m") || key.contains("_mb") || key.endsWith("mb") -> 1024.0 * 1024.0
        // Many store payloads send a raw byte count; smaller numeric values are commonly MB or KB.
        number >= 100_000.0 -> 1.0
        number >= 10_000.0 -> 1024.0
        else -> 1024.0 * 1024.0
    }
    val bytes = number * multiplier
    return bytes.toLong().takeIf { it > 0L && it < Long.MAX_VALUE.toDouble() }
}

/** Looks for common package-size fields in a JSON object without assuming one catalog's schema. */
fun JSONObject.catalogSizeBytes(vararg keys: String): Long? {
    for (key in keys) {
        if (!has(key) || opt(key) == JSONObject.NULL) continue
        parseCatalogSizeBytes(opt(key), key)?.let { return it }
    }
    return null
}

fun formatCatalogSize(bytes: Long?): String? {
    if (bytes == null || bytes <= 0L) return null
    val locale = Locale.getDefault()
    val mib = 1024.0 * 1024.0
    val gib = mib * 1024.0
    return when {
        bytes >= gib -> String.format(locale, "%.1f GB", bytes / gib)
        bytes >= mib -> String.format(locale, "%.1f MB", bytes / mib)
        bytes >= 1024L -> String.format(locale, "%.0f KB", bytes / 1024.0)
        else -> "$bytes B"
    }
}
