package si.pocketalbum

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity

class StatisticsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.activity_statistics)
    }
}