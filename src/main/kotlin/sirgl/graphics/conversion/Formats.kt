@file:Suppress("NOTHING_TO_INLINE")

package sirgl.graphics.conversion

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.Writer
import java.lang.Math.abs

class HSV(
        var h: Float,
        var s: Float,
        var v: Float
)

class LAB(
        var l: Float,
        var a: Float,
        var b: Float
) {
    constructor() : this(0f, 0f, 0f)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LAB

        if (abs(l - other.l) > 0.004) return false
        if (abs(a - other.a) > 0.004) return false
        if (abs(b - other.b) > 0.004) return false

        return true
    }

    override fun hashCode(): Int {
        var result = l.hashCode()
        result = 31 * result + a.hashCode()
        result = 31 * result + b.hashCode()
        return result
    }

}

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
    FormatType.RGB -> write(writer, ::printRGB, x1, y1, x2, y2)
    FormatType.HSV -> write(writer, ::printHSV, x1, y1, x2, y2)
    FormatType.LAB -> write(writer, ::printLAB, x1, y1, x2, y2)
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

