package sirgl.graphics.filter

import sirgl.graphics.observable.Observable
import java.awt.image.BufferedImage

open class PerChanelMatrixFilter(
        override val kernelObservable: Observable<KernelInfo>
) : KernelFilter(kernelObservable) {

    override fun transformRGB(x: Int, y: Int, img: BufferedImage, rgb: RGB) {
        val kernelData = kernelObservable.value ?: return
        val matrixKernelData = kernelData as MatrixKernelInfo
        val matrix = matrixKernelData.matrix
        rgb.r = convolveChanel(matrix, img, x, y, ChanelType.R)
        rgb.g = convolveChanel(matrix, img, x, y, ChanelType.G)
        rgb.b = convolveChanel(matrix, img, x, y, ChanelType.B)
    }
}