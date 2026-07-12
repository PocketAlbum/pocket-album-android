package si.pocketalbum.core

import android.content.Context
import si.pocketalbum.core.models.FilterModel
import si.pocketalbum.core.models.MetadataModel
import si.pocketalbum.core.sqlite.SQLiteAlbum
import java.io.File

class AlbumConnection(
    val album: IAlbum,
    var cache: ImageCache,
    val heatmaps: HeatmapCache,
    val metadata: MetadataModel
) {
    val fileSize = if (album is SQLiteAlbum) album.fileSize else null

    companion object {
        suspend fun open(context: Context, dbFile: File): AlbumConnection {
            val album = SQLiteAlbum(context, dbFile)
            val cache = ImageCache.load(album)
            val heatmaps = HeatmapCache.load(album, context)

            val metadata = album.getMetadata()
            metadata.validate()
            val connection = AlbumConnection(album, cache, heatmaps, metadata)

            return connection
        }
    }

    fun close() {
        album.close()
    }

    suspend fun changeFilter(newFilter: FilterModel) {
        cache = ImageCache.load(album, newFilter)
    }

    suspend fun buildHeatmaps(context: Context)
    {
        heatmaps.build(context)
    }
}