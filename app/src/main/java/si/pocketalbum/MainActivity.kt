package si.pocketalbum

import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.GridView
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import si.pocketalbum.core.ImageCache
import si.pocketalbum.core.sqlite.SQLiteAlbum
import si.pocketalbum.view.DateScroller
import si.pocketalbum.view.ImagesAdapter
import si.pocketalbum.view.SlidingGallery

class MainActivity : ComponentActivity() {

    private val LAST_POSITION = "LAST_POSITION"
    private val GALLERY_OPEN = "GALLERY_OPEN"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val lstImages = findViewById<GridView>(R.id.lstImages)
        val slidingGallery = findViewById<SlidingGallery>(R.id.slidingGallery)
        val dateScroller = findViewById<DateScroller>(R.id.dateScroller)

        lstImages.post {
            val size = resources.getDimensionPixelSize(R.dimen.tile_size)
            lstImages.numColumns = lstImages.width / size
        }

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true)
        {
            override fun handleOnBackPressed() {
                if (slidingGallery.visibility == VISIBLE)
                {
                    slidingGallery.visibility = GONE
                }
                else finish()
            }
        })

        try {
            val album = SQLiteAlbum(this)
            val cache = ImageCache(album)

            dateScroller.setAlbum(album)

            slidingGallery?.loadAlbum(album, cache)

            lstImages.adapter = ImagesAdapter(baseContext, album, cache)
            lstImages.setOnItemClickListener { adapterView, view, i, l ->
                slidingGallery.visibility = VISIBLE
                slidingGallery.openImage(i)
            }
            lstImages.setOnTouchListener(dateScroller)
            lstImages.setOnScrollChangeListener(dateScroller)

            if (savedInstanceState?.containsKey(LAST_POSITION) == true) {
                val position = savedInstanceState.getInt(LAST_POSITION)
                lstImages.post{
                    lstImages.setSelection(position)
                    slidingGallery.openImage(position)
                    if (savedInstanceState.getBoolean(GALLERY_OPEN, false))
                    {
                        slidingGallery.visibility = VISIBLE
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Unable to load album", e)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val lstImages = findViewById<GridView>(R.id.lstImages)
        val slidingGallery = findViewById<SlidingGallery>(R.id.slidingGallery)

        val galleryOpen = slidingGallery.visibility == VISIBLE

        outState.putBoolean(GALLERY_OPEN, galleryOpen)
        if (galleryOpen) {
            outState.putInt(LAST_POSITION, slidingGallery.currentImage())
        }
        else {
            outState.putInt(LAST_POSITION, lstImages.firstVisiblePosition)
        }
        super.onSaveInstanceState(outState)
    }
}
