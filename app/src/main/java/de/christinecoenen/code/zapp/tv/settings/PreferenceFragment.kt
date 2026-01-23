package de.christinecoenen.code.zapp.tv.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat
import androidx.lifecycle.lifecycleScope
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository
import de.christinecoenen.code.zapp.repositories.ChannelRepository
import de.christinecoenen.code.zapp.utils.system.PreferenceFragmentHelper
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class PreferenceFragment : LeanbackPreferenceFragmentCompat(),
	SharedPreferences.OnSharedPreferenceChangeListener {

	private val settingsRepository: SettingsRepository by inject()
	private val channelRepository: ChannelRepository by inject()
	private val preferenceFragmentHelper = PreferenceFragmentHelper(this, settingsRepository)

	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
		setPreferencesFromResource(R.xml.tv_preferences, rootKey)

		preferenceFragmentHelper.initPreferences()

		findPreference<androidx.preference.Preference>(getString(R.string.pref_key_zattoo_check))?.setOnPreferenceClickListener {
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

	override fun onResume() {
		super.onResume()
		preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
	}

	override fun onPause() {
		super.onPause()
		preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
	}

	override fun onDestroy() {
		super.onDestroy()
		preferenceFragmentHelper.destroy()
	}

	override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
		if (key == getString(R.string.pref_key_zattoo_username) ||
			key == getString(R.string.pref_key_zattoo_password)
		) {
			channelRepository.tryLoadZattooChannels(true)
		}
	}
}
