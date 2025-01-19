package si.pocketalbum.core

import android.content.res.Resources.NotFoundException
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.util.LruCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlin.math.min

class ImageCache(private val album: IAlbum) {
    private val futures = LruCache<Int, Deferred<HashMap<Int, ImageThumbnail>>>(10)

    private fun getDeferred(block: Int): Deferred<HashMap<Int, ImageThumbnail>>
    {
        synchronized(futures)
        {
            val future = futures[block]
            if (future == null) {
                Log.i("ImageCache", "Loading block $block, cache size ${futures.size()}")
                val result = CoroutineScope(Job() + Dispatchers.IO).async {
                    loadImages(block)
                }
                futures.put(block, result)
                return result
            }
            else {
                return future
            }
        }
    }

    private fun loadImages(block: Int): HashMap<Int, ImageThumbnail>
    {
        val first = block * 100
        val last = ((block + 1) * 100) - 1
        val images = album.getImages(first, last)
        val result = HashMap<Int, ImageThumbnail>()
        for (i in 0 .. min(199, images.size - 1))
        {
            result[i + first] = images[i]
        }
        return result
    }

    suspend fun getImage(number: Int): ImageThumbnail
    {
        val block = number / 100
        val image = getDeferred(block).await()[number]
            ?: throw NotFoundException("Image number $number not found")
        return image
    }

    suspend fun getData(number: Int): Bitmap {
        val image = getImage(number)
        val data = album.getData(image.imageInfo.id)
        return BitmapFactory.decodeByteArray(data, 0, data.size)
    }

    fun getData(id: String): Bitmap {
        val data = album.getData(id)
        return BitmapFactory.decodeByteArray(data, 0, data.size)
    }
}