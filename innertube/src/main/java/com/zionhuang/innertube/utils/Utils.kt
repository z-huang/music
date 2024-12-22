package com.zionhuang.innertube.utils

import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.ResponseContext
import com.zionhuang.innertube.models.Thumbnail
import com.zionhuang.innertube.models.Thumbnails
import com.zionhuang.innertube.models.response.PlayerResponse
import com.zionhuang.innertube.pages.PlaylistPage
import org.schabi.newpipe.extractor.stream.StreamInfo
import java.security.MessageDigest

suspend fun Result<PlaylistPage>.completed() = runCatching {
    val page = getOrThrow()
    val songs = page.songs.toMutableList()
    var continuation = page.songsContinuation
    while (continuation != null) {
        val continuationPage = YouTube.playlistContinuation(continuation).getOrNull() ?: break
        songs += continuationPage.songs
        continuation = continuationPage.continuation
    }
    PlaylistPage(
        playlist = page.playlist,
        songs = songs,
        songsContinuation = null,
        continuation = page.continuation
    )
}

fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }

fun sha1(str: String): String = MessageDigest.getInstance("SHA-1").digest(str.toByteArray()).toHex()

fun parseCookieString(cookie: String): Map<String, String> =
    cookie.split("; ")
        .filter { it.isNotEmpty() }
        .associate {
            val (key, value) = it.split("=")
            key to value
        }

fun String.parseTime(): Int? {
    try {
        val parts = split(":").map { it.toInt() }
        if (parts.size == 2) {
            return parts[0] * 60 + parts[1]
        }
        if (parts.size == 3) {
            return parts[0] * 3600 + parts[1] * 60 + parts[2]
        }
    } catch (e: Exception) {
        return null
    }
    return null
}

/**
 * Maps NewPipeExtractor StreamInfo to InnerTube PlayerResponse. Not a perfect match as there are
 * some parameters missing, but this will allow us to play streams correctly.
 */
fun StreamInfo.mapToPlayerResponse() = PlayerResponse(
    responseContext = ResponseContext(null, null),
    playabilityStatus = PlayerResponse.PlayabilityStatus("OK", null),
    playerConfig = null,
    streamingData = PlayerResponse.StreamingData(
        formats = null,
        adaptiveFormats = audioStreams.map { stream ->
            PlayerResponse.StreamingData.Format(
                itag = stream.itag,
                url = stream.content,
                mimeType = "${stream.format?.mimeType}; codecs=\"${stream.codec}\"",
                bitrate = stream.bitrate,
                width = null,
                height = null,
                contentLength = stream.content.substringAfter("clen=").substringBefore("&").toLongOrNull() ?: 10000000,
                quality = stream.quality,
                fps = null,
                qualityLabel = null,
                averageBitrate = stream.averageBitrate,
                audioQuality = null,
                approxDurationMs = null,
                audioSampleRate = null,
                audioChannels = null,
                loudnessDb = null,
                lastModified = null,
            )
        },
        expiresInSeconds = 21540 // NewPipeExtractor doesn't give us this data, but it seems to always be this value
    ),
    videoDetails = PlayerResponse.VideoDetails(
        videoId = id,
        title = name,
        author = uploaderName,
        channelId = uploaderUrl.removePrefix("https://www.youtube.com/channel/"),
        lengthSeconds = duration.toString(),
        musicVideoType = null,
        viewCount = viewCount.toString(),
        thumbnail = Thumbnails(
            thumbnails = thumbnails.map {
                Thumbnail(it.url, it.width, it.height)
            }
        )
    )
)

