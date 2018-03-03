@file:Suppress("NOTHING_TO_INLINE")

package sirgl.graphics.filter.sobel

import sirgl.graphics.filter.*
import sirgl.graphics.observable.SimpleObservable
import java.awt.image.BufferedImage
import kotlin.math.absoluteValue

//private val m = arrayOf(
//        arrayOf(0, -1, 0),
//        arrayOf(-1, 5, -1),
//        arrayOf(0, -1, 0)
//).flattenNum()


class SobelFilter : MatrixFilter(kernelDataObservable = SimpleObservable(KernelData(3, 8f))) {
    override fun transformRGB(x: Int, y: Int, img: BufferedImage, rgb: RGB) {
        rgb.r = transformChanel(img, x, y, ChanelType.R)
        rgb.g = transformChanel(img, x, y, ChanelType.G)
        rgb.b = transformChanel(img, x, y, ChanelType.B)
    }

    private inline fun transformChanel(img: BufferedImage, x: Int, y: Int, chanelType: ChanelType): Float {
        val gx = convolveChanel(xMatrixFlat, 3, img, x, y, chanelType)
        val gy = convolveChanel(yMatrixFlat, 3, img, x, y, chanelType)
        return Math.sqrt((gx * gx + gy * gy).toDouble()).toFloat()
    }
}

private val posArr = arrayOf(-1, 0, 1)
private val xMatrix = arrayOf(
        arrayOf(1, 0, -1),
        arrayOf(2, 0, -2),
        arrayOf(1, 0, -1)
)


private val yMatrix = arrayOf(
        arrayOf(1, 2, 1),
        arrayOf(0, 0, 0),
        arrayOf(-1, -2, -1)
)

private val xMatrixFlat = xMatrix.flattenNum()
private val yMatrixFlat = yMatrix.flattenNum()

private fun Array<Array<Int>>.flattenNum(): FloatArray {
    val flat = FloatArray(size * size)
    for (y in (0 until size)) {
        for (x in (0 until size)) {
            flat[y * size + x] = this[y][x].toFloat()
        }
    }
    return flat
}

// Old sobel generating function

private fun generatePos(): String = buildString {
    for (xOffset in posArr) {
        for (yOffset in posArr) {

            val xOffsetStr = sumInt(xOffset)
            val yOffsetStr = sumInt(yOffset)
            append("val rgb${xOffset + 1}${yOffset + 1} = img.getRGB(x$xOffsetStr, y$yOffsetStr)\n")
        }
    }
}

private fun sumInt(i: Int): String {
    return when {
        i == 0 -> ""
        i < 0 -> " - ${i.absoluteValue}"
        else -> " + $i"
    }
}

private val colors = arrayOf("Red", "Green", "Blue")

private fun generateMatrixConvolution(matrix: Array<Array<Int>>, name: String) = buildString {
    for (color in colors) {
        append("val $name$color = \n")
        for (y in (0..2)) {
            for (x in (0..2)) {
                val el = matrix[x][y]
                if (el == 0) continue
                val prefix = when {
                    el == 1 -> " + "
                    el == -1 -> " - "
                    el < 0 -> " - ${el.absoluteValue} * "
                    else -> " + $el *"
                }
                append(prefix).append("get$color(rgb$x$y)")
            }
            append("\n")
        }
        append("\n")
    }
}

private fun makeSobel() = buildString {
    append(generatePos())
    append(generateMatrixConvolution(xMatrix, "gx"))
    append(generateMatrixConvolution(yMatrix, "gy"))
    append(generateFinal())
}

private fun generateFinal() = buildString {
    for (color in colors) {
        val name = "g$color"
        append("val $name = Math.sqrt((gx$color * gx$color + gy$color * gy$color).toDouble()).roundToInt() / 8 \n")
    }
    append("return constructRgbI(gRed, gGreen, gBlue)")
}

fun main(args: Array<String>) {
    println(makeSobel())
}