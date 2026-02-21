package de.christinecoenen.code.zapp.app.mediathek.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.main.MainActivity
import de.christinecoenen.code.zapp.app.mediathek.ui.MediathekUiViewModel
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediathekListFragment : Fragment(), MenuProvider {

	private val viewModel: MediathekUiViewModel by viewModel()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		return ComposeView(requireContext()).apply {
			setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
			setContent {
				MaterialTheme {
					MediathekMobileScreen(
						viewModel = viewModel,
						onShowClick = ::onShowClicked
					)
				}
			}
		}
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		(requireActivity() as MainActivity).addMenuProviderToSearchBar(
			this,
			viewLifecycleOwner,
			Lifecycle.State.RESUMED
		)
	}

	override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
		menuInflater.inflate(R.menu.activity_main_toolbar, menu)
		menuInflater.inflate(R.menu.mediathek_list_fragment, menu)
	}

	override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
		return when (menuItem.itemId) {
			R.id.menu_refresh -> {
				viewModel.refresh()
				true
			}
			else -> false
		}
	}

	private fun onShowClicked(show: MediathekShow) {
		val directions = MediathekListFragmentDirections.toMediathekDetailFragment(show)
		findNavController().navigate(directions)
	}
}
