package de.christinecoenen.code.zapp.app.zattoo.model

import com.google.gson.annotations.SerializedName

data class ZattooChannelsResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("channels")
    val channels: List<ZattooChannel>
)

data class ZattooChannel(
    @SerializedName("cid")
    val cid: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("qualities")
    val qualities: List<ZattooQuality>
)

data class ZattooQuality(
    @SerializedName("logo_white_84")
    val logoUrl: String?,
    @SerializedName("level")
    val level: String,
    @SerializedName("availability")
    val availability: String,
    @SerializedName("drm_required")
    val drmRequired: Boolean = false
)
