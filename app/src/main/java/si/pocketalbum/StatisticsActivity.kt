package si.pocketalbum

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View.TEXT_ALIGNMENT_TEXT_END
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import si.pocketalbum.core.AlbumConnection
import si.pocketalbum.core.models.FilterModel
import si.pocketalbum.core.models.Interval
import si.pocketalbum.core.models.YearIndex
import si.pocketalbum.services.AlbumService
import si.pocketalbum.view.CurrentAlbum
import si.pocketalbum.view.formatSize

class StatisticsActivity : ComponentActivity() {

    private lateinit var albumService: AlbumService
    private var serviceBound: Boolean = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as AlbumService.LocalBinder
            albumService = binder.getService()
            serviceBound = true

            lifecycleScope.launch {
                albumService.openedAlbum.collect { connection ->
                    if (connection != null) {
                        openAlbum(connection)
                    }
                }
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            serviceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        val imgTopInset = findViewById<ImageView>(R.id.imgTopInset)
        ViewCompat.setOnApplyWindowInsetsListener(imgTopInset) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                        or WindowInsetsCompat.Type.displayCutout()
            )
            v.layoutParams.height = bars.top;
            WindowInsetsCompat.CONSUMED
        }

        val intent = Intent(this, AlbumService::class.java)
        startService(intent)
        bindService(intent, connection, BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()

        unbindService(connection)
        serviceBound = false
    }

    private fun openAlbum(connection: Deferred<AlbumConnection>)
    {
        val currentAlbum = findViewById<CurrentAlbum>(R.id.currentAlbum)
        val table = findViewById<TableLayout>(R.id.tableStatistics)

        CoroutineScope(Job() + Dispatchers.IO).launch {
            try {
                val con = connection.await()
                val yearIndex = con.album.getYearIndex()
                val meta = con.album.getMetadata()
                runOnUiThread {
                    currentAlbum.showInfo(yearIndex, meta)
                }
                showTable(table, con, yearIndex)
            }
            catch (e: Exception) {
                Log.e("MainActivity", "Failed to load album", e)
                startActivity(Intent(baseContext, ImportActivity::class.java))
            }
        }
    }

    suspend fun showTable(
        layout: TableLayout,
        connection: AlbumConnection,
        yearIndex: List<YearIndex>
    ) {
        for (y in yearIndex)
        {
            val filter = FilterModel(Interval(y.year.toLong()), null, null)
            val info = connection.album.getInfo(filter)

            val row = TableRow(baseContext)

            val columns = listOf(
                y.year.toString(),
                y.count.toString(),
                formatSize(y.size.toLong(), 1),
                formatSize(info.imagesSize, 1),
                formatSize(info.thumbnailsSize, 1)
            )

            columns.forEach { txt ->
                row.addView(TextView(baseContext).apply {
                    text = txt
                    textAlignment = TEXT_ALIGNMENT_TEXT_END
                })
            }

            val v = TextView(baseContext)
            v.textAlignment = TEXT_ALIGNMENT_TEXT_END
            runOnUiThread {
                layout.addView(row)
            }
        }
    }
}