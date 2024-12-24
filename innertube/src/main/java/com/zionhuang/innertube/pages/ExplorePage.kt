package com.zionhuang.innertube.pages

import com.zionhuang.innertube.models.AlbumItem
import com.zionhuang.innertube.models.MoodAndGenres

data class ExplorePage(
    val newReleaseAlbums: List<AlbumItem>,
    val moodAndGenres: List<MoodAndGenres.Item>,
)
