package de.christinecoenen.code.zapp.app.zattoo.model

import com.google.gson.annotations.SerializedName

data class ZattooWatchResponse(
    @SerializedName("success")
    val success: Boolean? = null,
    @SerializedName("stream")
    val stream: ZattooStream?
)

data class ZattooStream(
    @SerializedName("url")
    val url: String? = null,
    @SerializedName("watch_urls")
    val watchUrls: List<ZattooWatchUrl>? = null
)

data class ZattooWatchUrl(
    @SerializedName("url")
    val url: String? = null,
    @SerializedName("maxrate")
    val maxRate: Int? = null,
    @SerializedName("license_url")
    val licenseUrl: String? = null
)
