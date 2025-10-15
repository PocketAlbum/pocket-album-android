package si.pocketalbum.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.SeekBar
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import si.pocketalbum.PreferencesKeys
import si.pocketalbum.R
import si.pocketalbum.dataStore
import si.pocketalbum.view.settings.DisplayTheme

class SettingsPanel (context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    val displayTheme: DisplayTheme
    val skbThumbnailSize: SeekBar

    init {
        inflate(context, R.layout.view_panel_settings, this)

        findViewById<ImageButton>(R.id.btnClose).setOnClickListener {
            visibility = GONE
        }
        displayTheme = findViewById(R.id.displayTheme)
        skbThumbnailSize = findViewById(R.id.skbThumbnailSize)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        return true
    }

    fun passLifecycleOwner(owner: LifecycleOwner) {
        displayTheme.passLifecycleOwner(owner)

        skbThumbnailSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener
        {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                owner.lifecycleScope.launch {
                    changeThumbnailSize(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) { }

            override fun onStopTrackingTouch(seekBar: SeekBar?) { }
        })
    }

    suspend fun changeThumbnailSize(progress: Int) {
        withContext(Dispatchers.IO) {
            context.dataStore.edit { prefs ->
                prefs[PreferencesKeys.THUMBNAIL_SIZE] = progress
            }
        }
    }
}