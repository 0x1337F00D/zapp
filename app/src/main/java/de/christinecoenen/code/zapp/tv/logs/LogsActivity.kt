package de.christinecoenen.code.zapp.tv.logs

import android.content.Context
import android.content.Intent
import de.christinecoenen.code.zapp.utils.system.IStartableActivity

object LogsActivity : IStartableActivity {
	override fun getStartIntent(context: Context): Intent {
		return Intent(context, LogsActivityImpl::class.java)
	}
}
