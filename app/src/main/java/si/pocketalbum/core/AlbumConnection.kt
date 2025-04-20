package si.pocketalbum.core

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import si.pocketalbum.core.models.FilterModel
import si.pocketalbum.core.sqlite.SQLiteAlbum
import java.io.File

class AlbumConnection(val album: IAlbum, var cache: ImageCache, val heatmaps: HeatmapCache) {
    val metadata = album.getMetadata()

    companion object {
        fun open(context: Context, dbFile: File): AlbumConnection {
            val album = SQLiteAlbum(context, dbFile)
            val cache = ImageCache(album, FilterModel.Empty)
            val heatmaps = HeatmapCache.load(album, context)

            val connection = AlbumConnection(album, cache, heatmaps)
            connection.metadata.validate()
            CoroutineScope(Job() + Dispatchers.IO).launch {
                heatmaps.build(context)
            }
            return connection
        }
    }

    fun close() {
        album.close()
    }

    fun changeFilter(newFilter: FilterModel) {
        cache = ImageCache(album, newFilter)
    }
}