import android.graphics.Matrix
import android.graphics.PointF
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import kotlin.math.min

class MatrixAnimation(
    private val startMatrix: Matrix,
    endMatrix: Matrix,
    private val view : View
) : Animation() {
    private val scaleStart: PointF
    private val scaleEnd: PointF
    private val translateStart: PointF
    private val translateEnd: PointF

    init {
        val a = FloatArray(9)
        val b = FloatArray(9)

        startMatrix.getValues(a)
        endMatrix.getValues(b)

        scaleStart = PointF(
            a[Matrix.MSCALE_X],
            a[Matrix.MSCALE_Y]
        )
        scaleEnd = PointF(b[Matrix.MSCALE_X], b[Matrix.MSCALE_Y])
        translateStart = PointF(
            a[Matrix.MTRANS_X],
            a[Matrix.MTRANS_Y]
        )
        translateEnd = PointF(
            b[Matrix.MTRANS_X],
            b[Matrix.MTRANS_Y]
        )

        fillAfter = true
    }

    override fun getTransformation(currentTime: Long, outTransformation: Transformation?): Boolean {
        val r = super.getTransformation(currentTime, outTransformation)
        val t = min((currentTime - startTime) / duration.toFloat(), 1f)
        Log.i("Animation", "Get Transformation more: ${r} (time: ${currentTime}) t: ${t}")
        val matrix = startMatrix
        val sFactor = PointF(
            scaleEnd.x * t / scaleStart.x + 1 - t,
            scaleEnd.y * t / scaleStart.y + 1 - t
        )
        val tFactor = PointF(
            (translateEnd.x - translateStart.x) * t,
            (translateEnd.y - translateStart.y) * t
        )
        matrix.reset()
        matrix.postScale(scaleStart.x, scaleStart.y, 0f, 0f)
        matrix.postScale(sFactor.x, sFactor.y, 0f, 0f)
        matrix.postTranslate(translateStart.x, translateStart.y)
        matrix.postTranslate(tFactor.x, tFactor.y)
        view.invalidate()
        return r
    }

    override fun getTransformation(
        currentTime: Long,
        outTransformation: Transformation?,
        scale: Float
    ): Boolean {
        return getTransformation(currentTime, outTransformation)
    }

    override fun start() {
        duration = 300
        super.start()
    }
}