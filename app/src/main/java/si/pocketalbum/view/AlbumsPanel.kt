@file:OptIn(ExperimentalUuidApi::class)

package si.pocketalbum.view

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ListView
import si.pocketalbum.ImportActivity
import si.pocketalbum.R
import si.pocketalbum.StatisticsActivity
import si.pocketalbum.core.AlbumConnection
import si.pocketalbum.core.AlbumIndex
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
            if (i == adapter.count - 1)
            {
                val intent = Intent(context, ImportActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent)
            }
            else {
                val locator = adapter.getItem(i)
                val opened = locator.guid == openedAlbum.metadata.id

                if (opened) {
                    val intent = Intent(context, StatisticsActivity::class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent)
                } else {
                    service.loadAlbumAsync(locator)
                }
            }
        }

        lstAlbums.onItemLongClickListener = object : AdapterView.OnItemLongClickListener
        {
            override fun onItemLongClick(parent: AdapterView<*>?, view: View?, position: Int,
                                         id: Long): Boolean
            {

                if (position < adapter.count - 1)
                {
                    showDeleteDialog(adapter.getItem(position), service)
                    return true
                }
                return false
            }
        }
    }

    fun showDeleteDialog(album: AlbumIndex.AlbumLocator, service: AlbumService)
    {
        val dialogClickListener: DialogInterface.OnClickListener =
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    if (which == DialogInterface.BUTTON_POSITIVE)
                    {
                        service.deleteAlbum(album)
                    }
                }
            }

        val builder = AlertDialog.Builder(context)
        builder.setMessage("Do you wish to delete album '${album.name}' from application storage? " +
                "This operation can not be reversed.")
            .setPositiveButton("Yes", dialogClickListener)
            .setNegativeButton("No", dialogClickListener)
            .show()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        return true
    }
}