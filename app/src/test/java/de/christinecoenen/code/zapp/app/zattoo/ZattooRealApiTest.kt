package de.christinecoenen.code.zapp.app.zattoo

import de.christinecoenen.code.zapp.app.zattoo.api.ZattooApi
import kotlinx.coroutines.test.runTest
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Assert.assertNotNull
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.UUID

class ZattooRealApiTest {

    private lateinit var api: ZattooApi
    private lateinit var client: OkHttpClient
    private val username = System.getenv("ZATTOO_EMAIL")
    private val password = System.getenv("ZATTOO_PASSWORD")
    private val cookieStore = HashMap<String, List<Cookie>>()

    @Before
    fun setUp() {
        assumeTrue("Credentials not found", !username.isNullOrEmpty() && !password.isNullOrEmpty())

        client = OkHttpClient.Builder()
             .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36")
                    .build()
                chain.proceed(request)
            }
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
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(ZattooApi::class.java)
    }

    @Test
    fun testRealLogin() = runTest {
        val uuid = UUID.randomUUID().toString()

        // Manually set UUID cookie
        val cookie = Cookie.Builder()
            .domain("zattoo.com")
            .path("/")
            .name("uuid")
            .value(uuid)
            .build()
        cookieStore["zattoo.com"] = listOf(cookie)

        // Fetch App Token
        val tokenUrl = "https://zattoo.com/client/token.json?id=$uuid"
        val request = Request.Builder().url(tokenUrl).build()
        val response = client.newCall(request).execute()
        val json = response.body?.string()
        assumeTrue("Failed to fetch app token", json != null)

        val jsonObject = com.google.gson.Gson().fromJson(json, com.google.gson.JsonObject::class.java)
        assumeTrue("Session token not found", jsonObject.has("session_token"))
        val appToken = jsonObject.get("session_token").asString

        // Test Hello
        val sessionData = api.hello(
            uuid = uuid,
            lang = "de",
            clientAppToken = appToken,
            appVersion = "4.26.2",
            format = "json"
        )

        assertNotNull("Session data should not be null", sessionData)
        assert(sessionData.success != false) { "Session success is false. Data: $sessionData" }
        // In verify_zattoo.py, hello returned power_guide_hash
        assertNotNull("PowerGuideHash should not be null", sessionData.powerGuideHash)

        // Test Login
        val loginData = api.login(
            login = username!!,
            password = password!!,
            format = "json"
        )

        assertNotNull("Login data should not be null", loginData)
        assert(loginData.success != false)
        assertNotNull("Account should not be null", loginData.account)
        assertNotNull("PowerGuideHash should not be null", loginData.powerGuideHash)
    }
}
