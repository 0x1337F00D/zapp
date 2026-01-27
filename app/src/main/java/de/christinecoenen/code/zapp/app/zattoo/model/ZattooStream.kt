package de.christinecoenen.code.zapp.app.zattoo.model

import com.google.gson.annotations.SerializedName

data class ZattooStream(
    @SerializedName("url")
    val url: String,
    @SerializedName("watch_token")
    val watchToken: String?
)
