package de.christinecoenen.code.zapp.tv.logs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.leanback.app.VerticalGridSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.VerticalGridPresenter
import de.christinecoenen.code.zapp.utils.logging.LogRepository

class LogsFragment : VerticalGridSupportFragment() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val presenter = VerticalGridPresenter()
		presenter.numberOfColumns = 1
		gridPresenter = presenter

		val adapter = ArrayObjectAdapter(LogPresenter())
		val logs = LogRepository.getLogs().reversed()

		logs.forEach { adapter.add(it) }

		this.adapter = adapter
	}
}
