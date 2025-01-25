package si.pocketalbum.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import si.pocketalbum.R
import si.pocketalbum.core.IAlbum
import si.pocketalbum.core.ImageCache

class ImagesRecyclerAdapter(
    album: IAlbum,
    private val cache: ImageCache,
    private val onImageActions: ImageControl.OnImageActions
)
    : RecyclerView.Adapter<ImagesRecyclerAdapter.ImageViewHolder>() {

    private val info = album.getInfo()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_pager_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun getItemCount(): Int {
        return info.imageCount
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.imageView.setImageDrawable(null)
        holder.imageView.resetTransform()
        CoroutineScope(Job() + Dispatchers.IO).launch {
            val bitmap = cache.getData(info.imageCount - position - 1)
            holder.imageView.post {
                holder.imageView.setImageBitmap(bitmap)
            }
        }
        holder.imageView.setOnImageActionsListener(onImageActions)
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val imageView = itemView.findViewById<ImageControl>(R.id.imgThumbnail)
    }
}