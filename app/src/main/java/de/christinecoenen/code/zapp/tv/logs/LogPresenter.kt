package de.christinecoenen.code.zapp.tv.logs

import android.view.ViewGroup
import android.widget.TextView
import androidx.leanback.widget.Presenter

class LogPresenter : Presenter() {
	override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
		val textView = TextView(parent.context)
		textView.isFocusable = true
		textView.isFocusableInTouchMode = true
		textView.setPadding(20, 10, 20, 10)
		return ViewHolder(textView)
	}

	override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
		val textView = viewHolder.view as TextView
		textView.text = item as String
	}

	override fun onUnbindViewHolder(viewHolder: ViewHolder) {
		// nothing to do
	}
}
