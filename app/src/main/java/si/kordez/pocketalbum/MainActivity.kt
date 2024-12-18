package si.kordez.pocketalbum

import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.GridView
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.viewpager2.widget.ViewPager2
import si.kordez.pocketalbum.core.ImageCache
import si.kordez.pocketalbum.core.sqlite.SQLiteAlbum

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val lstImages = findViewById<GridView>(R.id.lstImages)
        val vpgImages = findViewById<ViewPager2>(R.id.vpgImages)

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true)
        {
            override fun handleOnBackPressed() {
                if (vpgImages.visibility == VISIBLE)
                {
                    vpgImages.visibility = GONE
                }
                else finish()
            }
        })

        try {
            val album = SQLiteAlbum(baseContext)
            val cache = ImageCache(album)
            val info = album.getInfo()
            val averageSize = info.ImagesSize / info.ImageCount
            val averageThumbSize = info.ThumbnailsSize / info.ImageCount

            vpgImages.adapter = ImagesRecyclerAdapter(album, cache)

            lstImages.adapter = ImagesAdapter(baseContext, album, cache)
            lstImages.setOnItemClickListener { adapterView, view, i, l ->
                vpgImages.setCurrentItem(i, false)
                vpgImages.visibility = VISIBLE
            }
        } catch (e: Exception) {
            System.out.println()
        }
    }
}
