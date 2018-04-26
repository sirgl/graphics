@file:Suppress("unused")

package sirgl.graphics.segmentation.meanshift

import sirgl.graphics.conversion.LAB
import sirgl.graphics.conversion.fromRgb
import sirgl.graphics.filter.ImageFilter
import sirgl.graphics.segmentation.ImgLike
import sirgl.graphics.segmentation.computeCiede2000Metrics
import sirgl.graphics.segmentation.toImg
import java.awt.image.BufferedImage
import kotlin.math.abs

class MeanShiftFilter : ImageFilter {
    override fun transform(src: BufferedImage, res: BufferedImage): Boolean {
        val srcImg = src.toImg()
        val resImg = res.toImg()
        applyMeanShift(srcImg, resImg)
        return true
    }
}

fun applyMeanShift(
        src: ImgLike,
        res: ImgLike,
        distanceF: (Point, Point) -> Float = ::labDistance,
        kernelWidth: Float = 3f,
        kernel: (Float, Float) -> Float = ::kernelEpanch
) {
    val bufferLab = LAB()
    val copy = toPointMatrix(src, bufferLab)
    val original = toPointMatrix(src, bufferLab)

    var idx = 0
    for (row in copy) {
        for (point in row) {
            if (idx % 1000 == 0) {
//                println(idx.toString() + " positions handled")
            }
            var previousShift = 12f
            var internalIdx = 0
            while (previousShift > 0.04) {
                previousShift = point.shift(original, distanceF, kernelWidth, kernel)
                internalIdx++
            }
//            println(internalIdx.toString() + " iterations")
            idx++
        }
    }

    val coordinateToColor = mutableMapOf<Coordinate, Int>()
    for ((y, row) in copy.withIndex()) {
        for ((x, point) in row.withIndex()) {
            val coordinate = Coordinate(point.x.toInt(), point.y.toInt())
            val color = coordinateToColor.computeIfAbsent(coordinate, { src.getRGB(x, y) })
            res.setRGB(x, y, color)
        }
    }
    println("${coordinateToColor.size} colors")
}

data class Coordinate(
        val x: Int,
        val y: Int
)

private fun toPointMatrix(src: ImgLike, bufferLab: LAB): MutableList<MutableList<Point>> {
    val matrix = mutableListOf<MutableList<Point>>()
    for (y in (0 until src.height)) {
        val row = mutableListOf<Point>()
        matrix.add(row)
        for (x in (0 until src.width)) {
            bufferLab.fromRgb(src.getRGB(x, y))
            row.add(Point(
                    x.toFloat(),
                    y.toFloat(),
                    LAB(bufferLab.l, bufferLab.a, bufferLab.b)
            ))
        }
    }
    return matrix
}

class Point(
        var x: Float,
        var y: Float,
        val lab: LAB
) {
    fun shift(
            originalPoints: MutableList<MutableList<Point>>,
            distanceFunc: (Point, Point) -> Float,
            kernelWidth: Float,
            kernel: (Float, Float) -> Float
    ): Float {
        var shiftX = 0f
        var shiftY = 0f
        var scaleFactor = 0f
        for (row in originalPoints) {
            for (point in row) {
                val distance = distanceFunc(this, point)
                val weight = kernel(distance, kernelWidth)
                shiftX += point.x * weight
                shiftY += point.y * weight
                scaleFactor += weight
            }
        }

        val newX = shiftX / scaleFactor
        val newY = shiftY / scaleFactor
        val shift = abs(x - newX) + abs(y - newY)
        x = newX
        y = newY
        return shift
    }
}

fun labDistance(p1: Point, p2: Point)  = computeCiede2000Metrics(p1.lab, p2.lab).toFloat()

fun manhattanDist(p1: Point, p2: Point) = abs(p1.x - p2.x) + abs(p1.y - p2.y)

fun kernelEpanch(distance: Float, kernelWidth: Float): Float {
    if (distance >= kernelWidth) return 0f
    return 0.75f * ( 1 - (distance / kernelWidth) * (distance / kernelWidth))
}