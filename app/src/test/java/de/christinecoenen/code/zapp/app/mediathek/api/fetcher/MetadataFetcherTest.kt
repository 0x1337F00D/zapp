package de.christinecoenen.code.zapp.app.mediathek.api.fetcher

import com.google.gson.Gson
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import kotlinx.coroutines.runBlocking
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class MetadataFetcherTest {

	@Test
	fun testArdFetcher() = runBlocking {
		val mockClient = mock<OkHttpClient>()
		val mockCall = mock<Call>()
		val gson = Gson()

		val json = """
			{
				"teasers": [
					{
						"id": "1",
						"longTitle": "Folge 2: Test Series | Staffel 1 (S01/E02)",
						"show": { "title": "Test Series" },
						"images": { "aspect16x9": { "src": "https://example.com/image.jpg?w={width}" } }
					}
				]
			}
		""".trimIndent()

		val response = Response.Builder()
			.code(200)
			.message("OK")
			.protocol(Protocol.HTTP_1_1)
			.request(Request.Builder().url("https://example.com").build())
			.body(json.toResponseBody())
			.build()

		whenever(mockClient.newCall(any())).doReturn(mockCall)
		whenever(mockCall.execute()).doReturn(response)

		val fetcher = ArdMediathekMetadataFetcher(mockClient, gson)
		val show = MediathekShow(
			apiId = "1",
			title = "Episode 2",
			channel = "ARD",
			videoUrl = "http://video",
			websiteUrl = "https://www.ardmediathek.de/video/Y3JpZDovL2Rhc2Vyc3RlLmRlL3RhZ2UtZGllLWVzLW5pY2h0LWdhYg/1"
		)

		val result = fetcher.fetch(show)

		assertEquals(1, result.seasonNumber)
		assertEquals(2, result.episodeNumber)
		assertEquals("Test Series", result.seriesTitle)
		assertEquals("https://example.com/image.jpg?w=1920", result.imageUrl)
	}

	@Test
	fun testZdfFetcher() = runBlocking {
		val mockClient = mock<OkHttpClient>()
		val mockCall = mock<Call>()
		val gson = Gson()

		val json = """
			{
				"seasonNumber": 12,
				"episodeNumber": 5,
				"brand": { "title": "Bettys Diagnose" },
				"teaser": {
					"imageWithoutLogo": {
						"layouts": {
							"dim1920X1080": "https://example.com/zdf_image.jpg"
						}
					}
				}
			}
		""".trimIndent()

		val response = Response.Builder()
			.code(200)
			.message("OK")
			.protocol(Protocol.HTTP_1_1)
			.request(Request.Builder().url("https://example.com").build())
			.body(json.toResponseBody())
			.build()

		whenever(mockClient.newCall(any())).doReturn(mockCall)
		whenever(mockCall.execute()).doReturn(response)

		val fetcher = ZdfMediathekMetadataFetcher(mockClient, gson)
		val show = MediathekShow(
			apiId = "2",
			title = "Betty",
			channel = "ZDF",
			videoUrl = "http://video",
			websiteUrl = "https://www.zdf.de/serien/bettys-diagnose/vertrauen-wagen-100.html"
		)

		val result = fetcher.fetch(show)

		assertEquals(12, result.seasonNumber)
		assertEquals(5, result.episodeNumber)
		assertEquals("Bettys Diagnose", result.seriesTitle)
		assertEquals("https://example.com/zdf_image.jpg", result.imageUrl)
	}
}
