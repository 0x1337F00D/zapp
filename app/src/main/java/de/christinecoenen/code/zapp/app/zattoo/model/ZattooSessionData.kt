package de.christinecoenen.code.zapp.app.zattoo.model

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class ZattooSessionData(
    @SerializedName("success")
    val success: Boolean? = null,
    @SerializedName("power_guide_hash")
    val powerGuideHash: String?,
    @SerializedName("account")
    val account: JsonElement?
)
