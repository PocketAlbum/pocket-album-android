package si.pocketalbum.view

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import android.widget.LinearLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import si.pocketalbum.R
import si.pocketalbum.core.ImageCache

class ImagesAdapter(ctx: Context, private val cache: ImageCache) : BaseAdapter() {

    private val info = cache.info
    private val inflater = LayoutInflater.from(ctx)

    override fun getCount(): Int {
        return info.imageCount
    }

    override fun getItem(p0: Int): Any {
        return 0
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getView(i: Int, existing: View?, viewGroup: ViewGroup?): View {
        val index = count - i - 1
        val view = existing ?: inflater.inflate(R.layout.item_image, null)
        val imageView = view.findViewById<ImageView>(R.id.imgThumbnail)

        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        if (viewGroup is GridView)
        {
            val w = viewGroup.columnWidth
            imageView.layoutParams = LinearLayout.LayoutParams(w, w)
        }
        imageView.setImageDrawable(null)

        val tag = imageView.tag
        if (tag is Job)
        {
            tag.cancel()
        }

        imageView.tag = CoroutineScope(Job() + Dispatchers.IO).launch {
            delay(100)
            try {
                val image = cache.getImage(index)
                val thumbnail = image.thumbnail
                val bm = BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.size)

                if (isActive) {
                    imageView.post {
                        imageView.setImageBitmap(bm)
                    }
                }
            }
            catch (e: Exception) {
                Log.e("APP", "Failed to load image", e)
            }
        }

        return view
    }
}