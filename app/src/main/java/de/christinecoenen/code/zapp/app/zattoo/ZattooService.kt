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
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.UUID

class ZattooService(private val context: Context, baseClient: OkHttpClient) {

    private val settingsRepository = SettingsRepository(context)
    private val httpClient: OkHttpClient
    private val api: ZattooApi

    private var powerGuideHash: String? = null
    private var appToken: String? = null

    private val cookieStore = HashMap<String, List<Cookie>>()

    init {
         httpClient = baseClient.newBuilder()
            .cookieJar(object : CookieJar {
                override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                    cookieStore[url.host] = cookies
                }

                override fun loadForRequest(url: HttpUrl): List<Cookie> {
                    return cookieStore[url.host] ?: ArrayList()
                }
            })
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://zattoo.com/")
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit.create(ZattooApi::class.java)
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
        val response = api.watch(ZattooWatchBody(cid = cid))
        if (response.success && response.stream != null) {
            response.stream.url
        } else {
            throw Exception("Could not get stream URL for $cid")
        }
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

        val uuid = UUID.randomUUID().toString()
        val helloResponse = api.hello(ZattooHelloBody(uuid = uuid, clientAppToken = appToken!!))
        if (!helloResponse.success) throw Exception("Hello failed")

        val loginResponse = api.login(ZattooLoginBody(login = username, password = password))
        if (loginResponse.success && loginResponse.session != null) {
            powerGuideHash = loginResponse.session.powerGuideHash
        } else {
            throw Exception("Login failed")
        }
    }

    private fun fetchAppToken(): String {
        val uuid = UUID.randomUUID().toString()
        val request = Request.Builder().url("https://zattoo.com/client/token.json?id=$uuid").build()
        val response = httpClient.newCall(request).execute()
        val json = response.body?.string() ?: throw Exception("Failed to load Zattoo token")

        val jsonObject = Gson().fromJson(json, JsonObject::class.java)
        if (jsonObject.has("session_token")) {
            return jsonObject.get("session_token").asString
        } else {
            throw Exception("App Token not found")
        }
    }
}
