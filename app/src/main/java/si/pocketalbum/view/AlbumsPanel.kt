package si.pocketalbum.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.ImageButton
import si.pocketalbum.R
import si.pocketalbum.core.AlbumConnection

class AlbumsPanel (context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    init {
        inflate(context, R.layout.view_panel_albums, this)

        findViewById<ImageButton>(R.id.btnClose).setOnClickListener {
            visibility = GONE
        }
    }

    fun showInfo(connection: AlbumConnection)
    {
        val albumView = findViewById<AlbumView>(R.id.albumView)
        val name = connection.album.getMetadata().name
        val size = connection.fileSize ?: 0
        albumView.showInfo(Pair(name, size))
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        return true
    }
}