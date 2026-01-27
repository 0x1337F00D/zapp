package de.christinecoenen.code.zapp.app.zattoo.api

import de.christinecoenen.code.zapp.app.zattoo.model.*
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ZattooApi {

    @FormUrlEncoded
    @POST("zapi/v3/session/hello")
    suspend fun hello(
        @Field("uuid") uuid: String,
        @Field("lang") lang: String,
        @Field("client_app_token") clientAppToken: String,
        @Field("app_version") appVersion: String,
        @Field("format") format: String
    ): ZattooSessionData

    @GET("zapi/v3/session")
    suspend fun getSession(): ZattooSessionData

    @FormUrlEncoded
    @POST("zapi/v3/account/login")
    suspend fun login(
        @Field("login") login: String,
        @Field("password") password: String,
        @Field("format") format: String
    ): ZattooSessionData

    @GET("zapi/v3/cached/{powerGuideHash}/channels")
    suspend fun getChannels(@Path("powerGuideHash") powerGuideHash: String): ZattooChannelsResponse

    @FormUrlEncoded
    @POST("zapi/watch")
    suspend fun watch(
        @Field("cid") cid: String,
        @Field("stream_type") streamType: String,
        @Field("https_watch_urls") httpsWatchUrls: Boolean
    ): ZattooStream
}
