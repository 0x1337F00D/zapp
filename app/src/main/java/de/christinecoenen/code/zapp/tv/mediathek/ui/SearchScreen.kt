package de.christinecoenen.code.zapp.tv.mediathek.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.BasicTextField
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.app.mediathek.ui.MediathekUiViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SearchScreen(
	onShowClick: (MediathekShow) -> Unit,
	onBack: () -> Unit,
	viewModel: MediathekUiViewModel = koinViewModel()
) {
	val searchQuery by viewModel.searchQuery.collectAsState()
	val searchResults by viewModel.searchResults.collectAsState()
	val focusRequester = remember { FocusRequester() }

	BackHandler(onBack = onBack)

	Column(
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.surface)
			.padding(32.dp)
	) {
		Text(
			text = stringResource(R.string.menu_search),
			style = MaterialTheme.typography.headlineMedium,
			modifier = Modifier.padding(bottom = 16.dp)
		)

		// Search Input
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
				.padding(16.dp)
		) {
			BasicTextField(
				value = searchQuery,
				onValueChange = viewModel::onSearchQueryChanged,
				modifier = Modifier
					.fillMaxWidth()
					.focusRequester(focusRequester),
				textStyle = TextStyle(
					color = MaterialTheme.colorScheme.onSurface,
					fontSize = 18.sp
				),
				cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
				singleLine = true,
				keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
				decorationBox = { innerTextField ->
					if (searchQuery.isEmpty()) {
						Text(
							text = stringResource(R.string.menu_search),
							color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
						)
					}
					innerTextField()
				}
			)
		}

		LaunchedEffect(Unit) {
			focusRequester.requestFocus()
		}

		// Results
		if (searchResults.isNotEmpty()) {
			LazyVerticalGrid(
				columns = GridCells.Adaptive(180.dp),
				contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp),
				verticalArrangement = Arrangement.spacedBy(16.dp),
				horizontalArrangement = Arrangement.spacedBy(16.dp)
			) {
				items(searchResults) { show ->
					val channel = remember(show.channel) { viewModel.getChannelModel(show.channel) }
					ShowCard(show = show, channel = channel, onShowClick = onShowClick)
				}
			}
		} else if (searchQuery.isNotEmpty()) {
			Text(
				text = stringResource(R.string.fragment_mediathek_no_results),
				modifier = Modifier.padding(top = 32.dp),
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
	}
}
