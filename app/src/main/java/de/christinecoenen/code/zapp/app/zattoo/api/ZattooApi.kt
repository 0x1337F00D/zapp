package de.christinecoenen.code.zapp.app.zattoo.api

import de.christinecoenen.code.zapp.app.zattoo.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ZattooApi {

    @POST("zapi/v2/session/hello")
    suspend fun hello(@Body body: ZattooHelloBody): ZattooSessionResponse

    @POST("zapi/v3/session")
    suspend fun login(@Body body: ZattooLoginBody): ZattooSessionResponse

    @GET("zapi/v3/cached/{powerGuideHash}/channels")
    suspend fun getChannels(@Path("powerGuideHash") powerGuideHash: String): ZattooChannelsResponse

    @POST("zapi/watch")
    suspend fun watch(@Body body: ZattooWatchBody): ZattooWatchResponse
}
