package si.pocketalbum.view.timeline

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.FrameLayout
import android.widget.GridView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import si.pocketalbum.R
import si.pocketalbum.core.AlbumConnection

class DateScroller(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    View.OnScrollChangeListener, View.OnTouchListener {

    private val frmRoot : FrameLayout
    private val lblDate : TextView
    private val timelineView : TimelineView
    private var offsets : List<YearOffset>? = null
    private var touch = false
    private var timeout = false
    private var mode = InteractionModes.HIDDEN
    private var connection: AlbumConnection? = null
    private var gridView: GridView? = null
    val btnWidth = resources.getDimensionPixelSize(R.dimen.date_button_width)

    enum class InteractionModes {
        HIDDEN, VISIBLE, SCROLLING
    }

    init {
        inflate(context, R.layout.view_date_scroller, this)
        frmRoot = findViewById(R.id.frmRoot)
        lblDate = findViewById(R.id.lblDate)
        timelineView = findViewById(R.id.timelineView)

        lblDate.setOnTouchListener(this)
        ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                        or WindowInsetsCompat.Type.displayCutout()
            )
            v.updatePadding(
                left = 0,
                top = bars.top,
                right = 0,
                bottom = 0,
            )
            WindowInsetsCompat.CONSUMED
        }
    }

    fun albumLoaded(connection: AlbumConnection, gridView: GridView) {
        this.connection = connection
        this.gridView = gridView
        loadAlbum()
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
            val availableHeight = height - paddingTop - paddingBottom - lblDate.height
            lblDate.text = getYearForIndex(index).toString()
            val layout = lblDate.layoutParams as LayoutParams
            layout.topMargin = (position * availableHeight).toInt()
            lblDate.layoutParams = layout

            if (mode == InteractionModes.HIDDEN)
            {
                mode = InteractionModes.VISIBLE
                animateLeftMargin(btnWidth * 3, btnWidth * 2)
            }
            hideWithTimeout()
        }
    }

    private fun hideWithTimeout() {
        handler.removeCallbacks(hideScrollbar)
        timeout = false
        handler.postDelayed(hideScrollbar, 500)
    }

    private val hideScrollbar = Runnable {
        if (touch) {
            timeout = true
        }
        else {
            animateLeftMargin(btnWidth * 2, btnWidth * 3)
            mode = InteractionModes.HIDDEN
        }
    }

    private fun animateLeftMargin(start : Int, end : Int) {
        lblDate.visibility = VISIBLE
        lblDate.clearAnimation()
        val animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                val leftMargin = start + ((end - start) * interpolatedTime).toInt()

                val params = lblDate.layoutParams as LayoutParams
                params.leftMargin = leftMargin
                lblDate.layoutParams = params

                val timelineParams = timelineView.layoutParams as LayoutParams
                timelineParams.leftMargin = leftMargin
                timelineView.layoutParams = timelineParams
            }
        }
        animation.duration = 200 // in ms
        lblDate.startAnimation(animation)
    }

    private fun getYearForIndex(index : Int) : Int
    {
        for (offset in offsets ?: listOf()) {
            if (offset.cumulativeCount > index) {
                return offset.year
            }
        }
        return 0
    }

    fun loadAlbum()
    {
        val newOffsets = mutableListOf<YearOffset>()
        var cumulativeCount = 0

        val cache = connection?.cache!!
        for (year in cache.info.years.sortedBy { it.year }) {
            cumulativeCount += year.count
            newOffsets.add(YearOffset(year.year, cumulativeCount))
        }
        offsets = newOffsets

        timelineView.offsets = newOffsets
        timelineView.refreshDrawableState()
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (event == null) {
            return false
        }

        if (event.action == ACTION_DOWN)
        {
            touch = true
        }
        else if (event.action == ACTION_UP)
        {
            touch = false
            if (timeout) {
                hideWithTimeout()
            }
        }

        if (v == lblDate) {
            if (event.action == ACTION_DOWN) {
                animateLeftMargin(btnWidth * 2, 0)
            }
            else if (event.action == ACTION_UP) {
                animateLeftMargin(0, btnWidth * 2)
                hideWithTimeout()
            }
            else if (event.action == ACTION_MOVE) {
                gridView?.let {
                    val locationArray = IntArray(2)
                    getLocationOnScreen(locationArray)
                    val availableHeight = height - paddingTop - paddingBottom - lblDate.height
                    val part = (event.rawY - locationArray[1] - paddingTop) / availableHeight
                    val position = (it.count * part).toInt()
                    Log.v("DateScroller", "Scroll to position: $position")
                    it.setSelection(position)
                }
            }

            return true
        }

        return false
    }

    data class YearOffset(val year: Int, val cumulativeCount: Int)
}