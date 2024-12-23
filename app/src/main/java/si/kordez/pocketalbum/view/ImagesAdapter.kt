package si.kordez.pocketalbum.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import android.widget.LinearLayout
import si.kordez.pocketalbum.R
import si.kordez.pocketalbum.core.IAlbum

class ImagesAdapter(ctx: Context, album: IAlbum, private val cache: ImageCache) : BaseAdapter() {

    private val info = album.getInfo()
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
        val view = existing ?: inflater.inflate(R.layout.item_image, null)
        val imgImage = view.findViewById<ImageView>(R.id.imgThumbnail)

        imgImage.scaleType = ImageView.ScaleType.CENTER_CROP
        if (viewGroup is GridView)
        {
            val w = viewGroup.columnWidth
            imgImage.layoutParams = LinearLayout.LayoutParams(w, w)
        }
        imgImage.setImageDrawable(null)

        cache.setThumbnail(i, imgImage)

        return view
    }
}