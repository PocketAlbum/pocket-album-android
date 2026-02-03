package si.pocketalbum.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ListView
import si.pocketalbum.R
import si.pocketalbum.services.AlbumService

class AlbumsPanel (ctx: Context, attrs: AttributeSet?) : FrameLayout(ctx, attrs) {
    init {
        inflate(context, R.layout.view_panel_albums, this)

        findViewById<ImageButton>(R.id.btnClose).setOnClickListener {
            visibility = GONE
        }
    }

    fun showInfo(service: AlbumService)
    {
        val lstAlbums = findViewById<ListView>(R.id.lstAlbums)
        val adapter = AlbumsAdapter(context, service.getAlbums())
        lstAlbums.adapter = adapter

        lstAlbums.setOnItemClickListener { adapterView, view, i, l ->
            val locator = adapter.getItem(i)
            service.loadAlbumAsync(locator)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        return true
    }
}