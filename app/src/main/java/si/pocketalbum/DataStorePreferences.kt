package si.pocketalbum

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferencesKeys {
    val DISPLAY_THEME = stringPreferencesKey("display_theme")
}

val Context.dataStore by preferencesDataStore(name = "settings")
