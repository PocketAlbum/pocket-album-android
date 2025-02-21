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

class AlbumService : Service() {
    private val binder = LocalBinder()

    private var album: SQLiteAlbum? = null
    private var cache: ImageCache? = null

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
            val openedAlbum = SQLiteAlbum(this, dbFile)
            val newCache = ImageCache(openedAlbum, FilterModel(null, null))
            album = openedAlbum
            cache = newCache
        }
        else throw FileNotFoundException("File album.sqlite not found")
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
}