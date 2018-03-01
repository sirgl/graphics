package sirgl.graphics.filter

import sirgl.graphics.observable.Observable
import java.awt.image.BufferedImage

// TODO add cache invalidation event observer
abstract class MatrixFilter(private val kernelDimensionObservable: Observable<Int>) : ImageFilter {
    private var internalImage = BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)

    override fun transform(src: BufferedImage, res: BufferedImage): Boolean {
        val srcHeight = src.height
        val srcWidth = src.width
        val dimension = kernelDimensionObservable.value ?: return false
        val edgeExtension = dimension - 1
        val extendedHeight = srcHeight + edgeExtension
        val extendedWidth = srcWidth + edgeExtension
        val singleEdgeExt = edgeExtension / 2
        if (extendedHeight != internalImage.height || extendedWidth != internalImage.width) { // TODO also check
            internalImage = BufferedImage(extendedWidth, extendedHeight, BufferedImage.TYPE_INT_RGB)
        }
        fillInternalImage(extendedWidth, extendedHeight, singleEdgeExt, srcWidth, srcHeight, src)
        for (y in (0 until srcHeight)) {
            for (x in (0 until srcWidth)) {
                transformRGB(x, y, internalImage)
            }
        }
        copy(singleEdgeExt + srcWidth - 1, singleEdgeExt + srcHeight - 1, internalImage, res, singleEdgeExt)
        return true
    }


    private fun copy(xEnd: Int, yEnd: Int, from: BufferedImage, to: BufferedImage, edgeExtesion: Int) {
        for (y in (edgeExtesion until yEnd)) {
            for (x in (edgeExtesion until xEnd)) {
                to.setRGB(x - edgeExtesion, y - edgeExtesion, from.getRGB(x, y))
            }
        }
    }

    /**
     * Here you can be sure, that size of [img] will be enough to handle edge pixels with kernel
     */
    abstract fun transformRGB(x: Int, y: Int, img: BufferedImage): Int

    private fun fillInternalImage(
            extendedWidth: Int,
            extendedHeight: Int,
            singleEdgeExt: Int,
            srcWidth: Int,
            srcHeight: Int,
            src: BufferedImage
    ) {
        for (y in (0 until extendedHeight)) {
            for (x in (0 until extendedWidth)) {
                val inLeftEdge = x in (0 until singleEdgeExt)
                val inRightEdge = x in (extendedWidth - singleEdgeExt until extendedWidth)
                val inUpEdge = y in (0 until singleEdgeExt)
                val inDownEdge = y in (extendedHeight - singleEdgeExt until extendedHeight)
                var srcX: Int
                var srcY: Int
                when {
                    inLeftEdge && inUpEdge -> {
                        srcX = 0
                        srcY = 0
                    }
                    inRightEdge && inUpEdge -> {
                        srcX = srcWidth - 1
                        srcY = 0
                    }
                    inRightEdge && inDownEdge -> {
                        srcX = srcWidth - 1
                        srcY = srcHeight - 1
                    }
                    inLeftEdge && inDownEdge -> {
                        srcX = 0
                        srcY = srcHeight - 1
                    }
                    inLeftEdge -> {
                        srcX = 0
                        srcY = y - singleEdgeExt
                    }
                    inUpEdge -> {
                        srcX = x - singleEdgeExt
                        srcY = 0
                    }
                    inDownEdge -> {
                        srcX = x - singleEdgeExt
                        srcY = srcHeight - 1
                    }
                    inRightEdge -> {
                        srcX = srcWidth - 1
                        srcY = y - singleEdgeExt
                    }
                    else -> {
                        srcX = x - singleEdgeExt
                        srcY = y - singleEdgeExt
                    }
                }
                internalImage.setRGB(x, y, src.getRGB(srcX, srcY))
            }
        }
    }


}

@Suppress("NOTHING_TO_INLINE")
private inline fun FloatArray.getXY(x: Int, y: Int, dimension: Int): Float {
    return this[dimension * y + x]
}