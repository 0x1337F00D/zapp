package de.christinecoenen.code.zapp.app.zattoo.model

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class ZattooSessionResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("session")
    val session: ZattooSessionData?
)

data class ZattooSessionData(
    @SerializedName("power_guide_hash")
    val powerGuideHash: String?,
    @SerializedName("account")
    val account: JsonObject?
)
