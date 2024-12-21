package si.kordez.pocketalbum.view

import MatrixAnimation
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.OnGestureListener
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.widget.ImageView
import androidx.viewpager2.widget.ViewPager2

class ImageControl(context: Context, attrs: AttributeSet?) : ImageView(context, attrs),
    OnScaleGestureListener, OnGestureListener {

    private val scaleDetector = ScaleGestureDetector(context, this)
    private val tapDetector = GestureDetector(context, this)
    private val transformMatrix = Matrix()
    private var scaling = false

    override fun onTouchEvent(event: MotionEvent): Boolean {
        tapDetector.onTouchEvent(event)
        scaleDetector.onTouchEvent(event)
        return true
    }

    override fun onDraw(canvas: Canvas) {
        if (animation == null || animation.hasEnded()) {
            canvas.setMatrix(transformMatrix)
        }
        super.onDraw(canvas)
    }

    fun resetTransform() {
        transformMatrix.reset()
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        transformMatrix.postTranslate(-detector.focusX, -detector.focusY)
        transformMatrix.postScale(detector.scaleFactor, detector.scaleFactor)
        transformMatrix.postTranslate(detector.focusX, detector.focusY)

        invalidate()
        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        scaling = true
        val p3 = parent?.parent?.parent
        if (p3 is ViewPager2)
        {
            p3.isUserInputEnabled = false
        }
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
        scaling = false
        val scale = getScale(transformMatrix)
        if (scale < 1.1f) {
            val animator = MatrixAnimation(transformMatrix, Matrix())
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

    override fun onDown(e: MotionEvent): Boolean {
        return true
    }

    override fun onShowPress(e: MotionEvent) {

    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return true
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        if (!scaling && getScale(transformMatrix) > 1.1) {
            transformMatrix.postTranslate(-distanceX, -distanceY)
            checkTranslation(transformMatrix)
            invalidate()
        }
        return true
    }

    override fun onLongPress(e: MotionEvent) {

    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        
        return true
    }
}