@file:Suppress("NOTHING_TO_INLINE")

package sirgl.graphics.filter

import sirgl.graphics.conversion.constructRgbI
import sirgl.graphics.observable.Observable
import java.awt.image.BufferedImage
import kotlin.math.max

// TODO add cache invalidation event observer
abstract class MatrixFilter(private val kernelDataObservable: Observable<KernelData>) : ImageFilter {
    private var extendedImage = BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)
    private var normalizationBufferR = FloatArray(0)
    private var normalizationBufferG = FloatArray(0)
    private var normalizationBufferB = FloatArray(0)
    private val rgbBuffer = RGB(0f, 0f, 0f)

    override fun transform(src: BufferedImage, res: BufferedImage): Boolean {
        val srcHeight = src.height
        val srcWidth = src.width
        val dimension = kernelDataObservable.value?.dimension ?: return false
        val edgeExtension = dimension - 1
        val extendedHeight = srcHeight + edgeExtension
        val extendedWidth = srcWidth + edgeExtension
        val singleEdgeExt = edgeExtension / 2
        if (extendedHeight != extendedImage.height || extendedWidth != extendedImage.width) { // TODO also check
            extendedImage = BufferedImage(extendedWidth, extendedHeight, BufferedImage.TYPE_INT_RGB)
            normalizationBufferR = FloatArray(extendedWidth * extendedHeight)
            normalizationBufferG = FloatArray(extendedWidth * extendedHeight)
            normalizationBufferB = FloatArray(extendedWidth * extendedHeight)
        }

        fillExtendedImage(extendedWidth, extendedHeight, singleEdgeExt, srcWidth, srcHeight, src)

        for (y in (0 until srcHeight)) {
            for (x in (0 until srcWidth)) {
                val xShifted = x + singleEdgeExt
                val yShifted = y + singleEdgeExt
                transformRGB(
                        xShifted,
                        yShifted,
                        extendedImage,
                        rgbBuffer
                )
                normalizationBufferR.setXY(xShifted, yShifted, extendedWidth, rgbBuffer.r)
                normalizationBufferG.setXY(xShifted, yShifted, extendedWidth, rgbBuffer.g)
                normalizationBufferB.setXY(xShifted, yShifted, extendedWidth, rgbBuffer.b)
            }
        }
        val maxValue = max(max(normalizationBufferR.max()!!, normalizationBufferG.max()!!), normalizationBufferB.max()!!)
        if (maxValue > 255.0f) {
            // Probably here we can use norm ratio
            val normValue = 255.0f / maxValue
            normalize(normValue, normalizationBufferR, normalizationBufferG, normalizationBufferB, extendedWidth, extendedHeight)
        }
        copy(singleEdgeExt + srcWidth, singleEdgeExt + srcHeight, res, singleEdgeExt, extendedWidth)
        return true
    }

    private fun normalize(
            normCoefficient: Float,
            rArr: FloatArray,
            gArr: FloatArray,
            bArr: FloatArray,
            extWidth: Int,
            extHeight: Int
    ) {
        for (y in (0 until extHeight)) {
            for (x in (0 until extWidth)) {
                rArr.setXY(x, y, extWidth, rArr.getXY(x, y, extWidth) * normCoefficient)
                gArr.setXY(x, y, extWidth, gArr.getXY(x, y, extWidth) * normCoefficient)
                bArr.setXY(x, y, extWidth, bArr.getXY(x, y, extWidth) * normCoefficient)
            }
        }
    }


    private fun copy(xEnd: Int, yEnd: Int, to: BufferedImage, edgeExtension: Int, width: Int) {
        for (y in (edgeExtension until yEnd)) {
            for (x in (edgeExtension until xEnd)) {
                val r = normalizationBufferR.getXY(x, y, width)
                val g = normalizationBufferG.getXY(x, y, width)
                val b = normalizationBufferB.getXY(x, y, width)
                if (r > 255f || g > 255f || b > 255f) {
                    println("$r $g $b")
                }
                val rgb = constructRgbI(r, g, b)
                to.setRGB(x - edgeExtension, y - edgeExtension, rgb)
            }
        }
    }

    /**
     * Here you can be sure, that size of [img] will be enough to handle edge pixels with kernel
     * This method should put new pixel rgb into appropriate buffer
     */
    abstract fun transformRGB(
            x: Int,
            y: Int,
            img: BufferedImage,
            rgb: RGB
    )

    @Suppress("UnnecessaryVariable")
    private fun fillExtendedImage(
            extendedWidth: Int,
            extendedHeight: Int,
            singleEdgeExt: Int,
            srcWidth: Int,
            srcHeight: Int,
            src: BufferedImage
    ) {
        val upInternalEdge = singleEdgeExt
        val downInternalEdge = singleEdgeExt + srcHeight
        val leftInternalEdge = singleEdgeExt
        val rightInternalEdge = singleEdgeExt + srcWidth
        // Fill left up corner
        fillCornerRegion(
                0,
                0,
                upInternalEdge,
                leftInternalEdge,
                src.getRGB(0, 0)
        )
        // Fill left down corner
        fillCornerRegion(
                0,
                downInternalEdge,
                leftInternalEdge,
                extendedHeight,
                src.getRGB(0, srcHeight - 1)
        )
        // Fill right down corner
        fillCornerRegion(
                rightInternalEdge,
                downInternalEdge,
                extendedWidth,
                extendedHeight,
                src.getRGB(srcWidth - 1, srcHeight - 1)
        )
        // Fill right up corner
        fillCornerRegion(
                rightInternalEdge,
                0,
                extendedWidth,
                singleEdgeExt,
                src.getRGB(srcWidth - 1, 0)
        )
        // Up
        fillSideRegion(
                singleEdgeExt,
                0,
                rightInternalEdge,
                singleEdgeExt,
                singleEdgeExt,
                { x, _ -> src.getRGB(x, 0) }
        )
        // Left
        fillSideRegion(
                0,
                singleEdgeExt,
                leftInternalEdge,
                downInternalEdge,
                singleEdgeExt,
                { x, y -> src.getRGB(0, y) }
        )
        // Down
        fillSideRegion(
                leftInternalEdge,
                downInternalEdge,
                rightInternalEdge,
                extendedHeight,
                singleEdgeExt,
                { x, y -> src.getRGB(x, srcHeight - 1) }
        )
        // Right
        fillSideRegion(
                rightInternalEdge,
                upInternalEdge,
                extendedWidth,
                downInternalEdge,
                singleEdgeExt,
                { x, y -> src.getRGB(srcWidth - 1, y) }
        )
        for (y in (0 until srcHeight)) {
            for (x in (0 until srcWidth)) {
                val extX = x + singleEdgeExt
                val extY = y + singleEdgeExt
                val rgb = src.getRGB(x, y)
                extendedImage.setRGB(extX, extY, rgb)
            }
        }
    }

    private inline fun fillCornerRegion(extXStart: Int, extYStart: Int, extXEnd: Int, extYEnd: Int, rgb: Int) {
        for (y in (extYStart until extYEnd)) {
            for (x in (extXStart until extXEnd)) {
                extendedImage.setRGB(x, y, rgb)
            }
        }
    }

    private inline fun fillSideRegion(extXStart: Int, extYStart: Int, extXEnd: Int, extYEnd: Int, singleEdgeExt: Int, getPixel: (Int, Int) -> Int) {
        for (y in (extYStart until extYEnd)) {
            for (x in (extXStart until extXEnd)) {
                extendedImage.setRGB(x, y, getPixel(x - singleEdgeExt, y - singleEdgeExt))
            }
        }
    }

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
                extendedImage.setRGB(x, y, src.getRGB(srcX, srcY))
            }
        }
    }


}

class KernelData (
        val dimension: Int,
        val normalizationRatio: Float
)

class RGB(
        var r: Float,
        var g: Float,
        var b: Float
)

@Suppress("NOTHING_TO_INLINE")
inline fun FloatArray.getXY(x: Int, y: Int, width: Int): Float {
    return this[width * y + x]
}

@Suppress("NOTHING_TO_INLINE")
inline fun FloatArray.setXY(x: Int, y: Int, width: Int, value: Float) {
    this[x + y * width] = value
}