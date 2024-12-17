package si.kordez.pocketalbum

import android.os.Bundle
import android.view.View
import android.widget.GridView
import android.widget.ListView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val lstImages = findViewById<GridView>(R.id.lstImages)

        lstImages.adapter = ImagesAdapter(baseContext)
    }
}
