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
                val index = lstImages.adapter.count - i - 1
                slidingGallery.visibility = VISIBLE
                slidingGallery.openImage(index)
            }
            lstImages.setOnTouchListener(dateScroller)
            lstImages.setOnScrollChangeListener(dateScroller)
        } catch (e: Exception) {
            Log.e("MainActivity", "Unable to load album", e)
        }
    }
}
