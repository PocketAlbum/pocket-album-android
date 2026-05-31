@file:OptIn(ExperimentalUuidApi::class)

package si.pocketalbum.view

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ListView
import si.pocketalbum.R
import si.pocketalbum.StatisticsActivity
import si.pocketalbum.core.AlbumConnection
import si.pocketalbum.services.AlbumService
import kotlin.uuid.ExperimentalUuidApi

class AlbumsPanel (ctx: Context, attrs: AttributeSet?) : FrameLayout(ctx, attrs) {
    init {
        inflate(context, R.layout.view_panel_albums, this)

        findViewById<ImageButton>(R.id.btnClose).setOnClickListener {
            visibility = GONE
        }
    }

    fun showInfo(service: AlbumService, openedAlbum: AlbumConnection)
    {
        val lstAlbums = findViewById<ListView>(R.id.lstAlbums)
        val adapter = AlbumsAdapter(context, service.getAlbums(), openedAlbum)
        lstAlbums.adapter = adapter

        lstAlbums.setOnItemClickListener { adapterView, view, i, l ->
            val locator = adapter.getItem(i)
            val opened = locator.guid == openedAlbum.metadata.id

            if (opened) {
                val intent = Intent(context, StatisticsActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent)
            }
            else {
                service.loadAlbumAsync(locator)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        return true
    }
}