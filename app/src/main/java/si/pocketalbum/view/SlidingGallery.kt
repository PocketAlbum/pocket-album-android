package si.pocketalbum.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider.getUriForFile
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.mediarouter.app.MediaRouteButton
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import si.pocketalbum.R
import si.pocketalbum.core.AlbumConnection
import si.pocketalbum.core.models.ImageInfo
import si.pocketalbum.services.AlbumService
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
    private val btnBack: ImageButton
    private val btnMap : Button
    private val btnInfo : Button
    private val btnShare : Button
    private val pnlInfo: InfoPanel
    private val btnCast : MediaRouteButton
    private val pnlCast: LinearLayout
    private var connection: AlbumConnection? = null
    private var service: AlbumService? = null

    init {
        inflate(context, R.layout.view_sliding_gallery, this)
        vpgImages = findViewById(R.id.vpgImages)
        lltDetails = findViewById(R.id.lltDetails)
        lltActions = findViewById(R.id.lltActions)
        lblDateTime = findViewById(R.id.lblDateTime)
        lblCoordinates = findViewById(R.id.lblCoordinates)
        btnBack = findViewById(R.id.btnBack)
        btnMap = findViewById(R.id.btnMap)
        btnInfo = findViewById(R.id.btnInfo)
        btnShare = findViewById(R.id.btnShare)
        pnlInfo = findViewById(R.id.pnlInfo)
        btnCast = findViewById(R.id.btnCast)
        pnlCast = findViewById(R.id.pnlCast)

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

        btnBack.setOnClickListener {
            visibility = GONE
        }

        pnlCast.setOnClickListener {
            btnCast.performClick()
        }

        CastButtonFactory.setUpMediaRouteButton(context, btnCast)
    }

    fun albumLoaded(connection: AlbumConnection, service: AlbumService) {
        this.connection = connection
        this.service = service
        loadAlbum()
    }

    fun loadAlbum() {
        vpgImages.adapter = ImagesRecyclerAdapter(connection?.cache!!) {
            val v = if (lltDetails.isVisible && !pnlInfo.isVisible) INVISIBLE else VISIBLE
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

    @SuppressLint("SetTextI18n")
    fun displayImageInfo(image: ImageInfo?) {
        if (image == null){
            lblDateTime.visibility = GONE
            lblCoordinates.visibility = GONE
            return
        }
        val created = formatDataTime(image.created)
        val coordinates = formatCoordinates(image.latitude, image.longitude)

        lblDateTime.text = created
        pnlInfo.showInfo(image)

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
            pnlInfo.visibility = if (pnlInfo.isVisible) GONE else VISIBLE
        }

        btnShare.setOnClickListener {
            val dir = File(context.cacheDir, "image-share")
            dir.mkdirs()
            val file = File(dir, image.filename)
            file.createNewFile()

            val fileStream = FileOutputStream(file)
            fileStream.write(connection?.album?.getImageData(image.id))
            fileStream.close()

            val uri: Uri = getUriForFile(context, "si.pocketalbum", file)
            ShareCompat.IntentBuilder(context)
                .setType("image/jpeg")
                .addStream(uri)
                .setChooserTitle("Share image")
                .setSubject("Shared image")
                .startChooser()
        }

        service?.caster?.castPhoto(image)
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

    fun openImage(position: Int) {
        vpgImages.setCurrentItem(position, false)
        vpgImages.isUserInputEnabled = true
        visibility = VISIBLE
        pnlInfo.visibility = GONE
    }

    fun currentImage() : Int {
        return vpgImages.currentItem
    }
}
