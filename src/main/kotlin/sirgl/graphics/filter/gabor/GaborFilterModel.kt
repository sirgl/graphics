package sirgl.graphics.filter.gabor

import sirgl.graphics.core.Filters
import sirgl.graphics.filter.*
import sirgl.graphics.observable.SimpleObservable
import sirgl.graphics.observable.map
import sirgl.graphics.observable.transmitTo
import java.lang.Math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin


class GaborFilterModel(private val presentable: Presentable, private val filters: Filters) : FilterModel, Presentable by presentable {
    private val kernelObservable = SimpleObservable(GaborInfo(1, 45f))
    override val filter = PerChanelMatrixFilter(kernelObservable.map {
        println("Gabor info changed $it")
        val radius = it?.radius ?: return@map null
        MatrixKernelInfo(generateGaborKernel(radius * 2 + 1, it.theta))
    })
    override val panel = GaborFilterPanel()
    init {
        panel.gaborInfoObservable.transmitTo(kernelObservable)
        kernelObservable.subscribe {
            filters.filterConfigurationChanged()
        }
    }
}


data class GaborInfo(val radius: Int, val theta: Float)

private fun convertToPolarCoordinates(x: Int, y: Int, theta: Float): Coordinate {
    val radian = Math.toRadians(theta.toDouble()).toFloat()
    val polarX = x * cos(radian) + y * sin(radian)
    val polarY = -x * sin(radian) + y * cos(radian)
    return Coordinate(polarX, polarY)
}

data class Coordinate(
        val x: Float,
        val y: Float
)

fun generateGaborKernel(size: Int, theta: Float, gamma: Float = 1.0f, lambda: Float = 2.0f): Matrix {
    val kernel = Matrix(size, size)
    val sigma = 0.56f * lambda

    for (x in 0 until size) {
        for (y in 0 until size) {
            val (polarX, polarY) = convertToPolarCoordinates(x - size / 2, y - size / 2, theta)
            val value = matrixElement(polarX, polarY, lambda, sigma, gamma)
            kernel.setXY(x, y, value)
        }
    }

    return kernel
}

private fun matrixElement(polarX: Float, polarY: Float, lambda: Float, sigma: Float, gamma: Float): Float {
    val m1 = exp(-(polarX * polarX + gamma * gamma * polarY * polarY) /
            (sigma * sigma * 2))
    val m2 = cos(2 * PI * polarX / lambda).toFloat()
    return m1 * m2
}
