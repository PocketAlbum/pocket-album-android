package si.pocketalbum.core

import android.content.Context
import si.pocketalbum.core.models.FilterModel
import si.pocketalbum.core.sqlite.SQLiteAlbum
import java.io.File

class AlbumConnection(val album: IAlbum, var cache: ImageCache) {
    val metadata = album.getMetadata()

    companion object {
        fun open(context: Context, dbFile: File): AlbumConnection {
            val album = SQLiteAlbum(context, dbFile)
            val cache = ImageCache(album, FilterModel(null, null, null))

            val connection = AlbumConnection(album, cache)
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
}