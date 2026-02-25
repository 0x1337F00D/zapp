package de.christinecoenen.code.zapp.app.mediathek.api.fetcher

import com.google.gson.Gson
import de.christinecoenen.code.zapp.models.shows.MediathekShow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.net.URI

class ArdMediathekMetadataFetcher(
	private val client: OkHttpClient,
	private val gson: Gson
) : IMediathekMetadataFetcher {

	override suspend fun fetch(show: MediathekShow): MediathekShow = withContext(Dispatchers.IO) {
		val url = show.websiteUrl ?: return@withContext show

		val pathSegments = try {
			URI(url).path.split("/")
		} catch (e: Exception) {
			return@withContext show
		}

		val assetId = pathSegments.firstOrNull { it.length > 20 && !it.contains("-") }
			?: return@withContext show

		val apiUrl = "https://api.ardmediathek.de/page-gateway/widgets/ard/asset/$assetId?pageNumber=0&pageSize=100&embedded=true&seasoned=false&seasonNumber=&withAudiodescription=false&withOriginalWithSubtitle=false&withOriginalversion=false&single=false"

		val request = Request.Builder()
			.url(apiUrl)
			.build()

		try {
			client.newCall(request).execute().use { response ->
				if (!response.isSuccessful) return@withContext show

				val json = response.body?.string() ?: return@withContext show
				val data = gson.fromJson(json, ArdWidgetResponse::class.java)

				// ARD Widget response is a list of items (teasers). We need to find the one matching our assetId if possible,
				// or just use the first valid one if the widget is specific to this asset.
				// However, the widget response for an asset ID typically contains the "teasers" list where the first item *might* be the show itself or related.
				// But looking at the curl output, the root object has a "teasers" array.

				// Let's assume the first teaser in the list that matches the ID or just the first one is relevant.
				// The curl output structure was:
				// { ..., "teasers": [ { "id": "...", "longTitle": "...", ... } ] }

				val teaser = data.teasers?.firstOrNull() ?: return@withContext show

				var season: Int? = null
				var episode: Int? = null
				val seriesTitle = teaser.show?.title
				val imageUrl = teaser.images?.aspect16x9?.src?.replace("{width}", "1920")

				// Regex extraction from title
				val longTitle = teaser.longTitle ?: teaser.shortTitle ?: ""

				// Pattern: "Folge 1: Tage, die es nicht gab | Staffel 1 (S01/E01) - Hörfassung"
				// Pattern: "S01/E01" or "Staffel 1" / "Folge 1"

				val sMatch = Regex("Staffel (\\d+)").find(longTitle)
				if (sMatch != null) season = sMatch.groupValues[1].toIntOrNull()

				val eMatch = Regex("Folge (\\d+)").find(longTitle)
				if (eMatch != null) episode = eMatch.groupValues[1].toIntOrNull()

				if (season == null || episode == null) {
					val seMatch = Regex("S(\\d+)/E(\\d+)").find(longTitle)
					if (seMatch != null) {
						season = seMatch.groupValues[1].toIntOrNull()
						episode = seMatch.groupValues[2].toIntOrNull()
					}
				}

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

	data class ArdWidgetResponse(
		val teasers: List<ArdTeaser>?
	)

	data class ArdTeaser(
		val id: String?,
		val longTitle: String?,
		val shortTitle: String?,
		val show: ArdShow?,
		val images: ArdImages?
	)

	data class ArdShow(val title: String?)
	data class ArdImages(val aspect16x9: ArdImage?)
	data class ArdImage(val src: String?)
}
