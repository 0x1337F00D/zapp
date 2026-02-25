package de.christinecoenen.code.zapp.app.mediathek.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.christinecoenen.code.zapp.app.mediathek.api.IMediathekApiService
import de.christinecoenen.code.zapp.app.mediathek.api.request.QueryRequest
import de.christinecoenen.code.zapp.models.channels.ChannelModel
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.models.shows.PersistedMediathekShow
import de.christinecoenen.code.zapp.repositories.ChannelRepository
import de.christinecoenen.code.zapp.repositories.MediathekRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

data class MediathekSeries(
	val title: String,
	val shows: List<MediathekShow>
)

@OptIn(FlowPreview::class)
class MediathekUiViewModel(
	private val mediathekRepository: MediathekRepository,
	private val channelRepository: ChannelRepository,
	private val mediathekApiService: IMediathekApiService
) : ViewModel() {

	private val _heroShow = MutableStateFlow<MediathekShow?>(null)
	val heroShow: StateFlow<MediathekShow?> = _heroShow.asStateFlow()

	private val _newShows = MutableStateFlow<List<MediathekShow>>(emptyList())
	val newShows: StateFlow<List<MediathekShow>> = _newShows.asStateFlow()

	val series: StateFlow<List<MediathekSeries>> = _newShows.map { shows ->
		shows
			.groupBy { it.topic }
			.filter { it.value.size > 1 } // Only show series with at least 2 episodes
			.map { MediathekSeries(it.key, it.value) }
			.sortedByDescending { it.shows.maxOfOrNull { show -> show.timestamp } }
	}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

	private val _broadcasters = MutableStateFlow<List<ChannelModel>>(emptyList())
	val broadcasters: StateFlow<List<ChannelModel>> = _broadcasters.asStateFlow()

	val continueWatching: StateFlow<List<PersistedMediathekShow>> = mediathekRepository
		.getStartedPersisted(10)
		.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

	val bookmarkedShows: StateFlow<List<MediathekShow>> = mediathekRepository
		.getBookmarked(20)
		.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

	val bookmarkedIds: StateFlow<Set<String>> = mediathekRepository
		.getBookmarkedIds()
		.map { it.toSet() }
		.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

	// Static list of popular genres/topics
	val genres = listOf(
		"Dokumentation", "Film", "Serie", "Nachrichten", "Comedy", "Kinder", "Sport", "Kultur"
	)

	private val _searchQuery = MutableStateFlow("")
	val searchQuery = _searchQuery.asStateFlow()

	private val _searchResults = MutableStateFlow<List<MediathekShow>>(emptyList())
	val searchResults = _searchResults.asStateFlow()

	init {
		loadData()

		viewModelScope.launch {
			_searchQuery
				.debounce(300)
				.collectLatest { query ->
					if (query.length > 2) {
						performSearch(query)
					} else {
						_searchResults.value = emptyList()
					}
				}
		}
	}

	fun onSearchQueryChanged(query: String) {
		_searchQuery.value = query
	}

	private suspend fun performSearch(query: String) {
		try {
			val request = QueryRequest().apply {
				setQueryString(query)
				size = 50
			}
			val response = mediathekApiService.listShows(request)
			_searchResults.value = response.result?.results ?: emptyList()
		} catch (e: Exception) {
			Timber.e(e, "Search failed")
			_searchResults.value = emptyList()
		}
	}

	fun refresh() {
		loadData()
	}

	private fun loadData() {
		viewModelScope.launch {
			try {
				// Load broadcasters
				_broadcasters.value = channelRepository.getChannelList().list

				// Load New Shows
				val queryRequest = QueryRequest().apply {
					size = 20
					future = false
				}
				val response = mediathekApiService.listShows(queryRequest)
				_newShows.value = response.result?.results ?: emptyList()

				// Pick a hero show (e.g., the first long duration show from new list, or just first)
				_heroShow.value = _newShows.value.firstOrNull {
					// Try to find a show with duration > 10 min to be "hero worthy"
					(it.duration?.toIntOrNull() ?: 0) > 600
				} ?: _newShows.value.firstOrNull()

			} catch (e: Exception) {
				Timber.e(e, "Failed to load mediathek data")
			}
		}
	}

	fun getChannelModel(channelId: String): ChannelModel? {
		// Try to find by ID or name
		return _broadcasters.value.find { it.id == channelId || it.name == channelId }
	}

	fun toggleBookmark(show: MediathekShow) {
		viewModelScope.launch {
			val isBookmarked = bookmarkedIds.value.contains(show.apiId)
			mediathekRepository.setBookmarked(show.apiId, !isBookmarked)
		}
	}
}
