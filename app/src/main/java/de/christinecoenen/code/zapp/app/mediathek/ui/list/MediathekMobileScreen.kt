package de.christinecoenen.code.zapp.app.mediathek.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.mediathek.ui.MediathekUiViewModel
import de.christinecoenen.code.zapp.models.channels.ChannelModel
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import org.koin.androidx.compose.koinViewModel

@Composable
fun MediathekMobileScreen(
	onShowClick: (MediathekShow) -> Unit,
	viewModel: MediathekUiViewModel = koinViewModel()
) {
	val heroShow by viewModel.heroShow.collectAsState()
	val newShows by viewModel.newShows.collectAsState()
	val continueWatching by viewModel.continueWatching.collectAsState()
	val broadcasters by viewModel.broadcasters.collectAsState()
	val genres = viewModel.genres

	Surface(
		modifier = Modifier.fillMaxSize(),
		color = MaterialTheme.colorScheme.background
	) {
		LazyColumn(
			contentPadding = PaddingValues(bottom = 80.dp), // Space for bottom nav
			verticalArrangement = Arrangement.spacedBy(24.dp)
		) {
			// Hero Section
			item {
				heroShow?.let { show ->
					val channel = remember(show.channel) { viewModel.getChannelModel(show.channel) }
					HeroSection(show = show, channel = channel, onShowClick = onShowClick)
				}
			}

			// Continue Watching
			if (continueWatching.isNotEmpty()) {
				item {
					ShowRow(
						title = stringResource(R.string.activity_main_tab_continue_watching),
						shows = continueWatching,
						viewModel = viewModel,
						onShowClick = onShowClick
					)
				}
			}

			// New Shows
			if (newShows.isNotEmpty()) {
				item {
					ShowRow(
						title = stringResource(R.string.activity_main_tab_mediathek),
						shows = newShows,
						viewModel = viewModel,
						onShowClick = onShowClick
					)
				}
			}

			// Broadcasters
			if (broadcasters.isNotEmpty()) {
				item {
					ChannelRow(
						title = stringResource(R.string.fragment_mediathek_channel),
						channels = broadcasters
					)
				}
			}

			// Genres
			item {
				GenreRow(
					title = stringResource(R.string.menu_filter),
					genres = genres
				)
			}
		}
	}
}

@Composable
fun HeroSection(
	show: MediathekShow,
	channel: ChannelModel?,
	onShowClick: (MediathekShow) -> Unit
) {
	val backgroundColor = channel?.color?.let { Color(it) } ?: MaterialTheme.colorScheme.primaryContainer

	Box(
		modifier = Modifier
			.fillMaxWidth()
			.height(400.dp)
	) {
		// Background
		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(backgroundColor)
		) {
			// Gradient Overlay
			Box(
				modifier = Modifier
					.fillMaxSize()
					.background(
						Brush.verticalGradient(
							colors = listOf(
								Color.Transparent,
								MaterialTheme.colorScheme.background
							)
						)
					)
			)
		}

		// Content
		Column(
			modifier = Modifier
				.align(Alignment.BottomStart)
				.padding(16.dp)
				.fillMaxWidth()
		) {
			channel?.let {
				AsyncImage(
					model = it.logoUrl,
					contentDescription = null,
					modifier = Modifier
						.height(32.dp)
						.padding(bottom = 8.dp),
					contentScale = ContentScale.Fit,
					placeholder = painterResource(it.drawableId),
					error = painterResource(it.drawableId)
				)
			}
			Text(
				text = show.title,
				style = MaterialTheme.typography.displaySmall,
				fontWeight = FontWeight.Bold,
				color = MaterialTheme.colorScheme.onBackground,
				maxLines = 2,
				overflow = TextOverflow.Ellipsis
			)
			Text(
				text = "${show.topic} • ${show.formattedDuration}",
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
				modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
			)
			Button(
				onClick = { onShowClick(show) },
				modifier = Modifier.fillMaxWidth()
			) {
				Text(text = stringResource(R.string.action_play))
			}
		}
	}
}

@Composable
fun ShowRow(
	title: String,
	shows: List<MediathekShow>,
	viewModel: MediathekUiViewModel,
	onShowClick: (MediathekShow) -> Unit
) {
	Column(modifier = Modifier.fillMaxWidth()) {
		Text(
			text = title,
			style = MaterialTheme.typography.titleMedium,
			fontWeight = FontWeight.Bold,
			modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
		)
		LazyRow(
			contentPadding = PaddingValues(horizontal = 16.dp),
			horizontalArrangement = Arrangement.spacedBy(8.dp)
		) {
			items(shows) { show ->
				val channel = remember(show.channel) { viewModel.getChannelModel(show.channel) }
				ShowCard(show = show, channel = channel, onShowClick = onShowClick)
			}
		}
	}
}

@Composable
fun ShowCard(
	show: MediathekShow,
	channel: ChannelModel?,
	onShowClick: (MediathekShow) -> Unit
) {
	val backgroundColor = channel?.color?.let { Color(it) } ?: MaterialTheme.colorScheme.surfaceVariant

	Card(
		onClick = { onShowClick(show) },
		modifier = Modifier
			.width(140.dp)
			.aspectRatio(2f / 3f), // Portrait aspect ratio for mobile cards look good
		shape = RoundedCornerShape(8.dp),
		colors = CardDefaults.cardColors(containerColor = backgroundColor)
	) {
		Box(modifier = Modifier.fillMaxSize()) {
			// Background Logo
			channel?.let {
				AsyncImage(
					model = it.logoUrl,
					contentDescription = null,
					modifier = Modifier
						.fillMaxSize()
						.padding(24.dp)
						.alpha(0.1f),
					contentScale = ContentScale.Fit,
					placeholder = painterResource(it.drawableId),
					error = painterResource(it.drawableId)
				)
			}

			// Content
			Column(
				modifier = Modifier
					.align(Alignment.BottomStart)
					.padding(8.dp)
			) {
				Text(
					text = show.title,
					style = MaterialTheme.typography.labelMedium,
					maxLines = 3,
					overflow = TextOverflow.Ellipsis,
					color = Color.White
				)
				Text(
					text = show.formattedDuration,
					style = MaterialTheme.typography.labelSmall,
					color = Color.White.copy(alpha = 0.7f)
				)
			}
		}
	}
}

@Composable
fun ChannelRow(
	title: String,
	channels: List<ChannelModel>
) {
	Column(modifier = Modifier.fillMaxWidth()) {
		Text(
			text = title,
			style = MaterialTheme.typography.titleMedium,
			fontWeight = FontWeight.Bold,
			modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
		)
		LazyRow(
			contentPadding = PaddingValues(horizontal = 16.dp),
			horizontalArrangement = Arrangement.spacedBy(16.dp)
		) {
			items(channels) { channel ->
				ChannelCircle(channel = channel)
			}
		}
	}
}

@Composable
fun ChannelCircle(channel: ChannelModel) {
	Column(horizontalAlignment = Alignment.CenterHorizontally) {
		Surface(
			modifier = Modifier
				.size(64.dp)
				.clip(CircleShape)
				.clickable { /* TODO: Navigate to channel */ },
			color = Color.White,
			shadowElevation = 4.dp
		) {
			Box(contentAlignment = Alignment.Center) {
				AsyncImage(
					model = channel.logoUrl,
					contentDescription = channel.name,
					modifier = Modifier.padding(12.dp),
					contentScale = ContentScale.Fit,
					placeholder = painterResource(channel.drawableId),
					error = painterResource(channel.drawableId)
				)
			}
		}
		Spacer(modifier = Modifier.height(4.dp))
		Text(
			text = channel.name,
			style = MaterialTheme.typography.labelSmall,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier.width(64.dp),
			textAlign = androidx.compose.ui.text.style.TextAlign.Center
		)
	}
}

@Composable
fun GenreRow(
	title: String,
	genres: List<String>
) {
	Column(modifier = Modifier.fillMaxWidth()) {
		Text(
			text = title,
			style = MaterialTheme.typography.titleMedium,
			fontWeight = FontWeight.Bold,
			modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
		)
		LazyRow(
			contentPadding = PaddingValues(horizontal = 16.dp),
			horizontalArrangement = Arrangement.spacedBy(8.dp)
		) {
			items(genres) { genre ->
				GenreChip(genre = genre)
			}
		}
	}
}

@Composable
fun GenreChip(genre: String) {
	Surface(
		shape = RoundedCornerShape(16.dp),
		color = MaterialTheme.colorScheme.surfaceVariant,
		modifier = Modifier.clickable { /* TODO: Navigate to genre */ }
	) {
		Text(
			text = genre,
			modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
			style = MaterialTheme.typography.labelMedium
		)
	}
}
