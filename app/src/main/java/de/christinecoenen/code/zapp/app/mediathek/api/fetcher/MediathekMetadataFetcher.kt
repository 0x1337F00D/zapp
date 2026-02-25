package de.christinecoenen.code.zapp.app.mediathek.api.fetcher

import de.christinecoenen.code.zapp.models.shows.MediathekShow

class MediathekMetadataFetcher(
	private val ardFetcher: ArdMediathekMetadataFetcher,
	private val zdfFetcher: ZdfMediathekMetadataFetcher
) {

	suspend fun fetch(show: MediathekShow): MediathekShow {
		val url = show.websiteUrl ?: return show

		return when {
			url.contains("zdf.de") -> zdfFetcher.fetch(show)
			url.contains("ardmediathek.de") -> ardFetcher.fetch(show)
			else -> show
		}
	}
}
