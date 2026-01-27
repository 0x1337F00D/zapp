package de.christinecoenen.code.zapp.app.zattoo

import de.christinecoenen.code.zapp.app.zattoo.api.ZattooApi
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Assert.assertNotNull
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.UUID

class ZattooRealApiTest {

    private lateinit var api: ZattooApi
    private val username = System.getenv("ZATTOO_EMAIL")
    private val password = System.getenv("ZATTOO_PASSWORD")

    @Before
    fun setUp() {
        assumeTrue("Credentials not found", !username.isNullOrEmpty() && !password.isNullOrEmpty())

        val client = OkHttpClient.Builder()
             .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36")
                    .build()
                chain.proceed(request)
            }
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

        // Fetch App Token
        val tokenUrl = "https://zattoo.com/client/token.json?id=$uuid"
        val request = okhttp3.Request.Builder().url(tokenUrl).build()
        val client = OkHttpClient()
        val response = client.newCall(request).execute()
        val json = response.body?.string()
        assumeTrue("Failed to fetch app token", json != null)

        val jsonObject = com.google.gson.Gson().fromJson(json, com.google.gson.JsonObject::class.java)
        assumeTrue("Session token not found", jsonObject.has("session_token"))
        val appToken = jsonObject.get("session_token").asString

        // Test Hello
        val sessionResponse = api.hello(
            uuid = uuid,
            lang = "de",
            clientAppToken = appToken,
            appVersion = "3.2038.0",
            format = "json"
        )

        assertNotNull("Session response should not be null", sessionResponse)
        assert(sessionResponse.success)
        assertNotNull("Session data should not be null", sessionResponse.session)
        // In verify_zattoo.py, hello returned power_guide_hash
        assertNotNull("PowerGuideHash should not be null", sessionResponse.session!!.powerGuideHash)

        // Test Login
        val loginResponse = api.login(
            login = username!!,
            password = password!!,
            format = "json"
        )

        assertNotNull("Login response should not be null", loginResponse)
        assert(loginResponse.success)
        assertNotNull("Session data should not be null", loginResponse.session)
        assertNotNull("Account should not be null", loginResponse.session!!.account)
        assertNotNull("PowerGuideHash should not be null", loginResponse.session!!.powerGuideHash)
    }
}
