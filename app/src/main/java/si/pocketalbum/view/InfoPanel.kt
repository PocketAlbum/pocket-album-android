package si.pocketalbum.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import si.pocketalbum.R
import si.pocketalbum.core.models.ImageInfo
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

class InfoPanel (context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    init {
        inflate(context, R.layout.view_panel_info, this)

        findViewById<ImageButton>(R.id.btnClose).setOnClickListener {
            visibility = GONE
        }
    }

    fun showInfo(image: ImageInfo)
    {
        val created = formatDataTime(image.created)
        val coordinates = formatCoordinates(image.latitude, image.longitude)

        findViewById<TextView>(R.id.lblFilename).text = image.filename
        findViewById<TextView>(R.id.lblDateTime).text = created
        findViewById<TextView>(R.id.lblCoordinates).text = coordinates
        findViewById<TextView>(R.id.lblDimensions).text = "${image.width} × ${image.height}"
        findViewById<TextView>(R.id.lblOriginalSize).text = formatSize(image.size)
    }

    private fun formatDataTime(isoDateTime: String): String {
        var iso = isoDateTime
        if (iso.endsWith(".000"))
        {
            iso = iso.substring(0, iso.length - 4)
        }
        val localDateTime = LocalDateTime.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val systemFormatter = DateTimeFormatter
            .ofLocalizedDateTime(java.time.format.FormatStyle.MEDIUM)
            .withLocale(Locale.getDefault())
        return localDateTime.format(systemFormatter)
    }

    private fun formatCoordinates(latitude: Double?, longitude: Double?): String? {
        if (latitude == null || longitude == null)
            return null

        val latDirection = if (latitude >= 0) "N" else "S"
        val lonDirection = if (longitude >= 0) "E" else "W"

        val formattedLatitude = "%.4f°%s".format(abs(latitude), latDirection)
        val formattedLongitude = "%.4f°%s".format(abs(longitude), lonDirection)

        return "$formattedLatitude $formattedLongitude"
    }

    private fun formatSize(size: Long):String {
        if (size > 1000000) {
            return "%.1f MB".format(size / 1000000f)
        } else if (size > 1000) {
            return "%.1f kB".format(size / 1000f)
        } else return "%d B".format(size)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        return true
    }
}