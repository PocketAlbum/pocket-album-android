package si.kordez.pocketalbum

import android.os.Bundle
import android.util.Log
import android.widget.GridView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import si.kordez.pocketalbum.core.sqlite.SQLiteAlbum

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val lstImages = findViewById<GridView>(R.id.lstImages)

        try {
            val album = SQLiteAlbum(baseContext)
            val info = album.getInfo()
            val averageSize = info.ImagesSize / info.ImageCount
            val averageThumbSize = info.ThumbnailsSize / info.ImageCount

            lstImages.adapter = ImagesAdapter(baseContext, album)
        } catch (e: Exception) {
            System.out.println()
        }
    }
}
