package de.christinecoenen.code.zapp.app.mediathek.api.fetcher

import de.christinecoenen.code.zapp.models.shows.MediathekShow

interface IMediathekMetadataFetcher {
	suspend fun fetch(show: MediathekShow): MediathekShow
}
