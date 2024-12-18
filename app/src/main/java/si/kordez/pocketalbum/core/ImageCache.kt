package si.kordez.pocketalbum.core

import android.graphics.BitmapFactory
import android.util.Log
import android.widget.ImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.math.min

class ImageCache(val album: IAlbum) {
    val futures = HashMap<Int, Deferred<HashMap<Int, ImageThumbnail>>>()

    private fun getDeferred(block: Int): Deferred<HashMap<Int, ImageThumbnail>>
    {
        synchronized(futures)
        {
            val future = futures[block]
            if (future == null) {
                Log.i("ImageCache", "Starting loading images for block ${block}")
                val result = CoroutineScope(Job() + Dispatchers.IO).async {
                    loadImages(block)
                }
                futures[block] = result
                return result
            }
            else {
                return future
            }
        }
    }

    fun loadImages(block: Int): HashMap<Int, ImageThumbnail>
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

    fun setThumbnail(number: Int, imageView: ImageView)
    {
        val block = number / 100
        val deferred = getDeferred(block)
        CoroutineScope(Job() + Dispatchers.IO).launch {
            val image = deferred.await()[number]
            if (image != null) {
                val thumbnail = image.thumbnail
                val bm = BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.size)
                imageView.post {
                    imageView.tag = image
                    imageView.setImageBitmap(bm)
                }
            }
        }
    }

    fun setImage(number: Int, imageView: ImageView)
    {
        val block = number / 100
        val deferred = getDeferred(block)
        CoroutineScope(Job() + Dispatchers.IO).launch {
            val image = deferred.await()[number]
            if (image != null) {
                val data = album.getData(image.imageInfo.id)
                val bm = BitmapFactory.decodeByteArray(data, 0, data.size)
                imageView.post {
                    imageView.tag = image
                    imageView.setImageBitmap(bm)
                }
            }
        }
    }
}