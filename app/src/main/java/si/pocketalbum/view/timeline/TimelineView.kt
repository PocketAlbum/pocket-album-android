package si.pocketalbum.view.timeline

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.view.View
import si.pocketalbum.R
import si.pocketalbum.view.timeline.DateScroller.YearOffset

class TimelineView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    var offsets: List<YearOffset>? = null

    private val linePrimary = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        strokeWidth = resources.getDimensionPixelSize(R.dimen.timeline_line_width).toFloat()
        textSize = resources.getDimensionPixelSize(R.dimen.timeline_text_size).toFloat()
    }

    private val textWidth = linePrimary.measureText("9999")

    override fun onDraw(canvas: Canvas) {
        if (offsets != null) {
            val h = height.toFloat()
            val w = width.toFloat()
            val startX = (width / 2f) - (textWidth / 2f)
            val reversed = offsets!!.reversed()

            var total : Int? = null
            var previousYear = 0
            for (offset in reversed) {
                if (total == null) {
                    total = offset.cumulativeCount
                    previousYear = offset.year
                    continue
                }
                val rel = offset.cumulativeCount.toFloat() / total
                val abs = h - (rel * h)

                canvas.drawLine(0f, abs, w, abs, linePrimary)
                canvas.drawText(previousYear.toString(), startX, abs - 5f, linePrimary)

                previousYear = offset.year
            }
        }

        super.onDraw(canvas)
    }
}