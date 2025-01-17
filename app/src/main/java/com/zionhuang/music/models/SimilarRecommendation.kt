package com.zionhuang.music.models

import com.zionhuang.innertube.models.YTItem
import com.zionhuang.music.db.entities.LocalItem

data class SimilarRecommendation(
    val title: LocalItem,
    val items: List<YTItem>,
)
