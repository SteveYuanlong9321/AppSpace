package com.example.appspace

import java.text.Normalizer
import java.util.Locale

/** Query-aware ranking shared by all catalogs so exact matches are source-independent. */
object AppSearchRanking {
    private val aliases = mapOf(
        "小红书" to listOf("xiaohongshu", "rednote"),
        "小紅書" to listOf("xiaohongshu", "rednote"),
        "微信" to listOf("wechat", "weixin"),
        "抖音" to listOf("douyin", "tiktok"),
        "淘宝" to listOf("taobao"),
        "支付宝" to listOf("alipay"),
        "快手" to listOf("kuaishou"),
        "哔哩哔哩" to listOf("bilibili", "b站"),
        "b站" to listOf("bilibili", "哔哩哔哩"),
        "微博" to listOf("weibo"),
        "京东" to listOf("jd", "jingdong"),
        "美团" to listOf("meituan"),
        "百度" to listOf("baidu"),
        "高德地图" to listOf("amap", "gaode maps"),
        "酷安" to listOf("coolapk"),
        "qq音乐" to listOf("qq music"),
        "网易云音乐" to listOf("netease cloud music", "netease music"),
        "腾讯视频" to listOf("tencent video"),
        "爱奇艺" to listOf("iqiyi"),
        "优酷" to listOf("youku"),
        "飞书" to listOf("lark", "feishu"),
        "钉钉" to listOf("dingtalk", "ding talk")
    )

    /**
     * Sort by the strongest match across the entire merged record, including alternate stores.
     * No catalog/source order is used as a ranking rule.
     */
    fun rankApps(query: String, apps: List<FdroidApp>): List<FdroidApp> =
        apps.sortedWith(
            compareByDescending<FdroidApp> { score(query, it) }
                .thenByDescending { bestTitleScore(query, it) }
                .thenBy { titleDistance(query, it) }
                .thenByDescending { hasAnyIcon(it) }
                .thenBy { normalize(cleanAppDisplayName(it.name)) }
                .thenBy { it.packageName.lowercase(Locale.ROOT) }
                .thenBy { it.sourceId.lowercase(Locale.ROOT) }
        )

    fun bestScore(query: String, apps: List<FdroidApp>): Int =
        apps.maxOfOrNull { score(query, it) } ?: 0

    /** The best match across the primary result and any merged source variants. */
    fun score(query: String, app: FdroidApp): Int {
        val candidates = buildList {
            add(ScoreCandidate(app.name, app.packageName, app.summary))
            app.alternateSources.forEach { variant ->
                add(ScoreCandidate(variant.name, variant.packageName, variant.summary))
            }
        }
        return candidates.maxOfOrNull { scoreSingle(query, it) } ?: 0
    }

    private data class ScoreCandidate(val name: String, val packageName: String, val summary: String)

    private fun scoreSingle(query: String, app: ScoreCandidate): Int {
        val forms = queryForms(query)
        if (forms.isEmpty()) return 0
        val title = normalize(cleanAppDisplayName(app.name))
        val packageName = normalize(
            app.packageName.removePrefix("huawei:")
        )
        val summary = normalize(app.summary)
        var best = 0
        val primaryForm = normalize(query)

        for (form in forms) {
            if (form.isBlank()) continue
            val formScore = when {
                title == form -> 1000
                title.startsWith(form) -> 910
                title.split(' ').any { it.startsWith(form) } -> 860
                title.contains(form) -> 810
                allTokensMatch(form, title) -> 740
                packageName == form -> 780
                packageName.contains(form) -> 430
                summary.contains(form) -> 210
                summary.split(' ').any { it.startsWith(form) } -> 180
                else -> 0
            }
            // A literal display-name match outranks a cross-language alias match.
            best = maxOf(best, if (form == primaryForm) formScore else (formScore - 30).coerceAtLeast(0))
        }

        val queryTokens = normalize(query).split(' ').filter { it.isNotBlank() }
        if (queryTokens.size > 1 && queryTokens.all { token -> title.contains(token) }) {
            best = maxOf(best, 750)
        }
        return best
    }

    private fun bestTitleScore(query: String, app: FdroidApp): Int {
        val primary = titleScore(query, cleanAppDisplayName(app.name))
        val alternate = app.alternateSources.maxOfOrNull { titleScore(query, cleanAppDisplayName(it.name)) } ?: 0
        return maxOf(primary, alternate)
    }

    private fun titleScore(query: String, name: String): Int {
        val title = normalize(name)
        if (title.isBlank()) return 0
        val forms = queryForms(query)
        return forms.maxOfOrNull { form ->
            when {
                title == form -> if (form == normalize(query)) 1000 else 970
                title.startsWith(form) -> if (form == normalize(query)) 910 else 880
                title.contains(form) -> if (form == normalize(query)) 810 else 780
                allTokensMatch(form, title) -> 740
                else -> 0
            }
        } ?: 0
    }

    /** Lower is better: prefer names with less unrelated text after a partial match. */
    private fun titleDistance(query: String, app: FdroidApp): Int {
        fun distance(name: String): Int {
            val title = normalize(cleanAppDisplayName(name))
            val forms = queryForms(query)
            val matchingForm = forms.filter { form -> title.contains(form) || allTokensMatch(form, title) }
                .minByOrNull { it.length } ?: return Int.MAX_VALUE / 4
            return (title.length - matchingForm.length).coerceAtLeast(0)
        }
        return (listOf(distance(app.name)) + app.alternateSources.map { distance(it.name) }).minOrNull()
            ?: Int.MAX_VALUE / 4
    }

    private fun hasAnyIcon(app: FdroidApp): Boolean =
        app.iconUrl.isNotBlank() || app.alternateSources.any { it.iconUrl.isNotBlank() }

    private fun queryForms(query: String): Set<String> {
        val original = normalize(query)
        if (original.isBlank()) return emptySet()
        val expanded = linkedSetOf(original)
        aliases.forEach { (key, values) ->
            val normalizedKey = normalize(key)
            val normalizedValues = values.map(::normalize)
            if (original == normalizedKey || normalizedValues.any { original == it }) {
                expanded += normalizedKey
                expanded += normalizedValues
            }
        }
        return expanded
    }

    private fun allTokensMatch(query: String, title: String): Boolean {
        val tokens = query.split(' ').filter { it.isNotBlank() }
        return tokens.isNotEmpty() && tokens.all(title::contains)
    }

    private fun normalize(value: String): String = Normalizer.normalize(value, Normalizer.Form.NFKD)
        .lowercase(Locale.ROOT)
        .replace(Regex("\\p{M}+"), "")
        .replace(Regex("[^\\p{L}\\p{N}]+"), " ")
        .trim()
        .replace(Regex("\\s+"), " ")
}