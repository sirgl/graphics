@file:Suppress("NOTHING_TO_INLINE")

package sirgl.graphics.conversion

import java.awt.Color
import kotlin.math.roundToInt

const val xn = 0.9504f
const val yn = 1.0000f
const val zn = 1.0888f

fun Color.toLab() = LAB().apply { fromRgb(rgb) }

fun LAB.fromRgb(rgb: Int) {
    val r = getRed(rgb)
    val g = getGreen(rgb)
    val b = getBlue(rgb)

    val x = (r * 0.5767309f + g * 0.1855540f + b * 0.1881852f) / 255.0f
    val y = (r * 0.2973769f + g * 0.6273491f + b * 0.0752741f) / 255.0f
    val z = (r * 0.0270343f + g * 0.0706872f + b * 0.9911085f) / 255.0f

    this.l = 116 * f(y / yn) - 16
    this.a = 500 * (f(x / xn) - f(y / yn))
    this.b = 200 * (f(y / yn) - f(z / zn))
}

private fun f(x: Float) = when {
    x > Math.pow(6.0 / 29, 3.0) -> Math.pow(x.toDouble(), 1.0 / 3).toFloat()
    else -> ((1.0 / 3) * Math.pow(29.0 / 6, 2.0) * x + (4.0 / 29)).toFloat()
}

fun Color.toHsv(): HSV {
    val r = red
    val g = green
    val b = blue
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)

    var h = when {
        max == r && g >= b -> 60 * (g - b) / (max - min).toFloat()
        max == r && g < b -> 60 * (g - b) / (max - min).toFloat() + 360
        max == g -> 60 * (b - r) / (max - min).toFloat() + 120
        max == b -> 60 * (r - g) / (max - min).toFloat() + 240
        else -> throw IllegalStateException()
    }
    if (h.isNaN()) h = 360.0f
    var s = 1 - min / max.toFloat()
    if (s.isNaN()) s = 1.0f
    val v = max.toFloat() / 255.0f

    return HSV(h / 360.0f, s, v)
}

fun toHsv(rgb: Int, hsv: HSV) {
    val r = getRed(rgb)
    val g = getGreen(rgb)
    val b = getBlue(rgb)
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)

    var h = when {
        max == r && g >= b -> 60 * (g - b) / (max - min).toFloat()
        max == r && g < b -> 60 * (g - b) / (max - min).toFloat() + 360
        max == g -> 60 * (b - r) / (max - min).toFloat() + 120
        max == b -> 60 * (r - g) / (max - min).toFloat() + 240
        else -> throw IllegalStateException()
    }
    if (h.isNaN()) h = 360.0f
    var s = 1 - min / max.toFloat()
    if (s.isNaN()) s = 1.0f
    val v = max.toFloat() / 255.0f

    hsv.h = h / 360.0f
    hsv.s = s
    hsv.v = v
}

//fun HSV.toRgb(): Color {
//    val hI = (h * 6).toInt()
//    val f = h * 6 - hI
//    val p = v * (1 - s)
//    val q = v * (1 - f * s)
//    val t = v * (1 - (1 - f) * s)
//
//    return when (hI) {
//        0 -> toColor(v, t, p)
//        1 -> toColor(q, v, p)
//        2 -> toColor(p, v, t)
//        3 -> toColor(p, q, v)
//        4 -> toColor(t, p, v)
//        5, 6 -> toColor(v, p, q)
//        else -> throw RuntimeException("Bad conversion for $h, $s, $v")
//    }
//}

fun HSV.toRgbI(): Int {
    val hI = (h * 6).toInt()
    val f = h * 6 - hI
    val p = v * (1 - s)
    val q = v * (1 - f * s)
    val t = v * (1 - (1 - f) * s)

    return when (hI) {
        0 -> toRgbI(v, t, p)
        1 -> toRgbI(q, v, p)
        2 -> toRgbI(p, v, t)
        3 -> toRgbI(p, q, v)
        4 -> toRgbI(t, p, v)
        5, 6 -> toRgbI(v, p, q)
        else -> throw RuntimeException("Bad conversion for $h, $s, $v")
    }
}

private inline fun toRgbI(r: Float, g: Float, b: Float) = constructRgbI(
        Math.round(r * 256).truncateRGB(),
        Math.round(g * 256).truncateRGB(),
        Math.round(b * 256).truncateRGB()
)

inline fun constructRgbI(r: Float, g: Float, b: Float) =
        constructRgbI(r.roundToInt(), g.roundToInt(), b.roundToInt())

inline fun constructRgbI(r: Int, g: Int, b: Int) =
        (r and 0xFF shl 16) or
                (g and 0xFF shl 8) or
                (b and 0xFF shl 0)

//private fun toColor(r: Float, g: Float, b: Float) = Color(
//        Math.round(r * 256).truncate(),
//        Math.round(g * 256).truncate(),
//        Math.round(b * 256).truncate()
//)

@Suppress("NOTHING_TO_INLINE")
fun Int.truncateRGB() = if (this >= 256) 255 else this