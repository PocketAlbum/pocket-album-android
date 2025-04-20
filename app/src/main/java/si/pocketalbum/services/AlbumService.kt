package si.pocketalbum.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import si.pocketalbum.core.AlbumConnection
import si.pocketalbum.core.HeatmapCache
import si.pocketalbum.core.models.FilterModel
import java.io.File
import java.io.FileNotFoundException
import java.time.Duration
import java.time.Instant

class AlbumService : Service() {
    private val binder = LocalBinder()

    private var deferredConnection: Deferred<AlbumConnection>? = null

    inner class LocalBinder : Binder() {
        fun getService(): AlbumService = this@AlbumService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Log.i("AlbumService", "Creating service")

        loadAlbumAsync()
    }

    fun loadAlbumAsync() {
        deferredConnection = CoroutineScope(Job() + Dispatchers.IO).async {
            loadAlbum()
        }
    }

    private fun loadAlbum(): AlbumConnection {
        try {
            val dbFile = File(filesDir, "album.sqlite")
            if (!dbFile.exists()) {
                throw FileNotFoundException("File album.sqlite not found")
            }
            Log.i("AlbumService", "Opening local album.sqlite")
            val start = Instant.now()
            val connection = AlbumConnection.open(baseContext, dbFile)
            val end = Instant.now()
            val duration = Duration.between(start, end)
            Log.i("AlbumService", "Opened album in ${duration.toMillis()} ms")
            return connection
        }
        catch (e: Exception) {
            Log.e("AlbumService", "Failed to load album", e)
            throw e
        }
    }

    override fun onDestroy() {
        Log.i("AlbumService", "Destroying service")
        try {
            deferredConnection?.getCompleted()?.close();
        }
        catch (e: Exception) {
            Log.i("AlbumService", "No album is loaded", e)
        }
    }

    fun getConnectionDeferred(): Deferred<AlbumConnection> {
        return deferredConnection ?: throw IllegalStateException("No deferred connection")
    }

    fun changeFilter(newFilter: FilterModel) {
        deferredConnection!!.getCompleted().changeFilter(newFilter)
    }

    fun getHeatmapCache(): HeatmapCache {
        try {
            return deferredConnection!!.getCompleted().heatmaps
        }
        catch (e: Exception) {
            throw IllegalStateException("No album is loaded", e)
        }
    }
}