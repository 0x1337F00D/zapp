package de.christinecoenen.code.zapp.app.zattoo

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import de.christinecoenen.code.zapp.R
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository
import de.christinecoenen.code.zapp.app.zattoo.api.ZattooApi
import de.christinecoenen.code.zapp.app.zattoo.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.JavaNetCookieJar
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.UUID
import kotlin.random.Random

class ZattooService(
    private val context: Context,
    baseClient: OkHttpClient,
    api: ZattooApi? = null
) {

    private val settingsRepository = SettingsRepository(context)
    private val httpClient: OkHttpClient
    private val api: ZattooApi

    private var powerGuideHash: String? = null
    private var appToken: String? = null
    private var channelCache: List<ZattooChannel> = emptyList()

    private val cookieManager = CookieManager().apply {
        setCookiePolicy(CookiePolicy.ACCEPT_ALL)
    }

    init {
         httpClient = baseClient.newBuilder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "Kodi/20.0 pvr.zattoo/MWE")
                    .build()
                chain.proceed(request)
            }
            .cookieJar(JavaNetCookieJar(cookieManager))
            .build()

        if (api != null) {
            this.api = api
        } else {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://zattoo.com/")
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            this.api = retrofit.create(ZattooApi::class.java)
        }
    }

    fun logout() {
        powerGuideHash = null
        appToken = null
    }

    suspend fun getChannels(): List<ZattooChannel> = withContext(Dispatchers.IO) {
        try {
            ensureLogin()
            val hash = powerGuideHash ?: return@withContext emptyList()

            val response = api.getChannels(hash)
            if (response.success) {
                channelCache = response.channels
                response.channels
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to load Zattoo channels")
            emptyList()
        }
    }

    suspend fun getStreamUrl(cid: String): String = withContext(Dispatchers.IO) {
        ensureLogin()
        // pick a quality based on cached channels; fall back to letting server decide
        val (quality, streamType) = pickQuality(cid)
        val response = api.watchLive(
            cid = cid,
            quality = quality,
            streamType = streamType,
            format = "json",
            timeshift = 10800,
            httpsWatchUrls = true
        )
        val stream = response.stream
        val chosenUrl = stream?.watchUrls?.firstOrNull()?.url ?: stream?.url
        return@withContext chosenUrl ?: throw Exception("Could not get stream URL for $cid")
    }

    private suspend fun ensureLogin() {
        if (powerGuideHash != null) return

        val username = settingsRepository.preferences.getString(context.getString(R.string.pref_key_zattoo_username), null)
        val password = settingsRepository.preferences.getString(context.getString(R.string.pref_key_zattoo_password), null)

        if (username.isNullOrEmpty() || password.isNullOrEmpty()) {
             throw Exception("No credentials")
        }

        if (appToken == null) {
            appToken = fetchAppToken()
        }

        val uuid = generateUuid()
        // Ensure UUID cookie is set
        val cookie = Cookie.Builder()
            .domain("zattoo.com")
            .path("/")
            .name("uuid")
            .value(uuid)
            .build()
        cookieManager.cookieStore.add("https://zattoo.com/".toHttpUrl().uri(), cookie)

        val helloResponse = api.hello(
            uuid = uuid,
            lang = "de",
            clientAppToken = appToken!!,
            appVersion = "3.2038.0",
            format = "json"
        )

        if (helloResponse.success == false) throw Exception("Hello failed")

        val sessionResponse = api.getSession()

        if (sessionResponse.success == false) throw Exception("Session check failed")

        // If not logged in, try login
        if (sessionResponse.account == null || sessionResponse.account.isJsonNull) {
             val loginResponse = api.login(
                 login = username,
                 password = password,
                 format = "json"
             )

             if (loginResponse.success != false) {
                 powerGuideHash = loginResponse.powerGuideHash
             } else {
                 throw Exception("Login failed")
             }
        } else {
             powerGuideHash = sessionResponse.powerGuideHash
        }
    }

    private fun pickQuality(cid: String): Pair<String?, String> {
        val channel = channelCache.firstOrNull { it.cid == cid }
        val quality = channel?.qualities?.firstOrNull { it.availability == "available" }
        val level = quality?.level
        val streamType = if (quality?.drmRequired == true) "dash_widevine" else "dash"
        return Pair(level, streamType)
    }

    private fun fetchAppToken(): String {
        // Try the documented token endpoint first, then fall back to /token.json
        val uuid = UUID.randomUUID().toString()
        val primary = Request.Builder().url("https://zattoo.com/client/token.json?id=$uuid").build()
        httpClient.newCall(primary).execute().use { response ->
            val body = response.body?.string()
            if (!body.isNullOrEmpty()) {
                val jsonObject = Gson().fromJson(body, JsonObject::class.java)
                if (jsonObject.has("session_token")) {
                    return jsonObject.get("session_token").asString
                }
            }
        }

        val fallback = Request.Builder().url("https://zattoo.com/token.json").build()
        httpClient.newCall(fallback).execute().use { response ->
            val body = response.body?.string() ?: throw Exception("Failed to load Zattoo token")
            val jsonObject = Gson().fromJson(body, JsonObject::class.java)
            if (jsonObject.has("session_token")) {
                return jsonObject.get("session_token").asString
            }
        }

        throw Exception("App Token not found")
    }

    private fun generateUuid(): String {
        val chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-"
        return (1..21).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }
}
