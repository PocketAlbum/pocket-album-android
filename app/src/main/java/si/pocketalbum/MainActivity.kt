package si.pocketalbum

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.GridView
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import si.pocketalbum.core.AlbumConnection
import si.pocketalbum.services.AlbumService
import si.pocketalbum.view.AlbumsPanel
import si.pocketalbum.view.ImagesAdapter
import si.pocketalbum.view.SettingsPanel
import si.pocketalbum.view.SlidingGallery
import si.pocketalbum.view.search.SearchPanel
import si.pocketalbum.view.timeline.DateScroller

class MainActivity : FragmentActivity() {

    companion object {
        const val LAST_POSITION = "LAST_POSITION"
        const val GALLERY_OPEN = "GALLERY_OPEN"
    }

    private lateinit var albumService: AlbumService
    private var serviceBound: Boolean = false
    private var savedInstanceState: Bundle? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as AlbumService.LocalBinder
            albumService = binder.getService()
            serviceBound = true

            CoroutineScope(Job() + Dispatchers.IO).launch {
                try {
                    val con = albumService.getConnectionDeferred().await()
                    runOnUiThread {
                        albumLoaded(con)
                    }
                }
                catch (e: Exception) {
                    Log.e("MainActivity", "Failed to load album", e)
                    startActivity(Intent(baseContext, ImportActivity::class.java))
                }
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            serviceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.savedInstanceState = savedInstanceState
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val lstImages = findViewById<GridView>(R.id.lstImages)
        val slidingGallery = findViewById<SlidingGallery>(R.id.slidingGallery)

        lifecycleScope.launch {
            dataStore.data
                .map { prefs -> prefs[PreferencesKeys.THUMBNAIL_SIZE] ?: 80 }
                .collect { size -> lstImages.post {
                    val dm = resources.displayMetrics
                    val size = TypedValue.applyDimension(COMPLEX_UNIT_DIP, size.toFloat(), dm)
                    lstImages.numColumns = (lstImages.width / size).toInt()
                }}
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

        val lltActions = findViewById<LinearLayout>(R.id.lltActions)
        ViewCompat.setOnApplyWindowInsetsListener(lltActions) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                        or WindowInsetsCompat.Type.displayCutout()
            )
            v.updatePadding(
                left = bars.left,
                right = bars.right,
                bottom = bars.bottom,
            )
            WindowInsetsCompat.CONSUMED
        }

        val intent = Intent(this, AlbumService::class.java)
        startService(intent)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()

        unbindService(connection)
        serviceBound = false
    }

    fun albumLoaded(connection: AlbumConnection) {
        val lstImages = findViewById<GridView>(R.id.lstImages)
        val slidingGallery = findViewById<SlidingGallery>(R.id.slidingGallery)
        val dateScroller = findViewById<DateScroller>(R.id.dateScroller)

        dateScroller.albumLoaded(connection, lstImages)
        slidingGallery.albumLoaded(connection, albumService)

        val adapter = ImagesAdapter(baseContext, connection)
        lstImages.adapter = adapter
        lstImages.setOnItemClickListener { adapterView, view, i, l ->
            slidingGallery.openImage(i)
        }
        lstImages.setOnTouchListener(dateScroller)
        lstImages.setOnScrollChangeListener(dateScroller)

        restoreSavedState(lstImages, slidingGallery)

        val pnlAlbum = findViewById<AlbumsPanel>(R.id.pnlAlbums)
        val pnlSearch = findViewById<SearchPanel>(R.id.pnlSearch)
        val pnlSettings = findViewById<SettingsPanel>(R.id.pnlSettings)

        pnlSettings.passLifecycleOwner(this)
        pnlSearch.setOnSearchListener {
            albumService.changeFilter(it)
            adapter.notifyDataSetChanged()
            dateScroller.loadAlbum()
            slidingGallery.loadAlbum()
        }
        pnlSearch.albumLoaded(albumService.getHeatmapCache())
        pnlAlbum.showInfo(connection)

        findViewById<Button>(R.id.btnAlbum).setOnClickListener {
            if (pnlAlbum.visibility == VISIBLE) {
                pnlAlbum.visibility = GONE
            }
            else {
                pnlAlbum.visibility = VISIBLE
                pnlSearch.visibility = GONE
                pnlSettings.visibility = GONE
            }
        }

        findViewById<Button>(R.id.btnSearch).setOnClickListener {
            if (pnlSearch.visibility == VISIBLE) {
                pnlSearch.visibility = GONE
            }
            else {
                pnlAlbum.visibility = GONE
                pnlSearch.visibility = VISIBLE
                pnlSettings.visibility = GONE
            }
        }

        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            if (pnlSettings.visibility == VISIBLE) {
                pnlSettings.visibility = GONE
            }
            else {
                pnlAlbum.visibility = GONE
                pnlSearch.visibility = GONE
                pnlSettings.visibility = VISIBLE
            }
        }
    }

    private fun restoreSavedState(lstImages: GridView, slidingGallery: SlidingGallery) {
        val state = savedInstanceState
        if (state?.containsKey(LAST_POSITION) == true) {
            val position = state.getInt(LAST_POSITION)
            lstImages.post{
                lstImages.setSelection(position)
                if (state.getBoolean(GALLERY_OPEN, false))
                {
                    slidingGallery.openImage(position)
                }
            }
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
