package de.christinecoenen.code.zapp.utils.system

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Size
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class ImageHelperTest {

    @Test
    fun loadThumbnailAsync_success() = runBlocking {
        val context = mock(Context::class.java)
        val contentResolver = mock(ContentResolver::class.java)
        whenever(context.contentResolver).thenReturn(contentResolver)

        val expectedBitmap = mock(Bitmap::class.java)
        whenever(contentResolver.loadThumbnail(any(Uri::class.java), any(Size::class.java), any())).thenReturn(expectedBitmap)

        val result = ImageHelper.loadThumbnailAsync(context, "file:///tmp/test.jpg")

        assertNotNull(result)
        verify(contentResolver).loadThumbnail(any(Uri::class.java), any(Size::class.java), any())
        Unit
    }
}
