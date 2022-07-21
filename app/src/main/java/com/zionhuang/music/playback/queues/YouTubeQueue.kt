package com.zionhuang.music.playback.queues

import com.google.android.exoplayer2.MediaItem
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.Item
import com.zionhuang.innertube.models.WatchEndpoint
import com.zionhuang.music.extensions.toMediaItem
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class YouTubeQueue(
    private val endpoint: WatchEndpoint,
    val item: Item? = null,
) : Queue {
    override val title: String? = null

    private var continuation: String? = null

    override suspend fun getInitialStatus(): Queue.Status {
        val nextResult = withContext(IO) { YouTube.next(endpoint, continuation) }
        continuation = nextResult.continuation
        return Queue.Status(
            items = nextResult.items.mapNotNull { it.toMediaItem() },
            index = nextResult.currentIndex ?: 0
        )
    }

    override fun hasNextPage(): Boolean = continuation != null

    override suspend fun nextPage(): List<MediaItem> {
        val nextResult = withContext(IO) { YouTube.next(endpoint, continuation) }
        continuation = nextResult.continuation
        return nextResult.items.mapNotNull { it.toMediaItem() }
    }
}