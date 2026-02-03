package si.pocketalbum.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import si.pocketalbum.R
import si.pocketalbum.core.AlbumIndex.AlbumLocator

class AlbumsAdapter(context: Context, val albums: List<AlbumLocator>) : BaseAdapter()
{
    private val inflater = LayoutInflater.from(context)

    override fun getCount(): Int = albums.size

    override fun getItem(position: Int): AlbumLocator = albums[position]

    override fun getItemId(position: Int): Long = 0

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup?
    ): View? {
        var view = convertView ?: inflater.inflate(R.layout.view_album_view, null)
        val lblFilename = view.findViewById<TextView>(R.id.lblFilename)

        val album = getItem(position)

        lblFilename.text = album.name
        return view
    }

}