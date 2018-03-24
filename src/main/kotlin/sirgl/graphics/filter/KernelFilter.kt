package sirgl.graphics.filter

import sirgl.graphics.conversion.constructRgbI
import sirgl.graphics.observable.Observable
import java.awt.image.BufferedImage
import kotlin.math.max
import kotlin.math.min

@Suppress("NOTHING_TO_INLINE")
abstract class KernelFilter(private val kernelObservable: Observable<KernelInfo>) : ImageFilter {
    private var extendedImage = BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)
    private var normalizationBufferR = Matrix(0, 0)
    private var normalizationBufferG = Matrix(0, 0)
    private var normalizationBufferB = Matrix(0, 0)
    private val rgbBuffer = RGB(0f, 0f, 0f)


    override fun transform(src: BufferedImage, res: BufferedImage): Boolean {
        val srcHeight = src.height
        val srcWidth = src.width
        val kernelInfo = kernelObservable.value ?: return false
        println("Apply kernel filter: ${kernelInfo.dimension}")
        val edgeExtension = kernelInfo.edgeExtension
        val extendedHeight = srcHeight + edgeExtension + edgeExtension
        val extendedWidth = srcWidth + edgeExtension + edgeExtension
        if (extendedHeight != extendedImage.height || extendedWidth != extendedImage.width) {
            extendedImage = BufferedImage(extendedWidth, extendedHeight, BufferedImage.TYPE_INT_RGB)
            normalizationBufferR = Matrix(srcWidth, srcHeight)
            normalizationBufferG = Matrix(srcWidth, srcHeight)
            normalizationBufferB = Matrix(srcWidth, srcHeight)
        }
        fillExtendedImage(extendedWidth, extendedHeight, edgeExtension, srcWidth, srcHeight, src)
        traverse(srcHeight, srcWidth, edgeExtension)
        normalize(kernelInfo)
        copyFromBuffers(res)
        return true
    }

    private fun copyFromBuffers(to: BufferedImage) {
        val width = to.width
        val height = to.height
        for (y in (0 until height)) {
            for (x in (0 until width)) {
                val r = normalizationBufferR.getXY(x, y)
                val g = normalizationBufferG.getXY(x, y)
                val b = normalizationBufferB.getXY(x, y)
                if (r > 255f || g > 255f || b > 255f) {
                    println("$r $g $b")
                }
                val rgb = constructRgbI(r, g, b)
                to.setRGB(x, y, rgb)
            }
        }
    }

    private fun normalize(kernelInfo: KernelInfo) {
        if (kernelInfo !is MatrixKernelInfo || kernelInfo.matrix.values.sum() < 0.04) {
            val minValue = min(
                    min(normalizationBufferR.values.min()!!, normalizationBufferG.values.min()!!),
                    normalizationBufferB.values.min()!!
            )
            val maxValue = max(
                    max(normalizationBufferR.values.max()!!, normalizationBufferG.values.max()!!),
                    normalizationBufferB.values.max()!!
            ) - minValue
            val normValue = 255.0f / maxValue
            normalize(normValue, minValue)
        } else {

            val sum = kernelInfo.matrix.values.sum()
            normalize(1 / sum, 0f)
        }
    }

    private fun traverse(srcHeight: Int, srcWidth: Int, edgeExtension: Int) {
        for (y in (0 until srcHeight)) {
            for (x in (0 until srcWidth)) {
                val xShifted = x + edgeExtension
                val yShifted = y + edgeExtension
                transformRGB(
                        xShifted,
                        yShifted,
                        extendedImage,
                        rgbBuffer
                )
                normalizationBufferR.setXY(x, y, rgbBuffer.r)
                normalizationBufferG.setXY(x, y, rgbBuffer.g)
                normalizationBufferB.setXY(x, y, rgbBuffer.b)
            }
        }
    }

    private fun normalize(
            normCoefficient: Float,
            minValue: Float
    ) {
        val height = normalizationBufferR.height
        val width = normalizationBufferR.width
        for (y in (0 until height)) {
            for (x in (0 until width)) {
                val r = (normalizationBufferR.getXY(x, y) - minValue) * normCoefficient
                normalizationBufferR.setXY(x, y, r)
                val g = (normalizationBufferG.getXY(x, y) - minValue) * normCoefficient
                normalizationBufferG.setXY(x, y, g)
                val b = (normalizationBufferB.getXY(x, y) - minValue) * normCoefficient
                normalizationBufferB.setXY(x, y, b)
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
                { _, y -> src.getRGB(0, y) }
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
}

open class KernelInfo(
        val dimension: Int
) {
    val edgeExtension: Int
        get() = dimension / 2
}

// Expects to be square
class MatrixKernelInfo(
        val matrix: Matrix
) : KernelInfo(matrix.width)

class RGB(
        var r: Float,
        var g: Float,
        var b: Float
)
