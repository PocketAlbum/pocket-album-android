package si.pocketalbum.view.settings

import android.app.UiModeManager
import android.app.UiModeManager.MODE_NIGHT_AUTO
import android.app.UiModeManager.MODE_NIGHT_NO
import android.app.UiModeManager.MODE_NIGHT_YES
import android.content.Context
import android.content.Context.UI_MODE_SERVICE
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import si.pocketalbum.PreferencesKeys
import si.pocketalbum.R
import si.pocketalbum.dataStore

class DisplayTheme (context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    enum class DisplayThemes {
        AUTO,
        LIGHT,
        DARK
    }

    private val btnAuto: ImageButton
    private val btnLight: ImageButton
    private val btnDark: ImageButton
    private val uiManager = context.getSystemService(UI_MODE_SERVICE) as UiModeManager

    init {
        inflate(context, R.layout.view_display_theme, this)

        btnAuto = findViewById(R.id.btnAuto)
        btnLight = findViewById(R.id.btnLight)
        btnDark = findViewById(R.id.btnDark)
    }

    private fun getClicked(v: View?): DisplayThemes {
        return when (v?.id) {
            R.id.btnLight -> { DisplayThemes.LIGHT }
            R.id.btnDark -> { DisplayThemes.DARK }
            else -> DisplayThemes.AUTO
        }
    }

    private fun bg(selected: Boolean) : Int {
        return if (selected) {
            R.drawable.background_red
        } else {
            R.drawable.background_primary
        }
    }

    suspend fun saveDisplayTheme(mode: DisplayThemes) {
        withContext(Dispatchers.IO) {
            context.dataStore.edit { prefs ->
                prefs[PreferencesKeys.DISPLAY_THEME] = mode.name
            }
        }
    }

    fun passLifecycleOwner(owner: LifecycleOwner) {
        owner.lifecycleScope.launch {
            displayThemeFlow.collect { mode ->
                btnAuto.setBackgroundResource(bg(mode == DisplayThemes.AUTO))
                btnLight.setBackgroundResource(bg(mode == DisplayThemes.LIGHT))
                btnDark.setBackgroundResource(bg(mode == DisplayThemes.DARK))

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (mode == DisplayThemes.AUTO) {
                        uiManager.setApplicationNightMode(MODE_NIGHT_AUTO)
                    }
                    else if (mode == DisplayThemes.DARK) {
                        uiManager.setApplicationNightMode(MODE_NIGHT_YES)
                    }
                    else if (mode == DisplayThemes.LIGHT) {
                        uiManager.setApplicationNightMode(MODE_NIGHT_NO)
                    }
                }
            }

        }
        val listener = OnClickListener { v ->
            owner.lifecycleScope.launch {
                saveDisplayTheme(getClicked(v))
            }
        }

        btnLight.setOnClickListener(listener)
        btnAuto.setOnClickListener(listener)
        btnDark.setOnClickListener(listener)
    }

    val displayThemeFlow: Flow<DisplayThemes> = context.dataStore.data.map { prefs ->
        val name = prefs[PreferencesKeys.DISPLAY_THEME]
        DisplayThemes.entries.find { it.name == name } ?: DisplayThemes.AUTO
    }
}