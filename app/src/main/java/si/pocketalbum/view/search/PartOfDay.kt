package si.pocketalbum.view.search

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import si.pocketalbum.R
import si.pocketalbum.core.models.FilterModel.TimesOfDay
import java.util.function.Consumer

class PartOfDay (context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    View.OnClickListener {

    private val btnMorning: ImageButton
    private val btnDay: ImageButton
    private val btnEvening: ImageButton
    private val btnNight: ImageButton
    private var listener: Consumer<TimesOfDay?>? = null
    private var selection: TimesOfDay? = null

    init {
        inflate(context, R.layout.view_part_of_day, this)

        btnMorning = findViewById(R.id.btnMorning)
        btnMorning.setOnClickListener(this)
        btnDay = findViewById(R.id.btnDay)
        btnDay.setOnClickListener(this)
        btnEvening = findViewById(R.id.btnEvening)
        btnEvening.setOnClickListener(this)
        btnNight = findViewById(R.id.btnNight)
        btnNight.setOnClickListener(this)
    }

    fun setOnChangeListener(listener: Consumer<TimesOfDay?>) {
        this.listener = listener
    }

    override fun onClick(v: View?) {
        val clicked = getClicked(v)
        selection = if (clicked == selection) { null } else { clicked }

        btnMorning.setBackgroundResource(bg(selection == TimesOfDay.Morning))
        btnDay.setBackgroundResource(bg(selection == TimesOfDay.Day))
        btnEvening.setBackgroundResource(bg(selection == TimesOfDay.Evening))
        btnNight.setBackgroundResource(bg(selection == TimesOfDay.Night))

        listener?.accept(selection)
    }

    private fun getClicked(v: View?): TimesOfDay? {
        return when (v?.id) {
            R.id.btnMorning -> { TimesOfDay.Morning }
            R.id.btnDay -> { TimesOfDay.Day }
            R.id.btnEvening -> { TimesOfDay.Evening }
            R.id.btnNight -> { TimesOfDay.Night }
            else -> null
        }
    }

    private fun bg(selected: Boolean) : Int {
        return if (selected) {
            R.drawable.background_red
        } else {
            R.drawable.background_white
        }
    }
}