package de.christinecoenen.code.zapp.models.channels.json

import android.content.Context
import de.christinecoenen.code.zapp.models.channels.SimpleChannelList

open class SortableVisibleJsonChannelList(context: Context) : SortableJsonChannelList(context) {

	override fun loadSortingFromDisk() {
		val listFromDisk = JsonChannelList(context).list
		val sortedChannels = channelOrderHelper.sortChannelList(listFromDisk, true)

		channelList = SimpleChannelList(sortedChannels)
	}

}
