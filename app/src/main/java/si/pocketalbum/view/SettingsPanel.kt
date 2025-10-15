package si.pocketalbum.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.lifecycle.LifecycleOwner
import si.pocketalbum.R
import si.pocketalbum.view.settings.DisplayTheme

class SettingsPanel (context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    val displayTheme: DisplayTheme

    init {
        inflate(context, R.layout.view_panel_settings, this)

        findViewById<ImageButton>(R.id.btnClose).setOnClickListener {
            visibility = GONE
        }
        displayTheme = findViewById(R.id.displayTheme)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        return true
    }


    fun passLifecycleOwner(owner: LifecycleOwner) {
        displayTheme.passLifecycleOwner(owner)
    }
}