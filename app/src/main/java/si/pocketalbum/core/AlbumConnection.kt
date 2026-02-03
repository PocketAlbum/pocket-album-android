package si.pocketalbum.core

import android.content.Context
import si.pocketalbum.core.models.FilterModel
import si.pocketalbum.core.sqlite.SQLiteAlbum
import java.io.File

class AlbumConnection(val album: IAlbum, var cache: ImageCache, val heatmaps: HeatmapCache) {
    val metadata = album.getMetadata()
    val fileSize = if (album is SQLiteAlbum) album.fileSize else null

    companion object {
        fun open(context: Context, dbFile: File): AlbumConnection {
            val album = SQLiteAlbum(context, dbFile)
            val cache = ImageCache(album, FilterModel.Empty)
            val heatmaps = HeatmapCache.load(album, context)

            val connection = AlbumConnection(album, cache, heatmaps)
            connection.metadata.validate()

            return connection
        }
    }

    fun close() {
        album.close()
    }

    fun changeFilter(newFilter: FilterModel) {
        cache = ImageCache(album, newFilter)
    }

    fun buildHeatmaps(context: Context)
    {
        heatmaps.build(context)
    }
}