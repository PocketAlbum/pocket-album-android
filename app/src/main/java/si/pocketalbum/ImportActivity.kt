package si.pocketalbum

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import si.pocketalbum.core.sqlite.SQLiteAlbum
import si.pocketalbum.services.AlbumService
import si.pocketalbum.view.AlbumView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ImportActivity : ComponentActivity() {
    private lateinit var getContent: ActivityResultLauncher<String>
    private lateinit var prgImport: ProgressBar
    private lateinit var albumView: AlbumView
    private lateinit var btnImport: Button
    private var savedInstanceState: Bundle? = null
    private var albumService: AlbumService? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as AlbumService.LocalBinder
            albumService = binder.getService()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            albumService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.savedInstanceState = savedInstanceState
        setContentView(R.layout.activity_import)

        val intent = Intent(this, AlbumService::class.java)
        startService(intent)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)

        prgImport = findViewById(R.id.prgImport)
        albumView = findViewById(R.id.albumView)
        btnImport = findViewById(R.id.btnImport)
        val lblError: TextView = findViewById(R.id.lblError)

        val btnSelectFile: Button = findViewById(R.id.btnSelectFile)
        btnSelectFile.setOnClickListener {
            getContent.launch("application/*")
        }

        getContent = registerForActivityResult(GetContent()) { uri: Uri? ->
            try {
                lblError.visibility = View.GONE
                SQLiteAlbum.verifyOrThrow(this, uri!!)
                val nameAndSize = UriUtils.getDisplayNameSize(this, uri)
                albumView.showInfo(nameAndSize)
                btnImport.isEnabled = true
                btnImport.setOnClickListener {
                    btnImport.isEnabled = false
                    btnImport.text = getString(R.string.importing)
                    btnSelectFile.visibility = View.GONE
                    importAlbum(uri, nameAndSize.second)
                }
            }
            catch (e: Exception) {
                Log.e("ImportActivity", "Error opening sqlite file", e)
                if (e is UserException) {
                    lblError.text = e.getMessage(baseContext)
                }
                else {
                    lblError.text = baseContext.getString(R.string.unknown_error)
                }
                lblError.visibility = View.VISIBLE
                albumView.clearInfo()
                btnImport.isEnabled = false
            }
        }
    }

    private fun importAlbum(uri: Uri, size: Long) {
        try {
            prgImport.max = (size / 1024).toInt()
            prgImport.progress = 0
            CoroutineScope(Job() + Dispatchers.IO).launch {
                copyUriToFile(baseContext, uri)
                runOnUiThread {
                    albumService!!.loadAlbumAsync()
                    finish()
                }
            }
        }
        catch (e: Exception) {
            Log.e("ImportActivity", "Unable to import album", e)
        }
    }

    private fun copyUriToFile(context: Context, uri: Uri) {
        context.contentResolver.openInputStream(uri)?.use {
            val destinationFile = File(context.filesDir, "album.sqlite")

            FileOutputStream(destinationFile).use { outputStream ->
                val buffer = ByteArray(1024)
                var bytesRead: Int
                while (it.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    runOnUiThread {
                        prgImport.progress++
                    }
                }
                outputStream.flush()
            }
        }
        throw IOException("Unable to open file from $uri")
    }
}