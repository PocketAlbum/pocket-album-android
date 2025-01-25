package si.pocketalbum.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.widget.FrameLayout
import android.widget.GridView
import android.widget.LinearLayout
import android.widget.LinearLayout.VERTICAL
import android.widget.TextView
import si.pocketalbum.R
import si.pocketalbum.core.IAlbum
import kotlin.collections.set

class DateScroller(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    View.OnScrollChangeListener, View.OnTouchListener {

    private val frmRoot : FrameLayout
    private val lblDate : TextView
    private val lltTimeline : LinearLayout
    private var offsets : Map<Int, Int>? = null
    private var touch = false
    private var timeout = false

    init {
        inflate(context, R.layout.view_date_scroller, this)
        frmRoot = findViewById(R.id.frmRoot)
        lblDate = findViewById(R.id.lblDate)
        lltTimeline = findViewById(R.id.lltTimeline)
    }

    override fun onScrollChange(
        v: View?,
        scrollX: Int,
        scrollY: Int,
        oldScrollX: Int,
        oldScrollY: Int
    ) {
        if (offsets != null && v is GridView)
        {
            val i = (v.lastVisiblePosition + v.firstVisiblePosition) / 2
            val index = v.count - i - 1
            val position = v.firstVisiblePosition.toDouble() / v.count
            val availableHeight = height - lblDate.height
            lblDate.text = getYearFoIndex(index).toString()
            val layout = lblDate.layoutParams as LayoutParams
            layout.topMargin = (position * availableHeight).toInt()
            lblDate.layoutParams = layout

            if (visibility != VISIBLE) {
                visibility = VISIBLE
            }

            handler.removeCallbacks(hideScrollbar)
            timeout = false
            handler.postDelayed(hideScrollbar, 500)
        }
    }

    private val hideScrollbar = Runnable {
        if (touch) {
            timeout = true
        }
        else {
            visibility = INVISIBLE
        }
    }

    private fun getYearFoIndex(index : Int) : Int
    {
        val yearOffsets = offsets?.entries
        if (yearOffsets != null) {
            for (year in yearOffsets) {
                if (year.key > index) {
                    return year.value
                }
            }
        }
        return 0
    }

    fun setAlbum(album: IAlbum)
    {
        val newOffsets = mutableMapOf<Int, Int>()
        var cumulativeCount = 0

        for (year in album.getYearIndex().sortedBy { it.year }) {
            cumulativeCount += year.count
            newOffsets[cumulativeCount] = year.year
        }
        offsets = newOffsets
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (event?.action == ACTION_DOWN)
        {
            touch = true
        }
        else if (event?.action == ACTION_UP)
        {
            touch = false
            if (timeout) {
                timeout = false
                handler.postDelayed(hideScrollbar, 500)
            }
        }
        return false
    }
}