package si.pocketalbum.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import si.pocketalbum.core.IAlbum
import si.pocketalbum.core.ImageCache
import si.pocketalbum.core.models.FilterModel
import si.pocketalbum.core.sqlite.SQLiteAlbum
import java.io.File
import java.io.FileNotFoundException
import java.time.Duration
import java.time.Instant

class AlbumService : Service() {
    private val binder = LocalBinder()

    private var album: SQLiteAlbum? = null
    private var cache: ImageCache? = null
    private var filter = FilterModel(null, null, null)

    inner class LocalBinder : Binder() {
        fun getService(): AlbumService = this@AlbumService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Log.i("AlbumService", "Creating service")
        try {
            reloadAlbumFile()
        }
        catch (e: Exception) {
            Log.e("AlbumService", "Failed to load album", e)
        }
    }

    fun reloadAlbumFile() {
        val dbFile = File(filesDir, "album.sqlite")
        if (dbFile.exists()) {
            album = SQLiteAlbum(this, dbFile)
            reloadCache()
        }
        else throw FileNotFoundException("File album.sqlite not found")
    }

    private fun reloadCache() {
        val start = Instant.now()
        cache = ImageCache(album!!, filter)
        val end = Instant.now()
        val duration = Duration.between(start, end)
        Log.i("AlbumService", "Album cache loaded in ${duration.toMillis()} ms")
    }

    override fun onDestroy() {
        Log.i("AlbumService", "Destroying service")
        album?.close()
    }

    fun getAlbum(): IAlbum {
        return album ?: throw IllegalStateException("No album is loaded")
    }

    fun getCache(): ImageCache {
        return cache ?: throw IllegalStateException("No album is loaded")
    }

    fun isAlbumLoaded(): Boolean {
        return album != null && cache != null
    }

    fun filterChanged(filter: FilterModel) {
        this.filter = filter
        reloadCache()
    }
}