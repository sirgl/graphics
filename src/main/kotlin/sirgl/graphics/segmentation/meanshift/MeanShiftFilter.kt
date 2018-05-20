@file:Suppress("unused")

package sirgl.graphics.segmentation.meanshift

import sirgl.graphics.conversion.LAB
import sirgl.graphics.conversion.fromRgb
import sirgl.graphics.filter.ImageFilter
import sirgl.graphics.segmentation.ImgLike
import sirgl.graphics.segmentation.computeCiede2000Metrics
import java.awt.image.BufferedImage
import java.lang.Math.pow
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sqrt

class Point(
    var x: Float,
    var y: Float,
    var lab: LAB
)

open class MeanShiftFilter(val maxDistance: Double = 0.002, val kernelBandWidth: Float = 20f) : ImageFilter {
    override fun transform(src: BufferedImage, res: BufferedImage): Boolean {
        val orig = mutableListOf<MutableList<Point>>()
        val copy = createCopy(src, orig)
        shift(copy, orig)

        val coordinateToColor = mutableMapOf<Coord, Int>()

        for ((originalX, row) in copy.withIndex()) {
            for ((originalY, point) in row.withIndex()) {
                val shiftedX = point.x.toInt()
                val shiftedY = point.y.toInt()
                val coordinate = Coord(shiftedX, shiftedY)

                var color = coordinateToColor[coordinate]
                if (color == null) {
                    color = coordinateToColor.computeIfAbsent(coordinate, {
                        src.getRGB(originalX, originalY)
                    })
                }
                res.setRGB(originalX, originalY, color)
            }
        }
        return true
    }

    private fun shift(
        copy: MutableList<MutableList<Point>>,
        original: MutableList<MutableList<Point>>
    ) {
        for (copyList in copy) {
            for (point in copyList) {
                var iterationNumber = 0
                do {
                    val oldValue = point
                    shift(point,
                        original,
                        { p1: Point, p2: Point -> computeCiede2000Metrics(p1.lab, p2.lab).toFloat() },
                        kernelBandWidth,
                        { distance: Float, bandwidth: Float -> gaussianKernel(distance, bandwidth).toFloat() })
                    iterationNumber++
                    val distance = computeCiede2000Metrics(oldValue.lab, point.lab)
                } while (distance > maxDistance)
            }
        }
    }

    private fun createCopy(
        src: BufferedImage,
        original: MutableList<MutableList<Point>>
    ): MutableList<MutableList<Point>> {
        val copy = mutableListOf<MutableList<Point>>()
        for (x in 0 until src.width) {
            original.add(mutableListOf())
            copy.add(mutableListOf())
            for (y in 0 until src.height) {
                val lab = LAB()
                lab.fromRgb(src.getRGB(x, y))
                original[x].add(Point(x.toFloat(), y.toFloat(), lab))
                copy[x].add(Point(x.toFloat(), y.toFloat(), lab))
            }
        }
        return copy
    }

    private fun shift(
        currentPoint: Point,
        originalPoints: List<MutableList<Point>>,
        distanceFunc: (Point, Point) -> Float,
        kernelWidth: Float,
        kernel: (Float, Float) -> Float
    ) {
        var shiftX = 0f
        var shiftY = 0f
        var scaleFactor = 0f
        for (row in originalPoints) {
            for (point in row) {
                val distance = distanceFunc(currentPoint, point)
                val weight = kernel(distance, kernelWidth)
                shiftX += point.x * weight
                shiftY += point.y * weight
                scaleFactor += weight
            }
        }
        val newX = shiftX / scaleFactor
        val newY = shiftY / scaleFactor
        currentPoint.x = newX
        currentPoint.y = newY
        currentPoint.lab = originalPoints[newX.toInt()][newY.toInt()].lab
    }

    fun gaussianKernel(distance: Float, kernelWidth: Float): Double {
        return (1 / (kernelWidth * sqrt(2 * PI))) * exp(-0.5 * pow(distance / kernelWidth.toDouble(), 2.0))
    }

}

data class Coord(
    val x: Int,
    val y: Int
)