package com.zionhuang.kugou

import com.github.houbb.opencc4j.util.ZhConverterUtil
import com.zionhuang.kugou.models.DownloadLyricsResponse
import com.zionhuang.kugou.models.SearchLyricsResponse
import com.zionhuang.kugou.models.SearchSongResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.encodeURLParameter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.decodeBase64String
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import java.lang.Character.UnicodeScript
import java.lang.Integer.min
import kotlin.math.abs

/**
 * KuGou Lyrics Library
 * Modified from [ViMusic](https://github.com/vfsfitvnm/ViMusic)
 */
object KuGou {
    var useTraditionalChinese: Boolean = false

    @OptIn(ExperimentalSerializationApi::class)
    private val client = HttpClient {
        expectSuccess = true

        install(ContentNegotiation) {
            val json = Json {
                ignoreUnknownKeys = true
                explicitNulls = false
                encodeDefaults = true
            }
            json(json)
            json(json, ContentType.Text.Html)
            json(json, ContentType.Text.Plain)
        }

        install(ContentEncoding) {
            gzip()
            deflate()
        }
    }

    suspend fun getLyrics(title: String, artist: String, duration: Int): Result<String> = runCatching {
        val keyword = generateKeyword(title, artist)
        getLyricsCandidate(keyword, duration)?.let { candidate ->
            downloadLyrics(candidate.id, candidate.accesskey).content.decodeBase64String().normalize(keyword)
        } ?: throw IllegalStateException("No lyrics candidate")
    }

    suspend fun getAllLyrics(title: String, artist: String, duration: Int, callback: (String) -> Unit) = runCatching {
        val keyword = generateKeyword(title, artist)
        searchSongs(keyword).data.info.forEach {
            if (duration == -1 || abs(it.duration - duration) <= DURATION_TOLERANCE) {
                searchLyricsByHash(it.hash).candidates.firstOrNull()?.let { candidate ->
                    downloadLyrics(candidate.id, candidate.accesskey)
                        .content
                        .decodeBase64String()
                        .normalize(keyword)
                        ?.let(callback)
                }
            }
        }
        searchLyricsByKeyword(keyword, duration).candidates.forEach { candidate ->
            downloadLyrics(candidate.id, candidate.accesskey)
                .content
                .decodeBase64String()
                .normalize(keyword)
                ?.let(callback)
        }
    }

    suspend fun getLyricsCandidate(keyword: Pair<String, String>, duration: Int): SearchLyricsResponse.Candidate? {
        searchSongs(keyword).data.info.forEach { song ->
            if (duration == -1 || abs(song.duration - duration) <= DURATION_TOLERANCE) { // if duration == -1, we don't care duration
                val candidate = searchLyricsByHash(song.hash).candidates.firstOrNull()
                if (candidate != null) return candidate
            }
        }
        return searchLyricsByKeyword(keyword, duration).candidates.firstOrNull()
    }

    private suspend fun searchSongs(keyword: Pair<String, String>) =
        client.get("https://mobileservice.kugou.com/api/v3/search/song") {
            parameter("version", 9108)
            parameter("plat", 0)
            parameter("pagesize", 8)
            parameter("showtype", 0)
            url.encodedParameters.append("keyword", "${keyword.first} - ${keyword.second}".encodeURLParameter(spaceToPlus = false))
        }.body<SearchSongResponse>()

    private suspend fun searchLyricsByKeyword(keyword: Pair<String, String>, duration: Int) =
        client.get("https://lyrics.kugou.com/search") {
            parameter("ver", 1)
            parameter("man", "yes")
            parameter("client", "pc")
            parameter("duration", duration.takeIf { it != -1 }?.times(1000)) // if duration == -1, we don't care duration
            url.encodedParameters.append("keyword", "${keyword.first} - ${keyword.second}".encodeURLParameter(spaceToPlus = false))
        }.body<SearchLyricsResponse>()

    private suspend fun searchLyricsByHash(hash: String) =
        client.get("https://lyrics.kugou.com/search") {
            parameter("ver", 1)
            parameter("man", "yes")
            parameter("client", "pc")
            parameter("hash", hash)
        }.body<SearchLyricsResponse>()

    private suspend fun downloadLyrics(id: Long, accessKey: String) =
        client.get("https://lyrics.kugou.com/download") {
            parameter("fmt", "lrc")
            parameter("charset", "utf8")
            parameter("client", "pc")
            parameter("ver", 1)
            parameter("id", id)
            parameter("accesskey", accessKey)
        }.body<DownloadLyricsResponse>()

    private fun normalizeTitle(title: String) = title
        .replace("\\(.*\\)".toRegex(), "")
        .replace("（.*）".toRegex(), "")
        .replace("「.*」".toRegex(), "")
        .replace("『.*』".toRegex(), "")
        .replace("<.*>".toRegex(), "")
        .replace("《.*》".toRegex(), "")
        .replace("〈.*〉".toRegex(), "")
        .replace("＜.*＞".toRegex(), "")

    private fun normalizeArtist(artist: String) = artist
        .replace(", ", "、")
        .replace(" & ", "、")
        .replace(".", "")
        .replace("和", "、")
        .replace("\\(.*\\)".toRegex(), "")
        .replace("（.*）".toRegex(), "")

    fun generateKeyword(title: String, artist: String) = normalizeTitle(title) to normalizeArtist(artist)

    private fun String.normalize(keyword: Pair<String, String>): String? =
        replace("&apos;", "'").lines().filter { line ->
            line matches ACCEPTED_REGEX
        }.let {
            // Remove useless information such as singer, writer, composer, guitar, etc.
            var headCutLine = 0
            for (i in min(30, it.lastIndex) downTo 0) {
                if (it[i] matches BANNED_REGEX) {
                    headCutLine = i + 1
                    break
                }
            }
            it.drop(headCutLine)
        }.let {
            var tailCutLine = 0
            for (i in min(it.size - 30, it.lastIndex) downTo 0) {
                if (it[it.lastIndex - i] matches BANNED_REGEX) {
                    tailCutLine = i + 1
                    break
                }
            }
            it.dropLast(tailCutLine)
        }.takeIf {
            it.isNotEmpty() && "纯音乐，请欣赏" !in it[0]
        }?.let { lines ->
            val firstLine = lines.firstOrNull()?.toSimplifiedChinese() ?: return@let lines
            val (title, artist) = keyword
            if (title.toSimplifiedChinese() in firstLine ||
                artist.split("、").any { it.toSimplifiedChinese() in firstLine }
            ) {
                lines.drop(1)
            } else lines
        }?.joinToString(separator = "\n")?.let {
            if (useTraditionalChinese) it.normalizeForTraditionalChinese()
            else it
        }

    private fun String.normalizeForTraditionalChinese() =
        if (none { c -> UnicodeScript.of(c.code) in JapaneseUnicodeScript }) toTraditionalChinese()
            .replace('着', '著')
            .replace('羣', '群')
        else this

    private fun String.toSimplifiedChinese() = ZhConverterUtil.toSimple(this)
    private fun String.toTraditionalChinese() = ZhConverterUtil.toTraditional(this)

    @Suppress("RegExpRedundantEscape")
    private val ACCEPTED_REGEX = "\\[(\\d\\d):(\\d\\d)\\.(\\d{2,3})\\].*".toRegex()
    private val BANNED_REGEX = ".+].+[:：].+".toRegex()

    private val JapaneseUnicodeScript = hashSetOf(
        UnicodeScript.HIRAGANA,
        UnicodeScript.KATAKANA,
    )

    private const val DURATION_TOLERANCE = 8
}
