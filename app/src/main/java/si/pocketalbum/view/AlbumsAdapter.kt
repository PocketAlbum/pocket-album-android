@file:OptIn(ExperimentalUuidApi::class)

package si.pocketalbum.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.core.view.isVisible
import si.pocketalbum.R
import si.pocketalbum.core.AlbumConnection
import si.pocketalbum.core.AlbumIndex.AlbumLocator
import kotlin.uuid.ExperimentalUuidApi

class AlbumsAdapter(context: Context,
                    val albums: List<AlbumLocator>,
                    val openedAlbum: AlbumConnection)
    : BaseAdapter()
{
    private val inflater = LayoutInflater.from(context)

    override fun getCount(): Int = albums.size + 1

    override fun getItem(position: Int): AlbumLocator = albums[position]

    override fun getItemId(position: Int): Long = if (position < albums.size) 0 else 1

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup?
    ): View? {
        if (position == albums.size)
        {
            return convertView ?: inflater.inflate(R.layout.view_album_add, null)
        }

        var view = convertView ?: inflater.inflate(R.layout.view_album_view, null)
        val lblFilename = view.findViewById<TextView>(R.id.lblFilename)

        val lblStatusIndicator = view.findViewById<TextView>(R.id.lblStatusIndicator)

        val album = getItem(position)

        val opened = openedAlbum.metadata.id == album.guid
        lblStatusIndicator.isVisible = opened

        lblFilename.text = album.name
        return view
    }

}