package de.christinecoenen.code.zapp.app.zattoo

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import de.christinecoenen.code.zapp.app.zattoo.api.ZattooApi
import de.christinecoenen.code.zapp.app.zattoo.model.ZattooSessionData
import de.christinecoenen.code.zapp.app.zattoo.model.ZattooStream
import com.google.gson.JsonObject
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.lang.reflect.Field

@RunWith(RobolectricTestRunner::class)
class ZattooServiceTest {

    private lateinit var context: Context
    private lateinit var zattooApi: ZattooApi
    private lateinit var zattooService: ZattooService
    private lateinit var okHttpClient: OkHttpClient

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        zattooApi = mock()
        okHttpClient = OkHttpClient()
        zattooService = ZattooService(context, okHttpClient, zattooApi)
    }

    @Test
    fun getStreamUrl_success() = runTest {
        // Setup
        val cid = "test_channel"
        val expectedUrl = "http://test.url/stream.m3u8"
        val mockSessionData = ZattooSessionData("hash123", JsonObject()) // account is not null

        // Mock API
        whenever(zattooApi.hello(any(), any(), any(), any(), any())).thenReturn(mockSessionData)
        whenever(zattooApi.getSession()).thenReturn(mockSessionData)
        whenever(zattooApi.watch(any(), any(), any())).thenReturn(ZattooStream(expectedUrl, null))

        // Inject App Token to avoid network call
        val appTokenField: Field = ZattooService::class.java.getDeclaredField("appToken")
        appTokenField.isAccessible = true
        appTokenField.set(zattooService, "mock_app_token")

        // Execute
        val result = zattooService.getStreamUrl(cid)

        // Verify
        assertEquals(expectedUrl, result)
    }
}
