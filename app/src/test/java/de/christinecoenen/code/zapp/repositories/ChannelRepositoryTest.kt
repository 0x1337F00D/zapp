package de.christinecoenen.code.zapp.repositories

import androidx.test.core.app.ApplicationProvider
import de.christinecoenen.code.zapp.app.livestream.api.IZappBackendApiService
import de.christinecoenen.code.zapp.app.zattoo.ZattooService
import de.christinecoenen.code.zapp.app.zattoo.model.ZattooChannel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ChannelRepositoryTest {

    private val testScope = TestScope()
    private val zappApi: IZappBackendApiService = mock()
    private val zattooService: ZattooService = mock()

    private lateinit var channelRepository: ChannelRepository

    @Before
    fun setup() {
        channelRepository = ChannelRepository(
            ApplicationProvider.getApplicationContext(),
            testScope,
            zappApi,
            zattooService
        )
    }

    @Test
    fun `tryLoadZattooChannels with forceReload calls logout`() = testScope.runTest {
        whenever(zattooService.getChannels()).thenReturn(emptyList())

        channelRepository.tryLoadZattooChannels(forceReload = true)

        verify(zattooService).logout()
        verify(zattooService).getChannels()
    }

    @Test
    fun `tryLoadZattooChannels without forceReload does not call logout`() = testScope.runTest {
        whenever(zattooService.getChannels()).thenReturn(emptyList())

        channelRepository.tryLoadZattooChannels(forceReload = false)

        verify(zattooService, never()).logout()
        verify(zattooService).getChannels()
    }

    @Test
    fun `checkZattooLogin returns true on success`() = testScope.runTest {
        val channel = mock<ZattooChannel> {
            on { cid } doReturn "cid"
            on { title } doReturn "title"
            on { qualities } doReturn emptyList()
        }
        whenever(zattooService.getChannels()).thenReturn(listOf(channel))

        val result = channelRepository.checkZattooLogin()

        verify(zattooService).logout()
        verify(zattooService).getChannels()
        Assert.assertTrue(result)
    }

    @Test
    fun `checkZattooLogin returns false on failure`() = testScope.runTest {
        whenever(zattooService.getChannels()).thenReturn(emptyList())

        val result = channelRepository.checkZattooLogin()

        verify(zattooService).logout()
        verify(zattooService).getChannels()
        Assert.assertFalse(result)
    }
}
