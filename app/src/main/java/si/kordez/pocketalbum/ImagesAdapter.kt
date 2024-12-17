package si.kordez.pocketalbum

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

class ImagesAdapter(ctx: Context) : BaseAdapter() {

    val inflater = LayoutInflater.from(ctx)

    override fun getCount(): Int {
        return 80 * 4000
    }

    override fun getItem(p0: Int): Any {
        return "a"
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getView(i: Int, existing: View?, viewGroup: ViewGroup?): View {
        val view = existing ?: inflater.inflate(R.layout.item_image, null)

        return view
    }

}