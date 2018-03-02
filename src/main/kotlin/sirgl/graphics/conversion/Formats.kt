@file:Suppress("NOTHING_TO_INLINE")

package sirgl.graphics.conversion

import sirgl.graphics.conversion.FormatType.*
import sirgl.graphics.conversion.FormatType.HSV
import sirgl.graphics.conversion.FormatType.LAB
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.Writer

class HSV(
        var h: Float,
        var s: Float,
        var v: Float
)

class LAB(
        val l: Float,
        val a: Float,
        val b: Float
)

enum class FormatType {
    RGB,
    HSV,
    LAB
}

inline fun getRed(rgb: Int): Int {
    return rgb shr 16 and 0xFF
}

inline fun getGreen(rgb: Int): Int {
    return rgb shr 8 and 0xFF
}

inline fun getBlue(rgb: Int): Int {
    return rgb shr 0 and 0xFF
}

fun BufferedImage.write(
        format: FormatType,
        writer: Writer,
        x1: Int = 0,
        y1: Int = 0,
        x2: Int = width - 1,
        y2: Int = height - 1
) = when (format) {
    RGB -> write(writer, ::printRGB, x1, y1, x2, y2)
    HSV -> write(writer, ::printHSV, x1, y1, x2, y2)
    LAB -> write(writer, ::printLAB, x1, y1, x2, y2)
}

fun BufferedImage.write(
        writer: Writer,
        toStringMapper: (Color) -> String,
        x1: Int = 0,
        y1: Int = 0,
        x2: Int = width - 1,
        y2: Int = height - 1
) {
    for (y in (y1..y2)) {
        writer.write("[")
        for (x in (x1..x2)) {
            writer.write(toStringMapper(Color(getRGB(x, y))))
        }
        writer.write("]")
    }
}

fun printRGB(color: Color) = "(${color.red}, ${color.green}, ${color.red})"
fun printHSV(color: Color) = color.toHsv().run { "($h, $s, $v)" }
fun printLAB(color: Color) = color.toLab().run { "($l, $a, $b)" }

