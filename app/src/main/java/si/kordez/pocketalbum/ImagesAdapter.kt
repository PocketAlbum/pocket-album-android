package si.kordez.pocketalbum

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import android.widget.LinearLayout
import si.kordez.pocketalbum.core.IAlbum
import si.kordez.pocketalbum.core.ImageCache


class ImagesAdapter(ctx: Context, album: IAlbum) : BaseAdapter() {

    val info = album.getInfo()
    val inflater = LayoutInflater.from(ctx)
    val cache = ImageCache(album)

    override fun getCount(): Int {
        return info.ImageCount
    }

    override fun getItem(p0: Int): Any {
        return "a"
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getView(i: Int, existing: View?, viewGroup: ViewGroup?): View {
        val view = existing ?: inflater.inflate(R.layout.item_image, null)
        val imgImage = view.findViewById<ImageView>(R.id.imgImage)

        imgImage.scaleType = ImageView.ScaleType.CENTER_CROP
        if (viewGroup is GridView)
        {
            val w = viewGroup.columnWidth
            imgImage.layoutParams = LinearLayout.LayoutParams(w, w)
        }
        imgImage.setImageDrawable(null)

        cache.get(i, imgImage)

        return view
    }
}