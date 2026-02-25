package de.christinecoenen.code.zapp.tv.mediathek.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.items
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.Carousel
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.mediathek.api.request.MediathekChannel
import de.christinecoenen.code.zapp.models.channels.ChannelModel
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.app.mediathek.ui.MediathekUiViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MediathekScreen(
	onShowClick: (MediathekShow) -> Unit,
	onSearchClick: () -> Unit,
	viewModel: MediathekUiViewModel = koinViewModel()
) {
	val selectedChannel by viewModel.selectedChannel.collectAsState()

	BackHandler(enabled = selectedChannel != null) {
		viewModel.clearSelection()
	}

	if (selectedChannel != null) {
		ChannelDetailScreen(
			onShowClick = onShowClick,
			onSearchClick = onSearchClick,
			viewModel = viewModel
		)
	} else {
		MediathekMainScreen(
			onShowClick = onShowClick,
			onSearchClick = onSearchClick,
			viewModel = viewModel
		)
	}
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MediathekMainScreen(
	onShowClick: (MediathekShow) -> Unit,
	onSearchClick: () -> Unit,
	viewModel: MediathekUiViewModel
) {
	val continueWatching by viewModel.continueWatching.collectAsState()
	val bookmarkedShows by viewModel.bookmarkedShows.collectAsState()
	val bookmarkedIds by viewModel.bookmarkedIds.collectAsState()
	val broadcasters by viewModel.broadcasters.collectAsState()

	val progressMap = remember(continueWatching) {
		continueWatching.associate { it.mediathekShow.apiId to (if (it.videoDuration > 0) it.playbackPosition.toFloat() / it.videoDuration else 0f) }
	}

	Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
		// Top Bar with Search
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
			contentAlignment = Alignment.CenterEnd
		) {
			IconButton(onClick = onSearchClick) {
				Icon(
					painter = painterResource(id = R.drawable.ic_baseline_search_24),
					contentDescription = stringResource(R.string.menu_search)
				)
			}
		}

		TvLazyColumn(
			contentPadding = PaddingValues(bottom = 32.dp),
			verticalArrangement = Arrangement.spacedBy(24.dp)
		) {
			// Broadcasters Grid
			item {
				Text(
					text = stringResource(R.string.fragment_mediathek_channel),
					style = MaterialTheme.typography.titleMedium,
					modifier = Modifier.padding(start = 32.dp, bottom = 12.dp)
				)
			}
			item {
				ChannelGrid(
					channels = broadcasters,
					onChannelClick = { channel ->
						val mediathekChannel = MediathekChannel.entries.find { it.apiId == channel.id }
						if (mediathekChannel != null) {
							viewModel.selectChannel(mediathekChannel)
						}
					}
				)
			}

			// Continue Watching
			if (continueWatching.isNotEmpty()) {
				item {
					ShowRow(
						title = stringResource(R.string.activity_main_tab_continue_watching),
						shows = continueWatching.map { it.mediathekShow },
						progressMap = progressMap,
						bookmarkedIds = bookmarkedIds,
						viewModel = viewModel,
						onShowClick = onShowClick
					)
				}
			}

			// Bookmarks
			if (bookmarkedShows.isNotEmpty()) {
				item {
					ShowRow(
						title = stringResource(R.string.activity_main_tab_bookmarks),
						shows = bookmarkedShows,
						progressMap = progressMap,
						bookmarkedIds = bookmarkedIds,
						viewModel = viewModel,
						onShowClick = onShowClick
					)
				}
			}
		}
	}
}

@Composable
fun ChannelGrid(
	channels: List<ChannelModel>,
	onChannelClick: (ChannelModel) -> Unit
) {
	TvLazyVerticalGrid(
		columns = TvGridCells.Fixed(5),
		contentPadding = PaddingValues(horizontal = 32.dp),
		verticalArrangement = Arrangement.spacedBy(16.dp),
		horizontalArrangement = Arrangement.spacedBy(16.dp),
		modifier = Modifier.height(200.dp) // Fixed height for grid in list? better separate
	) {
		items(channels) { channel ->
			ChannelCard(channel = channel, onClick = { onChannelClick(channel) })
		}
	}
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ChannelDetailScreen(
	onShowClick: (MediathekShow) -> Unit,
	onSearchClick: () -> Unit,
	viewModel: MediathekUiViewModel
) {
	val heroShow by viewModel.heroShow.collectAsState()
	val newShows by viewModel.channelNewShows.collectAsState()
	val movies by viewModel.channelMovies.collectAsState()
	val docs by viewModel.channelDocs.collectAsState()
	val series by viewModel.channelSeries.collectAsState()
	val bookmarkedIds by viewModel.bookmarkedIds.collectAsState()
	val continueWatching by viewModel.continueWatching.collectAsState()

	val progressMap = remember(continueWatching) {
		continueWatching.associate { it.mediathekShow.apiId to (if (it.videoDuration > 0) it.playbackPosition.toFloat() / it.videoDuration else 0f) }
	}

	Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
		// Top Bar
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
			contentAlignment = Alignment.CenterEnd
		) {
			IconButton(onClick = onSearchClick) {
				Icon(
					painter = painterResource(id = R.drawable.ic_baseline_search_24),
					contentDescription = stringResource(R.string.menu_search)
				)
			}
		}

		TvLazyColumn(
			contentPadding = PaddingValues(bottom = 32.dp),
			verticalArrangement = Arrangement.spacedBy(24.dp)
		) {
			// Hero Section
			item {
				heroShow?.let { show ->
					val channel = remember(show.channel) { viewModel.getChannelModel(show.channel) }
					val isBookmarked = bookmarkedIds.contains(show.apiId)
					HeroSection(
						show = show,
						channel = channel,
						isBookmarked = isBookmarked,
						onShowClick = onShowClick,
						onBookmarkClick = { viewModel.toggleBookmark(show) }
					)
				}
			}

			// New Shows
			if (newShows.isNotEmpty()) {
				item {
					ShowRow(
						title = stringResource(R.string.activity_main_tab_mediathek),
						shows = newShows,
						progressMap = progressMap,
						bookmarkedIds = bookmarkedIds,
						viewModel = viewModel,
						onShowClick = onShowClick
					)
				}
			}

			// Movies
			if (movies.isNotEmpty()) {
				item {
					ShowRow(
						title = stringResource(R.string.fragment_mediathek_movies),
						shows = movies,
						progressMap = progressMap,
						bookmarkedIds = bookmarkedIds,
						viewModel = viewModel,
						onShowClick = onShowClick
					)
				}
			}

			// Docs
			if (docs.isNotEmpty()) {
				item {
					ShowRow(
						title = stringResource(R.string.fragment_mediathek_docs),
						shows = docs,
						progressMap = progressMap,
						bookmarkedIds = bookmarkedIds,
						viewModel = viewModel,
						onShowClick = onShowClick
					)
				}
			}

			// Series
			if (series.isNotEmpty()) {
				items(series) { singleSeries ->
					ShowRow(
						title = singleSeries.title,
						shows = singleSeries.shows,
						progressMap = progressMap,
						bookmarkedIds = bookmarkedIds,
						viewModel = viewModel,
						onShowClick = onShowClick
					)
				}
			}
		}
	}
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HeroSection(
	show: MediathekShow,
	channel: ChannelModel?,
	isBookmarked: Boolean,
	onShowClick: (MediathekShow) -> Unit,
	onBookmarkClick: () -> Unit
) {
	val backgroundColor = channel?.color?.let { Color(it) } ?: MaterialTheme.colorScheme.secondaryContainer

	Carousel(
		itemCount = 1,
		modifier = Modifier
			.fillMaxWidth()
			.height(300.dp)
			.padding(horizontal = 32.dp)
	) { _ ->
		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(
					brush = Brush.horizontalGradient(
						colors = listOf(
							backgroundColor,
							backgroundColor.copy(alpha = 0.6f),
							Color.Transparent
						)
					)
				)
		) {
			// Content
			Column(
				modifier = Modifier
					.align(Alignment.BottomStart)
					.padding(32.dp)
					.fillMaxWidth(0.6f)
			) {
				channel?.let {
					AsyncImage(
						model = it.logoUrl,
						contentDescription = null,
						modifier = Modifier
							.height(40.dp)
							.padding(bottom = 8.dp),
						contentScale = ContentScale.Fit,
						placeholder = painterResource(it.drawableId),
						error = painterResource(it.drawableId)
					)
				}
				Text(
					text = show.title,
					style = MaterialTheme.typography.displaySmall,
					color = Color.White,
					maxLines = 2,
					overflow = TextOverflow.Ellipsis
				)
				Text(
					text = "${show.topic} • ${show.formattedDuration}",
					style = MaterialTheme.typography.bodyMedium,
					color = Color.White.copy(alpha = 0.8f),
					modifier = Modifier.padding(top = 8.dp)
				)
				Spacer(modifier = Modifier.height(16.dp))
				Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
					Button(onClick = { onShowClick(show) }) {
						Text(text = stringResource(R.string.action_play))
					}
					Button(onClick = onBookmarkClick) {
						val icon = if (isBookmarked) R.drawable.ic_baseline_bookmark_24 else R.drawable.ic_baseline_bookmark_border_24
						Row(verticalAlignment = Alignment.CenterVertically) {
							Icon(
								painter = painterResource(id = icon),
								contentDescription = null,
								modifier = Modifier.padding(end = 8.dp).size(20.dp)
							)
							Text(text = stringResource(R.string.activity_main_tab_bookmarks))
						}
					}
				}
			}
		}
	}
}

@Composable
fun ShowRow(
	title: String,
	shows: List<MediathekShow>,
	progressMap: Map<String, Float> = emptyMap(),
	bookmarkedIds: Set<String> = emptySet(),
	viewModel: MediathekUiViewModel,
	onShowClick: (MediathekShow) -> Unit
) {
	Column(modifier = Modifier.fillMaxWidth()) {
		Text(
			text = title,
			style = MaterialTheme.typography.titleMedium,
			modifier = Modifier.padding(start = 32.dp, bottom = 12.dp)
		)
		TvLazyRow(
			contentPadding = PaddingValues(horizontal = 32.dp),
			horizontalArrangement = Arrangement.spacedBy(16.dp)
		) {
			items(shows) { show ->
				val channel = remember(show.channel) { viewModel.getChannelModel(show.channel) }
				val progress = progressMap[show.apiId] ?: 0f
				val isBookmarked = bookmarkedIds.contains(show.apiId)
				ShowCard(show = show, channel = channel, progress = progress, isBookmarked = isBookmarked, onShowClick = onShowClick)
			}
		}
	}
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ShowCard(
	show: MediathekShow,
	channel: ChannelModel?,
	progress: Float = 0f,
	isBookmarked: Boolean = false,
	onShowClick: (MediathekShow) -> Unit
) {
	// Generate a stable color based on topic if no channel color or to differentiate
	val topicColor = remember(show.topic) {
		Color(show.topic.hashCode() or 0xFF000000.toInt())
	}

	val backgroundColor = channel?.color?.let { Color(it) } ?: topicColor

	Card(
		onClick = { onShowClick(show) },
		modifier = Modifier
			.width(220.dp)
			.aspectRatio(16f / 9f),
		scale = CardDefaults.scale(focusedScale = 1.1f)
	) {
		Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
			// Background Logo
			channel?.let {
				AsyncImage(
					model = it.logoUrl,
					contentDescription = null,
					modifier = Modifier
						.fillMaxSize()
						.padding(16.dp)
						.alpha(0.15f)
						.align(Alignment.CenterEnd),
					contentScale = ContentScale.Fit,
					placeholder = painterResource(it.drawableId),
					error = painterResource(it.drawableId)
				)
			}

			// Foreground Content
			Column(
				modifier = Modifier
					.align(Alignment.BottomStart)
					.padding(12.dp)
			) {
				Text(
					text = show.title,
					style = MaterialTheme.typography.bodyMedium,
					maxLines = 2,
					overflow = TextOverflow.Ellipsis,
					color = Color.White
				)
				Text(
					text = show.formattedDuration,
					style = MaterialTheme.typography.labelSmall,
					color = Color.White.copy(alpha = 0.7f)
				)
			}

			// Progress Bar
			if (progress > 0) {
				Box(
					modifier = Modifier
						.align(Alignment.BottomStart)
						.fillMaxWidth()
						.height(4.dp)
						.background(Color.White.copy(alpha = 0.3f))
				) {
					Box(
						modifier = Modifier
							.fillMaxHeight()
							.fillMaxWidth(progress)
							.background(MaterialTheme.colorScheme.primary)
					)
				}
			}

			// Bookmark Icon
			if (isBookmarked) {
				Icon(
					painter = painterResource(id = R.drawable.ic_baseline_bookmark_24),
					contentDescription = null,
					tint = MaterialTheme.colorScheme.primary,
					modifier = Modifier
						.align(Alignment.TopEnd)
						.padding(8.dp)
						.size(24.dp)
				)
			}
		}
	}
}


@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ChannelCard(channel: ChannelModel, onClick: () -> Unit) {
	Card(
		onClick = onClick,
		modifier = Modifier.size(80.dp),
		shape = CardDefaults.shape(shape = CircleShape),
		scale = CardDefaults.scale(focusedScale = 1.1f)
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(Color.White), // Channels usually have white/transparent logos
			contentAlignment = Alignment.Center
		) {
			AsyncImage(
				model = channel.logoUrl,
				contentDescription = channel.name,
				modifier = Modifier
					.padding(16.dp)
					.fillMaxSize(),
				contentScale = ContentScale.Fit,
				placeholder = painterResource(channel.drawableId),
				error = painterResource(channel.drawableId)
			)
		}
	}
}

