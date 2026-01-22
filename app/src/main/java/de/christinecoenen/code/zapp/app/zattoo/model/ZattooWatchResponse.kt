package de.christinecoenen.code.zapp.app.zattoo.model

import com.google.gson.annotations.SerializedName

data class ZattooWatchResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("stream")
    val stream: ZattooStream?
)

data class ZattooStream(
    @SerializedName("url")
    val url: String,
    @SerializedName("watch_token")
    val watchToken: String?
)
