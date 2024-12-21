package si.kordez.pocketalbum.view

import MatrixAnimation
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.OnDoubleTapListener
import android.view.GestureDetector.OnGestureListener
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.widget.ImageView
import androidx.viewpager2.widget.ViewPager2

class ImageControl(context: Context, attrs: AttributeSet?) : ImageView(context, attrs),
    OnScaleGestureListener, OnGestureListener, OnDoubleTapListener {

    private val scaleDetector = ScaleGestureDetector(context, this)
    private val tapDetector = GestureDetector(context, this)
    private val transformMatrix = Matrix()

    override fun onTouchEvent(event: MotionEvent): Boolean {
        tapDetector.onTouchEvent(event)
        scaleDetector.onTouchEvent(event)
        return true
    }

    override fun onDraw(canvas: Canvas) {
        canvas.setMatrix(transformMatrix)
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
        lockSwiping()
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
        val scale = getScale(transformMatrix)
        if (scale < 1.1f) {
            val animator = MatrixAnimation(transformMatrix, Matrix(), this)
            animator.duration = 400
            startAnimation(animator)
            unlockSwiping()
        }
    }

    private fun getScale(m: Matrix): Float {
        val v = FloatArray(9)
        m.getValues(v)
        return v[0]
    }

    fun checkTranslation(m: Matrix) {
        val v = FloatArray(9)
        m.getValues(v)
        Log.i("Image", "Translation is ${v[Matrix.MTRANS_X]} ${v[Matrix.MTRANS_Y]}")
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
        if (getScale(transformMatrix) > 1.1) {
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

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        return true
    }

    override fun onDoubleTap(e: MotionEvent): Boolean {
        val destination = Matrix()
        if (getScale(transformMatrix) > 1.1) {
            unlockSwiping()
        }
        else {
            lockSwiping()
            destination.postTranslate(-e.x, -e.y)
            destination.postScale(2f, 2f)
            destination.postTranslate(e.x, e.y)
        }
        val animator = MatrixAnimation(transformMatrix, destination, this)
        animator.duration = 400
        startAnimation(animator)
        return true
    }

    override fun onDoubleTapEvent(e: MotionEvent): Boolean {
        return true
    }

    private fun lockSwiping() {
        val p3 = parent?.parent?.parent
        if (p3 is ViewPager2)
        {
            p3.isUserInputEnabled = false
        }
    }

    private fun unlockSwiping() {
        val p3 = parent?.parent?.parent
        if (p3 is ViewPager2)
        {
            postDelayed({
                p3.isUserInputEnabled = true
            }, 200)
        }
    }
}