package de.christinecoenen.code.zapp.app.zattoo.model

import com.google.gson.annotations.SerializedName

data class ZattooHelloBody(
    @SerializedName("uuid")
    val uuid: String,
    @SerializedName("lang")
    val lang: String = "de",
    @SerializedName("app_token")
    val appToken: String? = null,
    @SerializedName("client_app_token")
    val clientAppToken: String,
    @SerializedName("format")
    val format: String = "json"
)
