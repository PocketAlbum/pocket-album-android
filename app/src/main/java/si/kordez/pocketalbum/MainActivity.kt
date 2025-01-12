package si.kordez.pocketalbum

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.GridView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
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
import si.kordez.pocketalbum.core.IAlbum
import si.kordez.pocketalbum.core.ImageInfo
import si.kordez.pocketalbum.core.sqlite.SQLiteAlbum
import si.kordez.pocketalbum.core.ImageCache
import si.kordez.pocketalbum.view.ImagesAdapter
import si.kordez.pocketalbum.view.ImagesRecyclerAdapter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

class MainActivity : ComponentActivity() {

    private var album : IAlbum? = null
    private var cache : ImageCache? = null

    private var lblDateTime : TextView? = null
    private var lblCoordinates : TextView? = null
    private var btnMap : Button? = null
    private var btnInfo : Button? = null
    private var btnShare : Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val lstImages = findViewById<GridView>(R.id.lstImages)
        val vpgImages = findViewById<ViewPager2>(R.id.vpgImages)
        val lltDetails = findViewById<LinearLayout>(R.id.lltDetails)
        val lltActions = findViewById<LinearLayout>(R.id.lltActions)
        lblDateTime = findViewById(R.id.lblDateTime)
        lblCoordinates = findViewById(R.id.lblCoordinates)
        btnMap = findViewById(R.id.btnMap)
        btnInfo = findViewById(R.id.btnInfo)
        btnShare = findViewById(R.id.btnShare)

        ViewCompat.setOnApplyWindowInsetsListener(lltActions) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
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
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = bars.left
                topMargin = bars.top
                bottomMargin = bars.bottom
                rightMargin = bars.right
            }
            WindowInsetsCompat.CONSUMED
        }

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true)
        {
            override fun handleOnBackPressed() {
                if (vpgImages.visibility == VISIBLE)
                {
                    vpgImages.visibility = GONE
                    lltDetails.visibility = INVISIBLE
                    lltActions.visibility = INVISIBLE
                }
                else finish()
            }
        })

        try {
            val album = SQLiteAlbum(this)
            val cache = ImageCache(album)
            this.album = album
            this.cache = cache

            vpgImages.adapter = ImagesRecyclerAdapter(album, cache) {
                val v = if (lltDetails.visibility == VISIBLE) INVISIBLE else VISIBLE
                lltDetails.visibility = v
                lltActions.visibility = v
            }
            vpgImages.registerOnPageChangeCallback(object : OnPageChangeCallback(){
                override fun onPageSelected(position: Int) {
                    try {
                        CoroutineScope(Job() + Dispatchers.IO).launch {
                            val im = cache.getImage(position)
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

            lstImages.adapter = ImagesAdapter(baseContext, album, cache)
            lstImages.setOnItemClickListener { adapterView, view, i, l ->
                vpgImages.setCurrentItem(i, false)
                vpgImages.isUserInputEnabled = true
                vpgImages.visibility = VISIBLE
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Unable to load album", e)
        }
    }

    fun displayImageInfo(image: ImageInfo?) {
        if (image == null){
            lblDateTime?.visibility = GONE
            lblCoordinates?.visibility = GONE
            return
        }
        val created = formatDataTime(image.created)
        val coordinates = formatCoordinates(image.latitude, image.longitude)

        lblDateTime?.text = created

        if (coordinates == null) {
            lblCoordinates?.visibility = GONE
            btnMap?.visibility = GONE
        }
        else {
            lblCoordinates?.text = coordinates
            lblCoordinates?.visibility = VISIBLE
            btnMap?.visibility = VISIBLE
            btnMap?.setOnClickListener {
                val uri = "geo:%f,%f?q=%f,%f(%s)".format(Locale.ENGLISH,
                    image.latitude, image.longitude,
                    image.latitude, image.longitude, image.filename)
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                startActivity(intent)
            }
        }

        btnInfo?.setOnClickListener {
            val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.dialog_image_info)

            dialog.findViewById<TextView>(R.id.lblFilename)
                .text = "File name: ${image.filename}"

            dialog.findViewById<TextView>(R.id.lblDateTime)
                .text = "Created: ${created}"

            dialog.findViewById<TextView>(R.id.lblCoordinates)
                .text = "Location: ${coordinates}"

            dialog.findViewById<TextView>(R.id.lblDimensions)
                .text = "Dimensions: ${image.width} × ${image.height}"

            dialog.findViewById<TextView>(R.id.lblOriginalSize)
                .text = "Original size: ${formatSize(image.size)}"

            dialog.findViewById<TextView>(R.id.btnClose).setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }

        btnShare?.setOnClickListener {
            val dir = File(cacheDir, "image-share")
            dir.mkdirs()
            val file = File(dir, image.filename)
            file.createNewFile()

            val fileStream = FileOutputStream(file)
            fileStream.write(album?.getData(image.id))
            fileStream.close()

            val uri: Uri = getUriForFile(this, "si.kordez.pocketalbum", file)
            ShareCompat.IntentBuilder(this)
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
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // For Android 8.0+ (API level 26+)
            val localDateTime = LocalDateTime.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val systemFormatter = DateTimeFormatter
                .ofLocalizedDateTime(java.time.format.FormatStyle.MEDIUM)
                .withLocale(Locale.getDefault())
            localDateTime.format(systemFormatter)
        } else {
            // For older Android versions
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = isoFormat.parse(isoDateTime) ?: return ""
            val systemFormat = SimpleDateFormat.getDateTimeInstance(
                SimpleDateFormat.MEDIUM,
                SimpleDateFormat.MEDIUM,
                Locale.getDefault()
            )
            systemFormat.format(date)
        }
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
}
