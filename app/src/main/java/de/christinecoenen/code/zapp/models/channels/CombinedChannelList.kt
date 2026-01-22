package de.christinecoenen.code.zapp.models.channels

import android.content.Context
import de.christinecoenen.code.zapp.models.channels.json.JsonChannelList
import de.christinecoenen.code.zapp.models.channels.json.SortableVisibleJsonChannelList

class CombinedChannelList(context: Context) : SortableVisibleJsonChannelList(context) {

    private var extraChannels: List<ChannelModel>? = null

    fun setExtraChannels(channels: List<ChannelModel>) {
        extraChannels = channels
        reload()
    }

    override fun loadSortingFromDisk() {
        val listFromDisk = JsonChannelList(context).list
        val allChannels = listFromDisk + (extraChannels ?: emptyList())

        val sortedChannels = channelOrderHelper.sortChannelList(allChannels, true)
        channelList = SimpleChannelList(sortedChannels)
    }
}
