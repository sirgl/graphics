package sirgl.graphics.filter.gauss

import sirgl.graphics.core.FFilters
import sirgl.graphics.filter.*
import sirgl.graphics.observable.SimpleObservable
import sirgl.graphics.observable.map
import sirgl.graphics.observable.transmitTo
import java.lang.Math.*

class GaussFilterModel(presentable: Presentable, filters: FFilters) : FilterModel, Presentable by presentable {
    private val gaussDataObservable = SimpleObservable(
            GaussData(KernelInfo(3), 1f)
    )

    override val filter = PerChanelMatrixFilter(gaussDataObservable.map {
        val kernelData = it?.kernelInfo ?: return@map null
        MatrixKernelInfo(generateGaussMatrix(kernelData.dimension, it.sigma))
    })
    override val panel = GaussFilterPanel()

    init {
        panel.gaussDataObservable.subscribe {
            filters.filterConfigurationChanged()
        }
        panel.gaussDataObservable.map {
            it ?: return@map null
            println("change")
            MatrixKernelInfo(generateGaussMatrix(it.size, it.sigma)) as KernelInfo
        }.transmitTo(filter.kernelObservable)
    }

}

class GaussData(
        val kernelInfo: KernelInfo,
        val sigma: Float
)

fun generateGaussMatrix(size: Int, sigma: Float): Matrix {
    val kernel = Matrix(size, size)
    val mean = size / 2.0
    var sum = 0.0f // For accumulating the kernel values
    for (x in (0 until size)) {
        for (y in (0 until size)) {
            val value = (exp(-0.5 * (pow((x - mean) / sigma, 2.0) + pow((y - mean) / sigma, 2.0))) / (2 * PI * sigma * sigma)).toFloat()
            kernel.setXY(x, y, value)

            // Accumulate the kernel values
            sum += value
        }
    }

    for (x in (0 until size)) {
        for (y in (0 until size)) {
            kernel.setXY(x, y, kernel.getXY(x, y) / sum)
        }
    }
    return kernel
}