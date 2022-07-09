package com.zionhuang.innertube.models

sealed class BaseItem {
    abstract val title: String
}

sealed class Item : BaseItem() {
    abstract override val title: String
    abstract val subtitle: String?
    abstract val thumbnails: List<Thumbnail>
    abstract val menu: ItemMenu
    abstract val navigationEndpoint: NavigationEndpoint

    interface FromContent<out T : Item> {
        fun from(item: MusicResponsiveListItemRenderer): T
        fun from(item: MusicTwoRowItemRenderer): T
    }
}

data class SongItem(
    override val title: String,
    override val subtitle: String,
    val index: String? = null,
    override val thumbnails: List<Thumbnail>,
    override val menu: ItemMenu,
    override val navigationEndpoint: NavigationEndpoint,
) : Item() {
    companion object : FromContent<SongItem> {
        override fun from(item: MusicResponsiveListItemRenderer): SongItem = SongItem(
            title = item.getTitle(),
            subtitle = item.getSubtitle(),
            index = item.index?.toString(),
            thumbnails = item.thumbnail?.getThumbnails().orEmpty(),
            menu = item.menu.toItemMenu(),
            navigationEndpoint = item.flexColumns[0].musicResponsiveListItemFlexColumnRenderer.text.runs[0].navigationEndpoint!!
        )

        override fun from(item: MusicTwoRowItemRenderer): SongItem = SongItem(
            title = item.title.toString(),
            subtitle = item.subtitle.toString(),
            thumbnails = item.thumbnailRenderer.getThumbnails(),
            menu = item.menu.toItemMenu(),
            navigationEndpoint = item.navigationEndpoint
        )
    }
}

data class VideoItem(
    override val title: String,
    override val subtitle: String,
    override val thumbnails: List<Thumbnail>,
    override val menu: ItemMenu,
    override val navigationEndpoint: NavigationEndpoint,
) : Item() {
    companion object : FromContent<VideoItem> {
        override fun from(item: MusicResponsiveListItemRenderer): VideoItem = VideoItem(
            title = item.getTitle(),
            subtitle = item.getSubtitle(),
            thumbnails = item.thumbnail!!.getThumbnails(),
            menu = item.menu.toItemMenu(),
            navigationEndpoint = item.flexColumns[0].musicResponsiveListItemFlexColumnRenderer.text.runs[0].navigationEndpoint!!
        )

        override fun from(item: MusicTwoRowItemRenderer): VideoItem = VideoItem(
            title = item.title.toString(),
            subtitle = item.subtitle.toString(),
            thumbnails = item.thumbnailRenderer.getThumbnails(),
            menu = item.menu.toItemMenu(),
            navigationEndpoint = item.navigationEndpoint
        )
    }
}

data class AlbumItem(
    override val title: String,
    override val subtitle: String,
    override val thumbnails: List<Thumbnail>,
    override val menu: ItemMenu,
    override val navigationEndpoint: NavigationEndpoint,
) : Item() {
    companion object : FromContent<AlbumItem> {
        override fun from(item: MusicResponsiveListItemRenderer): AlbumItem = AlbumItem(
            title = item.getTitle(),
            subtitle = item.getSubtitle(),
            thumbnails = item.thumbnail!!.getThumbnails(),
            menu = item.menu.toItemMenu(),
            navigationEndpoint = item.navigationEndpoint!!
        )

        override fun from(item: MusicTwoRowItemRenderer): AlbumItem = AlbumItem(
            title = item.title.toString(),
            subtitle = item.subtitle.toString(),
            thumbnails = item.thumbnailRenderer.getThumbnails(),
            menu = item.menu.toItemMenu(),
            navigationEndpoint = item.navigationEndpoint
        )
    }
}

data class PlaylistItem(
    override val title: String,
    override val subtitle: String,
    override val thumbnails: List<Thumbnail>,
    override val menu: ItemMenu,
    override val navigationEndpoint: NavigationEndpoint,
) : Item() {
    companion object : FromContent<PlaylistItem> {
        override fun from(item: MusicResponsiveListItemRenderer): PlaylistItem = PlaylistItem(
            title = item.getTitle(),
            subtitle = item.getSubtitle(),
            thumbnails = item.thumbnail!!.getThumbnails(),
            menu = item.menu.toItemMenu(),
            navigationEndpoint = item.navigationEndpoint!!
        )

        override fun from(item: MusicTwoRowItemRenderer): PlaylistItem = PlaylistItem(
            title = item.title.toString(),
            subtitle = item.subtitle.toString(),
            thumbnails = item.thumbnailRenderer.getThumbnails(),
            menu = item.menu.toItemMenu(),
            navigationEndpoint = item.navigationEndpoint
        )
    }
}

data class ArtistItem(
    override val title: String,
    override val subtitle: String,
    override val thumbnails: List<Thumbnail>,
    override val menu: ItemMenu,
    override val navigationEndpoint: NavigationEndpoint,
) : Item() {
    companion object : FromContent<ArtistItem> {
        override fun from(item: MusicResponsiveListItemRenderer): ArtistItem = ArtistItem(
            title = item.getTitle(),
            subtitle = item.getSubtitle(),
            thumbnails = item.thumbnail!!.getThumbnails(),
            menu = item.menu.toItemMenu(),
            navigationEndpoint = item.navigationEndpoint!!
        )

        override fun from(item: MusicTwoRowItemRenderer): ArtistItem = ArtistItem(
            title = item.title.toString(),
            subtitle = item.subtitle.toString(),
            thumbnails = item.thumbnailRenderer.getThumbnails(),
            menu = item.menu.toItemMenu(),
            navigationEndpoint = item.navigationEndpoint
        )
    }
}

data class NavigationItem(
    override val title: String,
    val subtitle: String? = null,
    val icon: String?,
    val stripeColor: Long?,
    val navigationEndpoint: NavigationEndpoint,
) : BaseItem()

data class SuggestionTextItem(
    override val title: String,
) : BaseItem()

object Separator : BaseItem() {
    override val title: String = ""
}

const val ITEM_UNKNOWN = -1
const val ITEM_SONG = 0
const val ITEM_VIDEO = 1
const val ITEM_ALBUM = 2
const val ITEM_PLAYLIST = 3
const val ITEM_ARTIST = 4