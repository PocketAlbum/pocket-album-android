package si.kordez.pocketalbum.view

import MatrixAnimation
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.widget.ImageView
import androidx.viewpager2.widget.ViewPager2

class ImageControl(context: Context, attrs: AttributeSet?) : ImageView(context, attrs),
    OnScaleGestureListener{

    val gestureListener = ScaleGestureDetector(context, this)
    val m = Matrix()

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureListener.onTouchEvent(event)
        return true
    }

    override fun onDraw(canvas: Canvas) {
        if (animation == null || animation.hasEnded()) {
            canvas.setMatrix(m)
        }
        super.onDraw(canvas)
    }

    fun resetTransform() {
        m.reset()
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        m.postTranslate(-detector.focusX, -detector.focusY)
        m.postScale(detector.scaleFactor, detector.scaleFactor)
        m.postTranslate(detector.focusX, detector.focusY)

        invalidate()
        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        val p3 = parent?.parent?.parent
        if (p3 is ViewPager2)
        {
            p3.isUserInputEnabled = false
        }
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {

        val scale = getScale(m)
        if (scale < 1.1f) {
            val animator = MatrixAnimation(m, Matrix())
            animator.duration = 500
            startAnimation(animator)
            resetTransform()

            val p3 = parent?.parent?.parent
            if (p3 is ViewPager2)
            {
                postDelayed({
                    p3.isUserInputEnabled = true
                }, 200)
            }
        }
    }

    fun getScale(m: Matrix): Float {
        val v = FloatArray(9)
        m.getValues(v)
        return v[0]
    }
}