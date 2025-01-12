package si.kordez.pocketalbum

import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.GridView
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import si.kordez.pocketalbum.core.ImageCache
import si.kordez.pocketalbum.core.sqlite.SQLiteAlbum
import si.kordez.pocketalbum.view.ImagesAdapter
import si.kordez.pocketalbum.view.SlidingGallery

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val lstImages = findViewById<GridView>(R.id.lstImages)
        val slidingGallery = findViewById<SlidingGallery>(R.id.slidingGallery)

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

            slidingGallery?.loadAlbum(album, cache)

            lstImages.adapter = ImagesAdapter(baseContext, album, cache)
            lstImages.setOnItemClickListener { adapterView, view, i, l ->
                slidingGallery.visibility = VISIBLE
                slidingGallery.openImage(i)
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Unable to load album", e)
        }
    }
}
