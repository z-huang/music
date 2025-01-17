package com.zionhuang.music.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.AlbumItem
import com.zionhuang.music.db.MusicDatabase
import com.zionhuang.music.utils.reportException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumViewModel @Inject constructor(
    database: MusicDatabase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val albumId = savedStateHandle.get<String>("albumId")!!
    val albumWithSongs = database.albumWithSongs(albumId)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val otherVersions = MutableStateFlow<List<AlbumItem>>(emptyList())

    init {
        viewModelScope.launch {
            val album = database.album(albumId).first()
            YouTube.album(albumId).onSuccess {
                if (album == null || album.album.songCount == 0) {
                    database.transaction {
                        if (album == null) insert(it)
                        else update(album.album, it)
                    }
                }
                otherVersions.value = it.otherVersions
            }.onFailure {
                reportException(it)
                if (it.message?.contains("NOT_FOUND") == true) {
                    // This album no longer exists in YouTube Music
                    database.query {
                        album?.album?.let(::delete)
                    }
                }
            }
        }
    }
}
