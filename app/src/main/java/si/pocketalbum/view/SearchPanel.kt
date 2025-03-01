package si.pocketalbum.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.ImageButton
import si.pocketalbum.R
import si.pocketalbum.core.models.FilterModel
import java.util.function.Consumer

class SearchPanel (context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    init {
        inflate(context, R.layout.view_search_panel, this)

        findViewById<ImageButton>(R.id.btnClose).setOnClickListener {
            visibility = GONE
        }

        val partOfDay = findViewById<PartOfDay>(R.id.partOfDay)
        partOfDay.setOnChangeListener {
            filter = FilterModel(null, it)
            listener?.accept(filter)
        }
    }
    private var filter = FilterModel(null, null)
    private var listener: Consumer<FilterModel>? = null

    fun setOnSearchListener(l: Consumer<FilterModel>) {
        listener = l
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        return true
    }
}
