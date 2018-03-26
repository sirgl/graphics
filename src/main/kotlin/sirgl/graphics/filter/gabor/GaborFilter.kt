package sirgl.graphics.filter.gabor

import sirgl.graphics.filter.*
import sirgl.graphics.observable.Observable
import java.awt.image.BufferedImage

class GaborFilter(kernelObservable: Observable<KernelInfo>) : KernelFilter(kernelObservable) {
    override fun transformRGB(x: Int, y: Int, img: BufferedImage, rgb: RGB) {
        val kernelData = kernelObservable.value ?: return
        val matrixKernelData = kernelData as MatrixKernelInfo
        val matrix = matrixKernelData.matrix
        val value = convolveChanelValue(matrix, img, x, y)
        rgb.r = value
        rgb.g = value
        rgb.b = value
    }
}