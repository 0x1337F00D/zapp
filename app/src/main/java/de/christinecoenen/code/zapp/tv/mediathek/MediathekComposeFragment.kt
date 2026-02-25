package de.christinecoenen.code.zapp.tv.mediathek

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Typography
import androidx.tv.material3.darkColorScheme
import de.christinecoenen.code.zapp.app.player.VideoInfo
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.repositories.MediathekRepository
import de.christinecoenen.code.zapp.tv.mediathek.ui.MediathekScreen
import de.christinecoenen.code.zapp.tv.mediathek.ui.SearchScreen
import de.christinecoenen.code.zapp.app.mediathek.ui.MediathekUiViewModel
import de.christinecoenen.code.zapp.tv.player.PlayerActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel

class MediathekComposeFragment : Fragment() {

    private val mediathekRepository: MediathekRepository by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(
                    colorScheme = darkColorScheme(),
                    typography = Typography()
                ) {
                   MediathekApp(
                       onShowClick = { show -> launchPlayer(show) }
                   )
                }
            }
        }
    }

    private fun launchPlayer(show: MediathekShow) {
        viewLifecycleOwner.lifecycleScope.launch {
            val persistedShow = mediathekRepository.persistOrUpdateShow(show).first()
            val videoInfo = VideoInfo.fromShow(persistedShow)
            val intent = PlayerActivity.getStartIntent(requireContext(), videoInfo)
            startActivity(intent)
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MediathekApp(
    onShowClick: (MediathekShow) -> Unit,
    viewModel: MediathekUiViewModel = koinViewModel()
) {
    var isSearchVisible by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        if (isSearchVisible) {
            SearchScreen(
                onShowClick = onShowClick,
                onBack = { isSearchVisible = false },
                viewModel = viewModel
            )
        } else {
            MediathekScreen(
                onShowClick = onShowClick,
                onSearchClick = { isSearchVisible = true },
                viewModel = viewModel
            )
        }
    }
}
