package sirgl.graphics.conversion

import sirgl.graphics.conversion.FormatType.*
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.Writer

class HSV (
        val h: Double,
        val s: Double,
        val v: Double
)

class LAB (
        val l: Double,
        val a: Double,
        val b: Double
)

enum class FormatType {
    RGB,
    HSV,
    LAB
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

fun printRGB(color: Color)  = "(${color.red}, ${color.green}, ${color.red})"
fun printHSV(color: Color)  = color.toHsv().run { "($h, $s, $v)"}
fun printLAB(color: Color)  = color.toLab().run { "($l, $a, $b)"}

