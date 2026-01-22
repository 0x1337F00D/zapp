package de.christinecoenen.code.zapp.app.zattoo.model

import com.google.gson.annotations.SerializedName

data class ZattooLoginBody(
    @SerializedName("login")
    val login: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("format")
    val format: String = "json"
)
