package si.pocketalbum.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import si.pocketalbum.R
import java.util.Locale
import kotlin.math.ln
import kotlin.math.pow

class AlbumView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    private val lblFilename: TextView
    private val lblSize: TextView

    init {
        inflate(context, R.layout.view_album_view, this)
        lblFilename = findViewById(R.id.lblFilename)
        lblSize = findViewById(R.id.lblSize)
    }

    fun showInfo(info: Pair<String?, Long>) {
        lblFilename.text = info.first
        lblSize.visibility = VISIBLE
        lblSize.text = formatSize(info.second)
    }

    fun clearInfo() {
        lblFilename.setText(R.string.no_file_selected)
        lblSize.visibility = GONE
    }

    private fun formatSize(bytes: Long): String {
        if (bytes < 1000) return "$bytes B"

        val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB", "EB")
        val exponent = (ln(bytes.toDouble()) / ln(1000.0)).toInt()
        val formattedSize = bytes / 1000.0.pow(exponent.toDouble())

        return String.format(Locale.getDefault(), "%.2f %s", formattedSize, units[exponent])
    }

}