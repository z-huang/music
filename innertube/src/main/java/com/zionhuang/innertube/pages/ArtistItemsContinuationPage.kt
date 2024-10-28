package com.zionhuang.innertube.pages

import com.zionhuang.innertube.models.YTItem

data class ArtistItemsContinuationPage(
    val items: List<YTItem>,
    val continuation: String?,
)
