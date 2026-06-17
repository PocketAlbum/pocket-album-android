@file:OptIn(ExperimentalUuidApi::class)

package si.pocketalbum.view

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import si.pocketalbum.ImportActivity
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
                    val album = adapter.getItem(position)
                    showDeleteAlbumDialog(album.name)
                    {
                        service.deleteAlbum(album)
                    }
                    return true
                }
                return false
            }
        }
    }

    fun showDeleteAlbumDialog(
        albumName: String,
        onDeleteConfirmed: () -> Unit
    ) {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.dialog_delete_album, null)

        val txtAlbumName = view.findViewById<TextView>(R.id.txtAlbumName)
        val edtConfirmation = view.findViewById<EditText>(R.id.edtConfirmation)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val btnDelete = view.findViewById<Button>(R.id.btnDelete)

        txtAlbumName.text = albumName

        val dialog = AlertDialog.Builder(context)
            .setView(view)
            .create()

        btnDelete.setOnClickListener {
            if (edtConfirmation.text.toString().trim() == albumName) {
                dialog.dismiss()
                onDeleteConfirmed()
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        return true
    }
}