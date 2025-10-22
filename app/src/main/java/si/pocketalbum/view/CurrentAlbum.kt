package si.pocketalbum.view

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateFormat
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import si.pocketalbum.R
import si.pocketalbum.core.AlbumConnection
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import kotlin.math.ln
import kotlin.math.pow

class CurrentAlbum(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    private val lblName: TextView
    private val lblDescription: TextView
    private val lblPhotos: TextView
    private val lblSize: TextView
    private val lblYearRange: TextView
    private val lblCreated: TextView
    private val lblUpdated: TextView

    init {
        inflate(context, R.layout.view_current_album, this)
        lblName = findViewById(R.id.lblName)
        lblDescription = findViewById(R.id.lblDescription)
        lblPhotos = findViewById(R.id.lblPhotos)
        lblSize = findViewById(R.id.lblSize)
        lblYearRange = findViewById(R.id.lblYearRange)
        lblCreated = findViewById(R.id.lblCreated)
        lblUpdated = findViewById(R.id.lblUpdated)
    }

    @SuppressLint("SetTextI18n")
    fun showInfo(albumConnection: AlbumConnection)
    {
        val yearIndex = albumConnection.album.getYearIndex()
        val meta = albumConnection.album.getMetadata()
        lblName.text = meta.name
        lblDescription.text = meta.description
        lblPhotos.text = yearIndex.sumOf { it.count }.toString(10)
        lblSize.text = formatSize(albumConnection.fileSize ?: 0)
        lblUpdated.text = formatDate(meta.updated)
        lblCreated.text = formatDate(meta.created)
        if (yearIndex.count() == 0)
        {
            lblYearRange.text = "/"
        }
        else if (yearIndex.count() > 1) {
            lblYearRange.text = "${yearIndex.first().year} - ${yearIndex.last().year}"
        }
        else {
            lblYearRange.text = yearIndex.first().year.toString()
        }
    }

    fun formatDate(localDateTime: LocalDateTime): String {
        val date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant())
        val dateFormat = DateFormat.getDateFormat(context)
        return dateFormat.format(date)
    }

    private fun formatSize(bytes: Long): String {
        if (bytes < 1000) return "$bytes B"

        val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB", "EB")
        val exponent = (ln(bytes.toDouble()) / ln(1000.0)).toInt()
        val formattedSize = bytes / 1000.0.pow(exponent.toDouble())

        return String.format(Locale.getDefault(), "%.2f %s", formattedSize, units[exponent])
    }
}