package si.kordez.pocketalbum.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import si.kordez.pocketalbum.R
import si.kordez.pocketalbum.core.IAlbum

class ImagesRecyclerAdapter(album: IAlbum, private val cache: ImageCache)
    : RecyclerView.Adapter<ImagesRecyclerAdapter.ImageViewHolder>() {

    private val info = album.getInfo()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_pager_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun getItemCount(): Int {
        return info.ImageCount
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.imageView.setImageDrawable(null)
        holder.imageView.resetTransform()
        cache.setImage(position, holder.imageView)
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val imageView = itemView.findViewById<ImageControl>(R.id.imgThumbnail);
    }
}