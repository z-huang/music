package com.zionhuang.music.utils

import android.net.ConnectivityManager
import androidx.media3.common.PlaybackException
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.YouTubeClient
import com.zionhuang.innertube.models.YouTubeClient.Companion.IOS
import com.zionhuang.innertube.models.YouTubeClient.Companion.WEB_CREATOR
import com.zionhuang.innertube.models.YouTubeClient.Companion.WEB_REMIX
import com.zionhuang.innertube.models.response.PlayerResponse
import com.zionhuang.music.constants.AudioQuality
import com.zionhuang.music.db.entities.FormatEntity
import okhttp3.OkHttpClient

object YTPlayerUtils {

    private val httpClient = OkHttpClient.Builder().build()

    /**
     * The main client is used for metadata and initial streams.
     * Do not use other clients for this because it can result in inconsistent metadata.
     * For example other clients can have different normalization targets (loudnessDb).
     *
     * [com.zionhuang.innertube.models.YouTubeClient.WEB_REMIX] should be preferred here because currently it is the only client which provides:
     * - the correct metadata (like loudnessDb)
     * - premium formats
     */
    private val MAIN_CLIENT: YouTubeClient = WEB_REMIX

    /**
     * Clients used for fallback streams in case the streams of the main client do not work.
     */
    private val STREAM_FALLBACK_CLIENTS: Array<YouTubeClient> = arrayOf(WEB_CREATOR, IOS)

    data class PlaybackData(
        val audioConfig: PlayerResponse.PlayerConfig.AudioConfig?,
        val videoDetails: PlayerResponse.VideoDetails?,
        val format: PlayerResponse.StreamingData.Format,
        val streamUrl: String,
        val streamExpiresInSeconds: Int,
    )

    /**
     * Custom player response intended to use for playback.
     * Metadata like audioConfig and videoDetails are from [MAIN_CLIENT].
     * Format & stream can be from [MAIN_CLIENT] or [STREAM_FALLBACK_CLIENTS].
     */
    suspend fun playerResponseForPlayback(
        videoId: String,
        playlistId: String? = null,
        playedFormat: FormatEntity?,
        audioQuality: AudioQuality,
        connectivityManager: ConnectivityManager,
    ): Result<PlaybackData> = runCatching {
        val mainPlayerResponse =
            YouTube.player(videoId, playlistId, client = MAIN_CLIENT).getOrThrow()

        val audioConfig = mainPlayerResponse.playerConfig?.audioConfig
        val videoDetails = mainPlayerResponse.videoDetails

        var format: PlayerResponse.StreamingData.Format? = null
        var streamUrl: String? = null
        var streamExpiresInSeconds: Int? = null

        if (mainPlayerResponse.playabilityStatus.status == "OK") {
            format = findFormat(
                mainPlayerResponse,
                playedFormat,
                audioQuality,
                connectivityManager,
            )
            if (format != null) {
                streamUrl = format.findUrl()
                streamExpiresInSeconds = mainPlayerResponse.streamingData?.expiresInSeconds
                if (streamUrl != null && streamExpiresInSeconds != null && validateStatus(streamUrl)) {
                    return@runCatching PlaybackData(
                        audioConfig,
                        videoDetails,
                        format,
                        streamUrl,
                        streamExpiresInSeconds,
                    )
                }
            }
        }

        var fallbackPlayerResponse: PlayerResponse? = null
        for (client in STREAM_FALLBACK_CLIENTS) {
            // reset for each client
            format = null
            streamUrl = null
            streamExpiresInSeconds = null

            // process current client response
            fallbackPlayerResponse =
                YouTube.player(videoId, playlistId, client).getOrNull()
            if (fallbackPlayerResponse?.playabilityStatus?.status == "OK") {
                format =
                    findFormat(
                        fallbackPlayerResponse,
                        playedFormat,
                        audioQuality,
                        connectivityManager,
                    ) ?: continue
                streamUrl = format.findUrl() ?: continue
                streamExpiresInSeconds = fallbackPlayerResponse.streamingData?.expiresInSeconds ?: continue

                if (validateStatus(streamUrl)) {
                    // working stream found
                    break
                }
            }
        }

        if (fallbackPlayerResponse == null) {
            throw Exception("Bad fallback player response")
        }
        if (fallbackPlayerResponse.playabilityStatus.status != "OK") {
            throw PlaybackException(
                fallbackPlayerResponse.playabilityStatus.reason,
                null,
                PlaybackException.ERROR_CODE_REMOTE_ERROR
            )
        }
        if (streamExpiresInSeconds == null) {
            throw Exception("Missing stream expire time")
        }
        if (format == null) {
            throw Exception("Could not find format")
        }
        if (streamUrl == null) {
            throw Exception("Could not find stream url")
        }

        PlaybackData(
            audioConfig,
            videoDetails,
            format,
            streamUrl,
            streamExpiresInSeconds,
        )
    }

    /**
     * Simple player response intended to use for metadata only.
     * Stream URLs of this response might not work so don't use them.
     */
    suspend fun playerResponseForMetadata(
        videoId: String,
        playlistId: String? = null,
    ): Result<PlayerResponse> =
        YouTube.player(videoId, playlistId, client = MAIN_CLIENT)

    private fun findFormat(
        playerResponse: PlayerResponse,
        playedFormat: FormatEntity?,
        audioQuality: AudioQuality,
        connectivityManager: ConnectivityManager,
    ): PlayerResponse.StreamingData.Format? =
        if (playedFormat != null) {
            playerResponse.streamingData?.adaptiveFormats?.find { it.itag == playedFormat.itag }
        } else {
            playerResponse.streamingData?.adaptiveFormats
                ?.filter { it.isAudio }
                ?.maxByOrNull {
                    it.bitrate * when (audioQuality) {
                        AudioQuality.AUTO -> if (connectivityManager.isActiveNetworkMetered) -1 else 1
                        AudioQuality.HIGH -> 1
                        AudioQuality.LOW -> -1
                    } + (if (it.mimeType.startsWith("audio/webm")) 10240 else 0) // prefer opus stream
                }
        }

    private fun validateStatus(url: String): Boolean {
        val requestBuilder = okhttp3.Request.Builder()
            .head()
            .url(url)
        val response = httpClient.newCall(requestBuilder.build()).execute()
        return response.isSuccessful
    }
}