package de.christinecoenen.code.zapp.app.mediathek.controller.downloads

import android.content.Context
import android.app.NotificationManager
import android.net.ConnectivityManager
import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkManager
import de.christinecoenen.code.zapp.app.settings.repository.SettingsRepository
import de.christinecoenen.code.zapp.models.shows.DownloadStatus
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import de.christinecoenen.code.zapp.models.shows.PersistedMediathekShow
import de.christinecoenen.code.zapp.repositories.MediathekRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.job
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class WorkManagerDownloadControllerTest {

    private val applicationContext: Context = mock()
    private val mediathekRepository: MediathekRepository = mock()
    private val settingsRepository: SettingsRepository = mock()
    private val downloadFileInfoManager: DownloadFileInfoManager = mock()
    private val workManager: WorkManager = mock()
    private val notificationManager: NotificationManagerCompat = mock()
    private val connectivityManager: ConnectivityManager = mock()

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var controller: WorkManagerDownloadController

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        whenever(applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(connectivityManager)
        whenever(applicationContext.getString(any())).thenReturn("Mock Channel Name")
        whenever(applicationContext.getSystemService(NotificationManager::class.java)).thenReturn(mock())

        // Mock WorkManager LiveData
        val mockLiveData = androidx.lifecycle.MutableLiveData<List<androidx.work.WorkInfo>>()
        whenever(workManager.getWorkInfosByTagLiveData(any())).thenReturn(mockLiveData)

        controller = WorkManagerDownloadController(
            applicationContext,
            testScope,
            mediathekRepository,
            settingsRepository,
            downloadFileInfoManager,
            workManager,
            notificationManager,
            connectivityManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun deleteDownloadsWithDeletedFiles_Benchmark() = testScope.runTest {
        // Prepare
        val showCount = 100
        val shows = (1..showCount).map { id ->
            val mediathekShow = MediathekShow(
                apiId = "apiId$id",
                title = "Title $id",
                channel = "Channel",
                videoUrl = "http://example.com/video$id.mp4"
            )
            PersistedMediathekShow(
                id = id,
                mediathekShow = mediathekShow,
                downloadStatus = DownloadStatus.COMPLETED,
                downloadedVideoPath = "/tmp/video$id.mp4",
                downloadId = id
            )
        }

        whenever(mediathekRepository.getCompletedDownloads()).thenReturn(flowOf(shows))
        whenever(downloadFileInfoManager.shouldDeleteDownload(any())).thenReturn(true)
        whenever(mediathekRepository.updateDownloadedVideoPath(any(), anyOrNull())).thenReturn(Unit)
        whenever(mediathekRepository.updateDownloadProgress(any(), any())).thenReturn(Unit)
        whenever(mediathekRepository.updateShow(any())).thenReturn(Unit)
        whenever(mediathekRepository.updateShows(any())).thenReturn(Unit)

        // Act
        controller.deleteDownloadsWithDeletedFiles()
        advanceUntilIdle()

        // Verify / Measure
        // optimized: 1 updateShows call, 0 others.

        verify(mediathekRepository, org.mockito.kotlin.timeout(10000).times(1)).updateShows(any())
        verify(mediathekRepository, org.mockito.kotlin.never()).updateDownloadedVideoPath(any(), anyOrNull())
        verify(mediathekRepository, org.mockito.kotlin.never()).updateDownloadProgress(any(), any())
        verify(mediathekRepository, org.mockito.kotlin.never()).updateShow(any())

        println("Benchmark: Verified 1 batch DB update for $showCount items.")

        testScope.coroutineContext.job.cancelChildren()
    }
}
