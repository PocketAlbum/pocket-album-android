package si.pocketalbum.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import si.pocketalbum.core.IAlbum
import si.pocketalbum.core.ImageCache
import si.pocketalbum.core.sqlite.SQLiteAlbum

class AlbumService : Service() {
    private val binder = LocalBinder()

    private lateinit var album: SQLiteAlbum
    private lateinit var cache: ImageCache

    inner class LocalBinder : Binder() {
        fun getService(): AlbumService = this@AlbumService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Log.i("AlbumService", "Creating service")
        album = SQLiteAlbum(this)
        cache = ImageCache(album)
    }

    override fun onDestroy() {
        Log.i("AlbumService", "Destroying service")
        album.close()
    }

    fun getAlbum(): IAlbum {
        return album
    }

    fun getCache(): ImageCache {
        return cache
    }
}