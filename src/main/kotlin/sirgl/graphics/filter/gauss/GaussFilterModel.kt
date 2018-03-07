package sirgl.graphics.filter.gauss

import sirgl.graphics.core.App
import sirgl.graphics.filter.*
import sirgl.graphics.observable.SimpleObservable
import sirgl.graphics.observable.map
import sirgl.graphics.observable.transmitTo

class GaussFilterModel(presentable: Presentable, app: App) : FilterModel, Presentable by presentable {
    private val gaussDataObservable = SimpleObservable(
            GaussData(KernelData(3, NormalizationType.Natural), 1f)
    )

    override val filter = PerChanelMatrixFilter(gaussDataObservable.map {
        val kernelData = it?.kernelData ?: return@map null
        MatrixKernelData(kernelData.dimension, NormalizationType.Natural, generateGaussMatrix(kernelData.dimension, it.sigma))
    })
    override val panel = GaussFilterPanel()
    init {
        panel.gaussDataObservable.subscribe {
            app.imageToDrawChanged()
        }
        panel.gaussDataObservable.map {
            it ?: return@map null
            MatrixKernelData(it.size, NormalizationType.Natural, generateGaussMatrix(it.size, it.sigma)) as KernelData
        }.transmitTo(filter.kernelDataObservable)
    }

}

class GaussData (
        val kernelData: KernelData,
        val sigma: Float
)

fun generateGaussMatrix(size: Int, sigma: Float): FloatArray {
    val matrix = FloatArray(size * size)
    val multiplier = 1f / (2 * Math.PI.toFloat() * sigma * sigma)
    for (y in (0  until size)) {
        for (x in (0 until size)) {
            matrix.setXY(x, y, size, multiplier * Math.exp(-(x * x + y * y) / (2 * sigma * sigma).toDouble()).toFloat())
        }
    }
    return matrix
}