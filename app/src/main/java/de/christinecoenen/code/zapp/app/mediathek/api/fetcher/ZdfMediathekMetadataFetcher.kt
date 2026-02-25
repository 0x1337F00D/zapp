package de.christinecoenen.code.zapp.app.mediathek.api.fetcher

import com.google.gson.Gson
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.net.URI

class ZdfMediathekMetadataFetcher(
	private val client: OkHttpClient,
	private val gson: Gson
) : IMediathekMetadataFetcher {

	private val apiToken = "aa3noh4ohz9eeboo8shiesheec9ciequ9Quah7el"

	override suspend fun fetch(show: MediathekShow): MediathekShow = withContext(Dispatchers.IO) {
		val url = show.websiteUrl ?: return@withContext show

		val path = try {
			URI(url).path
		} catch (e: Exception) {
			return@withContext show
		}

		val cleanPath = path.removePrefix("/").removeSuffix(".html")
		val apiUrl = "https://api.zdf.de/content/documents/zdf/$cleanPath"

		val request = Request.Builder()
			.url(apiUrl)
			.addHeader("Api-Auth", "Bearer $apiToken")
			.addHeader("Host", "api.zdf.de")
			.build()

		try {
			client.newCall(request).execute().use { response ->
				if (!response.isSuccessful) return@withContext show

				val json = response.body?.string() ?: return@withContext show
				val data = gson.fromJson(json, ZdfResponse::class.java)

				var season = data.seasonNumber
				var episode = data.episodeNumber
				var seriesTitle = data.brand?.title

				// Try to find the highest resolution image
				// The map keys are usually "1920x1080", "1280x720" etc.
				var imageUrl = data.teaserImage
				val imagesMap = data.teaser?.imageWithoutLogo?.layouts

				if (imagesMap != null) {
					// Prefer 1920x1080, then 1280x720, then whatever is available
					imageUrl = imagesMap["dim1920X1080"]
						?: imagesMap["dim1280X720"]
						?: imagesMap.values.firstOrNull()
						?: imageUrl
				}

				// If season/episode are missing in root, check episodeInfo object
				if (season == null) season = data.episodeInfo?.seasonNumber
				if (episode == null) episode = data.episodeInfo?.episodeNumber

				show.copy(
					seriesTitle = seriesTitle ?: show.topic,
					seasonNumber = season,
					episodeNumber = episode,
					imageUrl = imageUrl
				)
			}
		} catch (e: Exception) {
			Timber.e(e)
			show
		}
	}

	data class ZdfResponse(
		val seasonNumber: Int?,
		val episodeNumber: Int?,
		val brand: ZdfBrand?,
		val teaser: ZdfTeaser?,
		val teaserImage: String?,
		val episodeInfo: ZdfEpisodeInfo?
	)

	data class ZdfBrand(val title: String?)
	data class ZdfTeaser(val imageWithoutLogo: ZdfImageContainer?)
	data class ZdfImageContainer(val layouts: Map<String, String>?)
	data class ZdfEpisodeInfo(val seasonNumber: Int?, val episodeNumber: Int?)
}
