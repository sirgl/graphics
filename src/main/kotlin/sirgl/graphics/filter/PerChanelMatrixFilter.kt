package sirgl.graphics.filter

import sirgl.graphics.observable.Observable
import java.awt.image.BufferedImage

class PerChanelMatrixFilter(
        val kernelDataObservable: Observable<KernelData>
) : MatrixFilter(kernelDataObservable) {

    override fun transformRGB(x: Int, y: Int, img: BufferedImage, rgb: RGB) {
        val kernelData = kernelDataObservable.value ?: return
        val matrixKernelData = kernelData as MatrixKernelData
        val matrix = matrixKernelData.matrix
        rgb.r = convolveChanel(matrix, kernelData.dimension, img, x, y, ChanelType.R)
        rgb.g = convolveChanel(matrix, kernelData.dimension, img, x, y, ChanelType.G)
        rgb.b = convolveChanel(matrix, kernelData.dimension, img, x, y, ChanelType.B)
    }

}