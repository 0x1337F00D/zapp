package de.christinecoenen.code.zapp.app.zattoo.model

import com.google.gson.annotations.SerializedName

data class ZattooWatchBody(
    @SerializedName("cid")
    val cid: String,
    @SerializedName("stream_type")
    val streamType: String = "hls",
    @SerializedName("https_watch_urls")
    val httpsWatchUrls: Boolean = true
)
