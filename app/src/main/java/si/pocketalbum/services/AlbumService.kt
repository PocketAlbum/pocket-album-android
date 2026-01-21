package si.pocketalbum.services

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import si.pocketalbum.UriUtils
import si.pocketalbum.core.AlbumConnection
import si.pocketalbum.core.AlbumIndex
import si.pocketalbum.core.AlbumIndex.AlbumLocator
import si.pocketalbum.core.HeatmapCache
import si.pocketalbum.core.IntegrityChecker
import si.pocketalbum.core.models.FilterModel
import si.pocketalbum.streaming.AlbumCaster
import si.pocketalbum.streaming.AlbumStreamer
import java.io.File
import java.io.FileNotFoundException
import java.time.Duration
import java.time.Instant
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class AlbumService : Service() {
    private val binder = LocalBinder()
    private var deferredConnection: Deferred<AlbumConnection>? = null
    private var streamer = AlbumStreamer(this)

    lateinit var caster: AlbumCaster
    private lateinit var index: AlbumIndex

    inner class LocalBinder : Binder() {
        fun getService(): AlbumService = this@AlbumService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Log.i("AlbumService", "Creating service")
        caster = AlbumCaster(applicationContext, streamer)
        index = AlbumIndex(filesDir.toPath())
        index.load()
        loadAlbumAsync()
    }

    fun loadAlbumAsync(locator: AlbumLocator? = null) {
        deferredConnection = CoroutineScope(Job() + Dispatchers.IO).async {
            loadAlbum(locator)
        }
    }

    private fun loadAlbum(locator: AlbumLocator? = null): AlbumConnection {
        try {
            val album = locator ?:
                index.getAlbums().firstOrNull() ?:
                throw FileNotFoundException("No albums in the index")

            val dbFile = File(album.uri)
            if (!dbFile.exists()) {
                throw FileNotFoundException("Album file ${album.uri} missing")
            }
            Log.i("AlbumService", "Opening album file ${album.uri}")
            val start = Instant.now()

            val connection = AlbumConnection.open(baseContext, dbFile)
            IntegrityChecker.checkAllYears(connection.album)

            val end = Instant.now()
            val duration = Duration.between(start, end)
            Log.i("AlbumService", "Opened album in ${duration.toMillis()} ms")

            CoroutineScope(Job() + Dispatchers.IO).launch {
                connection.buildHeatmaps(baseContext)
            }

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

    fun getAlbums(): List<AlbumLocator>
    {
        return index.getAlbums()
    }
    
    fun import(uri: Uri, progress: (Double) -> Unit): AlbumLocator {
        var tempFile: File? = null
        var dstFile: File? = null
        try {
            tempFile = File.createTempFile("album", ".sqlite", applicationContext.cacheDir)
            UriUtils.copyFile(applicationContext, uri, tempFile, progress)
            val connection = AlbumConnection.open(baseContext, tempFile)

            val albumsDir = File(applicationContext.filesDir, "albums")
            albumsDir.mkdirs()
            dstFile = File(albumsDir, "${connection.metadata.id}.sqlite")
            val locator = index.addAlbum(connection.metadata, dstFile.path)

            connection.close()

            tempFile.renameTo(dstFile)

            return locator
        }
        catch (e: Exception) {
            dstFile?.delete()
            throw e
        }
        finally {
            tempFile?.delete()
        }
    }
}