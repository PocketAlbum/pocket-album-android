package si.pocketalbum.view

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider.getUriForFile
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import si.pocketalbum.R
import si.pocketalbum.core.AlbumConnection
import si.pocketalbum.core.models.ImageInfo
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

class SlidingGallery(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs)
{
    private val vpgImages : ViewPager2
    private val lltDetails : LinearLayout
    private val lltActions : LinearLayout
    private val lblDateTime : TextView
    private val lblCoordinates : TextView
    private val btnMap : Button
    private val btnInfo : Button
    private val btnShare : Button
    private var connection: AlbumConnection? = null

    init {
        inflate(context, R.layout.view_sliding_gallery, this)
        vpgImages = findViewById(R.id.vpgImages)
        lltDetails = findViewById(R.id.lltDetails)
        lltActions = findViewById(R.id.lltActions)
        lblDateTime = findViewById(R.id.lblDateTime)
        lblCoordinates = findViewById(R.id.lblCoordinates)
        btnMap = findViewById(R.id.btnMap)
        btnInfo = findViewById(R.id.btnInfo)
        btnShare = findViewById(R.id.btnShare)

        ViewCompat.setOnApplyWindowInsetsListener(lltActions) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updateLayoutParams<MarginLayoutParams> {
                leftMargin = insets.left
                bottomMargin = insets.bottom
                rightMargin = insets.right
            }

            WindowInsetsCompat.CONSUMED
        }

        ViewCompat.setOnApplyWindowInsetsListener(lltDetails) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                        or WindowInsetsCompat.Type.displayCutout()
            )
            v.updateLayoutParams<MarginLayoutParams> {
                leftMargin = bars.left
                topMargin = bars.top
                bottomMargin = bars.bottom
                rightMargin = bars.right
            }
            WindowInsetsCompat.CONSUMED
        }
    }

    fun albumLoaded(connection: AlbumConnection) {
        this.connection = connection
        loadAlbum()
    }

    fun loadAlbum() {
        vpgImages.adapter = ImagesRecyclerAdapter(connection?.cache!!) {
            val v = if (lltDetails.visibility == VISIBLE) INVISIBLE else VISIBLE
            lltDetails.visibility = v
            lltActions.visibility = v
        }
        vpgImages.registerOnPageChangeCallback(object : OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                try {
                    CoroutineScope(Job() + Dispatchers.IO).launch {
                        val cache = connection?.cache!!
                        val im = cache.getImage(cache.info.imageCount - position - 1)
                        lltDetails.post {
                            displayImageInfo(im.imageInfo)
                        }
                    }
                }
                catch (e:Exception) {
                    Log.e("MainActivity", "Unable to load page $position", e)
                }
            }
        })
    }

    fun displayImageInfo(image: ImageInfo?) {
        if (image == null){
            lblDateTime.visibility = GONE
            lblCoordinates.visibility = GONE
            return
        }
        val created = formatDataTime(image.created)
        val coordinates = formatCoordinates(image.latitude, image.longitude)

        lblDateTime.text = created

        if (coordinates == null) {
            lblCoordinates.visibility = GONE
            btnMap.visibility = GONE
        }
        else {
            lblCoordinates.text = coordinates
            lblCoordinates.visibility = VISIBLE
            btnMap.visibility = VISIBLE
            btnMap.setOnClickListener {
                val uri = "geo:%f,%f?q=%f,%f(%s)".format(
                    Locale.ENGLISH,
                    image.latitude, image.longitude,
                    image.latitude, image.longitude, image.filename)
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                context.startActivity(intent)
            }
        }

        btnInfo.setOnClickListener {
            val ctx = context
            val dialog = Dialog(context, R.style.DialogWindowPrimary)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.dialog_image_info)

            dialog.findViewById<TextView>(R.id.lblFilename)
                .text = ctx.getString(R.string.file_name, image.filename)

            dialog.findViewById<TextView>(R.id.lblDateTime)
                .text = ctx.getString(R.string.created, created)

            dialog.findViewById<TextView>(R.id.lblCoordinates)
                .text = ctx.getString(R.string.location, coordinates)

            dialog.findViewById<TextView>(R.id.lblDimensions)
                .text = ctx.getString(R.string.dimensions, image.width, image.height)

            dialog.findViewById<TextView>(R.id.lblOriginalSize)
                .text = ctx.getString(R.string.original_size, formatSize(image.size))

            dialog.findViewById<TextView>(R.id.btnClose).setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }

        btnShare.setOnClickListener {
            val dir = File(context.cacheDir, "image-share")
            dir.mkdirs()
            val file = File(dir, image.filename)
            file.createNewFile()

            val fileStream = FileOutputStream(file)
            fileStream.write(connection?.album?.getData(image.id))
            fileStream.close()

            val uri: Uri = getUriForFile(context, "si.pocketalbum", file)
            ShareCompat.IntentBuilder(context)
                .setType("image/jpeg")
                .addStream(uri)
                .setChooserTitle("Share image")
                .setSubject("Shared image")
                .startChooser()
        }
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

    fun openImage(position: Int) {
        vpgImages.setCurrentItem(position, false)
        vpgImages.isUserInputEnabled = true
    }

    fun currentImage() : Int {
        return vpgImages.currentItem
    }
}
