package com.zionhuang.innertube.models

import com.zionhuang.innertube.models.endpoints.NavigationEndpoint

sealed class Section {
    abstract val header: Header?

    enum class ViewType {
        LIST, BLOCK
    }

    data class Header(
        val title: String,
        val subtitle: String? = null,
        val moreNavigationEndpoint: NavigationEndpoint? = null,
    )
}

data class ListSection(
    override val header: Header? = null,
    val items: List<Item>,
    val continuation: String? = null,
    val itemViewType: ViewType,
) : Section()

data class CarouselSection(
    override val header: Header? = null,
    val items: List<Item>,
    val numItemsPerColumn: Int = 1,
    val itemViewType: ViewType,
) : Section()

data class GridSection(
    override val header: Header? = null,
    val items: List<Item>,
//    val moreNavigationEndpoint: BrowseEndpoint,
) : Section()

data class DescriptionSection(
    override val header: Header? = null,
    val description: String,
) : Section()