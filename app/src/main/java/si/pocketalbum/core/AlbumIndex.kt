package si.pocketalbum.core

import android.util.Log
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import si.pocketalbum.UuidSerializer
import si.pocketalbum.core.models.MetadataModel
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream
import kotlin.io.path.pathString
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class AlbumIndex(val filesDir: Path) {
    private val albums: MutableList<AlbumLocator> = mutableListOf<AlbumLocator>()

    @Serializable
    data class AlbumLocator(
        val name: String,
        @Serializable(with = UuidSerializer::class)
        val guid: Uuid?,
        val uri: String)
    {
        override fun equals(other: Any?): Boolean {
            return other is AlbumLocator && uri == other.uri
        }

        override fun hashCode(): Int = uri.hashCode()
    }

    fun load()
    {
        var index = loadIndex()
        val localFiles = loadLocalFiles()
        albums.clear()
        albums.addAll(index.intersect(localFiles))
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun loadIndex(): List<AlbumLocator>
    {
        try {
            val stream = filesDir.resolve("album-index.json").inputStream()
            return Json.decodeFromStream<List<AlbumLocator>>(stream)
        }
        catch (e: Exception)
        {
            Log.e("AlbumIndex", "Failed to open album index", e)
            return emptyList()
        }
    }

    private fun loadLocalFiles(): List<AlbumLocator>
    {
        try {
            return Files.list(filesDir.resolve("albums"))
                .filter { f -> f.fileName.toString().endsWith(".sqlite") }
                .map { f -> AlbumLocator(f.fileName.toString(), Uuid.NIL, f.pathString) }
                .collect(Collectors.toList<AlbumLocator>())
        }
        catch (e: Exception)
        {
            Log.e("AlbumIndex", "Failed to list local albums")
            return emptyList()
        }
    }

    fun addAlbum(metadata: MetadataModel, uri: String): AlbumLocator
    {
        val locator = AlbumLocator(metadata.name, metadata.id, uri)
        if (albums.contains(locator))
        {
            throw IllegalStateException("Album with id ${metadata.id} already exists")
        }
        albums.add(locator)
        store()
        return locator
    }

    fun getAlbums(): List<AlbumLocator> {
        return this.albums
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun store()
    {
        try {
            val stream = filesDir.resolve("album-index.json").outputStream()
            Json.encodeToStream(albums, stream)
        }
        catch (e: Exception)
        {
            Log.e("AlbumIndex", "Failed to store album index", e)
        }
    }
}