package de.christinecoenen.code.zapp.app.zattoo.model

import com.google.gson.annotations.SerializedName

data class ZattooStream(
    @SerializedName("success")
    val success: Boolean? = null,
    @SerializedName("url")
    val url: String?,
    @SerializedName("watch_token")
    val watchToken: String?
)
