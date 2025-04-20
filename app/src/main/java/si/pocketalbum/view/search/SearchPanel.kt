package si.pocketalbum.view.search

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.ImageButton
import si.pocketalbum.R
import si.pocketalbum.core.HeatmapCache
import si.pocketalbum.core.models.FilterModel
import java.util.function.Consumer

class SearchPanel (context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    private val locationMap: LocationsMap

    init {
        inflate(context, R.layout.view_search_panel, this)

        findViewById<ImageButton>(R.id.btnClose).setOnClickListener {
            visibility = GONE
        }

        val partOfDay = findViewById<PartOfDay>(R.id.partOfDay)
        partOfDay.setOnChangeListener {
            filter = FilterModel(filter.year, it, filter.location)
            listener?.accept(filter)
        }

        locationMap = findViewById(R.id.locationsMap)
        locationMap.setOnChangeListener {
            filter = FilterModel(filter.year, filter.timeOfDay, it)
            listener?.accept(filter)
        }

    }
    private var filter = FilterModel(null, null, null)
    private var listener: Consumer<FilterModel>? = null

    fun setOnSearchListener(l: Consumer<FilterModel>) {
        listener = l
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        return true
    }

    fun albumLoaded(heatmap: HeatmapCache) {
        locationMap.showHeatmap(heatmap)
    }
}
