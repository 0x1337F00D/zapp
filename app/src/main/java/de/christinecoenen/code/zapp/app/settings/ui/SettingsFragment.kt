package de.christinecoenen.code.zapp.app.settings.ui

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository
import de.christinecoenen.code.zapp.repositories.ChannelRepository
import de.christinecoenen.code.zapp.utils.system.PreferenceFragmentHelper
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class SettingsFragment : BaseSettingsFragment() {

	private val settingsRepository: SettingsRepository by inject()
	private val channelRepository: ChannelRepository by inject()
	private val preferenceFragmentHelper = PreferenceFragmentHelper(this, settingsRepository)

	private val channelSelectionClickListener = Preference.OnPreferenceClickListener {
		val direction =
			SettingsFragmentDirections.toChannelSelectionFragment()
		findNavController().navigate(direction)
		true
	}

	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
		addPreferencesFromResource(R.xml.preferences)

		preferenceFragmentHelper.initPreferences(channelSelectionClickListener)

		findPreference<Preference>(getString(R.string.pref_key_zattoo_check))?.setOnPreferenceClickListener {
			viewLifecycleOwner.lifecycleScope.launch {
				val success = channelRepository.checkZattooLogin()
				val messageResId = if (success) {
					R.string.zattoo_login_success
				} else {
					R.string.zattoo_login_error
				}
				Toast.makeText(requireContext(), messageResId, Toast.LENGTH_SHORT).show()
			}
			true
		}
	}

	override fun onDestroy() {
		super.onDestroy()
		preferenceFragmentHelper.destroy()
	}
}
