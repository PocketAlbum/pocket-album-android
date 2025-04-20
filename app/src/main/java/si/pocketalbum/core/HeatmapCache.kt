@file:OptIn(ExperimentalUuidApi::class, ExperimentalSerializationApi::class)

package si.pocketalbum.core

import android.content.Context
import android.util.Log
import dev.is_a.jakakordez.mapsforge.heatmap.Heatmap
import dev.is_a.jakakordez.mapsforge.heatmap.HeatmapBuilder
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import org.mapsforge.core.model.LatLong
import si.pocketalbum.core.models.FilterModel
import si.pocketalbum.core.models.Interval
import si.pocketalbum.core.models.YearIndex
import java.io.File
import java.util.stream.Collectors
import kotlin.uuid.ExperimentalUuidApi

class HeatmapCache(val album: IAlbum, val years: MutableMap<Int, Heatmap>) {
    @Serializable
    data class YearHeatmap(val year: Int, val crc: UInt, val heatmap: Heatmap)

    var listener: (()->Unit)? = null

    fun subscribe(listener: () -> Unit) {
        this.listener = listener
    }

    companion object {
        fun load(album: IAlbum, context: Context): HeatmapCache {
            val id = album.getMetadata().id
            val cacheFile = File(context.cacheDir, "$id-heatmap.cbor")
            var cache = mutableMapOf<Int, Heatmap>()
            try {
                val array = cacheFile.readBytes()
                val cached = Cbor.decodeFromByteArray<List<YearHeatmap>>(array)
                val yearIndex = album.getYearIndex().associate { it.year to it }
                Log.i("HeatmapCache", "Loaded heatmap cache from file, found ${cached.size} years")
                cache = cached
                    .filter {
                        val yearInfo = yearIndex.getOrDefault(it.year, null)
                        yearInfo?.crc == it.crc
                    }
                    .associate { it.year to it.heatmap }
                    .toMutableMap()
            }
            catch (e: Exception) {
                Log.e("HeatmapCache", "Unable to load heatmap cache", e)
            }
            Log.i("HeatmapCache", "Initializing cache with valid ${cache.size} years")
            return HeatmapCache(album, cache)
        }
    }

    fun build(context: Context) {
        Log.i("HeatmapCache", "Starting to build missing heatmaps")
        val yearIndex = album.getYearIndex()
        yearIndex.forEach {
            if (!years.containsKey(it.year)) {
                try {
                    val yearHeatmap = buildYear(it.year)
                    years.put(it.year, yearHeatmap)
                    listener?.invoke()
                }
                catch (e: Exception)
                {
                    Log.e("HeatmapCache", "Unable to generate heatmap for year ${it.year}", e)
                }
            }
        }
        Log.i("HeatmapCache", "Saving updated heatmap cache")
        save(context, yearIndex)
    }

    fun save(context: Context, yearIndex: List<YearIndex>) {
        val id = album.getMetadata().id
        val cacheFile = File(context.cacheDir, "$id-heatmap.cbor")
        val cacheList = yearIndex
            .filter { years.containsKey(it.year) }
            .map { YearHeatmap(it.year, it.crc, years.get(it.year)!!) }
        val array = Cbor.encodeToByteArray(cacheList)
        cacheFile.writeBytes(array)
        Log.i("HeatmapCache", "Heatmap cache size: ${array.size / 1000} kB")
    }

    private fun buildYear(year: Int): Heatmap {
        Log.i("HeatmapCache", "Building heatmap for year $year")

        val filter = FilterModel(Interval(year.toLong()), null, null)
        val info = album.getInfo(filter)
        val images = album.getImages(filter, Interval(0, info.imageCount.toLong()))

        val locations = images.stream()
            .map { it.imageInfo }
            .filter { it.latitude != null && it.longitude != null }
            .map { LatLong(it.latitude!!, it.longitude!!) }
            .collect(Collectors.toList())

        Log.i("HeatmapCache", "Found ${locations.size} location from ${images.size} images")

        if (locations.isEmpty()) {
            throw IllegalStateException("No locations found for year $year")
        }

        val builder = HeatmapBuilder(HeatmapBuilder.Options())
        builder.feed(locations)

        return builder.build()
    }
}