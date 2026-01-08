package com.ngt.pixplay.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ngt.pixplay.data.model.Album
import com.ngt.pixplay.data.model.AudioItem
import com.ngt.pixplay.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: MusicRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _filter = MutableStateFlow(SearchFilter.ALL)
    val filter: StateFlow<SearchFilter> = _filter.asStateFlow()

    val searchResult: StateFlow<SearchResult> = combine(_query, _filter) { query, filter ->
        query to filter
    }.flatMapLatest { (query, filter) ->
        if (query.isEmpty()) {
            flowOf(SearchResult(query, filter, emptyMap()))
        } else {
            val songsFlow = repository.searchSongs(query)
            val albumsFlow = repository.searchAlbums(query)

            combine(songsFlow, albumsFlow) { songs, albums ->
                val resultMap = mutableMapOf<SearchFilter, List<Any>>()
                
                if (filter == SearchFilter.ALL || filter == SearchFilter.SONG) {
                    if (songs.isNotEmpty()) resultMap[SearchFilter.SONG] = songs
                }
                if (filter == SearchFilter.ALL || filter == SearchFilter.ALBUM) {
                    if (albums.isNotEmpty()) resultMap[SearchFilter.ALBUM] = albums
                }
                
                SearchResult(query, filter, resultMap)
            }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        SearchResult("", SearchFilter.ALL, emptyMap())
    )

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }
    
    fun onFilterChange(newFilter: SearchFilter) {
        _filter.value = newFilter
    }
}

enum class SearchFilter {
    ALL,
    SONG,
    ALBUM
}

data class SearchResult(
    val query: String,
    val filter: SearchFilter,
    val map: Map<SearchFilter, List<Any>>
)
