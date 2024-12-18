package si.kordez.pocketalbum

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import si.kordez.pocketalbum.core.IAlbum
import si.kordez.pocketalbum.core.ImageCache

class ImagesRecyclerAdapter(album: IAlbum, val cache: ImageCache)
    : RecyclerView.Adapter<ImagesRecyclerAdapter.ImageViewHolder>() {

    val info = album.getInfo()

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
        cache.setImage(position, holder.imageView)
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val imageView = itemView.findViewById<ImageView>(R.id.imgThumbnail);
    }
}