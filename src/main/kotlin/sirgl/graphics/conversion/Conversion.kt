package sirgl.graphics.conversion

import java.awt.Color

const val xn = 0.31382
const val yn = 0.331
const val zn = 0.35518

fun Color.toLab() : LAB {
    val r = red
    val g = green
    val b = blue

    val x = r * 0.5767309 + g * 0.1855540 + b * 0.1881852
    val y = r * 0.2973769 + g * 0.6273491 + b * 0.0752741
    val z = r * 0.270343 + g * 0.0706872 + b * 0.9911085

    val lV = 116 * f(y / yn) - 16
    val aV = 500 * (f(x / xn) - f(y / yn))
    val bV = 200 * (f(y / yn) - f(z / zn))
    return LAB(lV, aV, bV)
}

private fun f(x: Double) = when {
    x > Math.pow(6.0 / 29, 3.0) -> Math.pow(x, 1.0 / 3)
    else -> (1.0 / 3) * Math.pow(29.0 / 6, 2.0) * x + (4.0 / 29)
}

fun Color.toHsv(): HSV {
    val r = red
    val g = green
    val b = blue
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)

    var h = when {
        max == r && g >= b -> 60 * (g - b) / (max - min).toDouble()
        max == r && g < b -> 60 * (g - b) / (max - min).toDouble() + 360
        max == g -> 60 * (b - r) / (max - min).toDouble() + 120
        max == b -> 60 * (r - g) / (max - min).toDouble() + 240
        else -> throw IllegalStateException()
    }

    if (h.isNaN()) {
        h = 360.0
    }

    var s = 1 - min / max.toDouble()

    if (s.isNaN()) {
        s = 1.0
    }
    val v = max.toDouble() / 255.0

    return HSV(h / 360.0, s, v)
}

fun HSV.toRgb(): Color {
    val hI = (h * 6).toInt()
    val f = h * 6 - hI
    val p = v * (1 - s)
    val q = v * (1 - f * s)
    val t = v * (1 - (1 - f) * s)

    return when (hI) {
        0 -> toColor(v, t, p)
        1 -> toColor(q, v, p)
        2 -> toColor(p, v, t)
        3 -> toColor(p, q, v)
        4 -> toColor(t, p, v)
        5, 6 -> toColor(v, p, q)
        else -> throw RuntimeException("Bad conversion for $h, $s, $v")
    }
}

private fun toColor(r: Double, g: Double, b: Double) =  Color(
    Math.round(r * 256).toInt().truncate(),
    Math.round(g * 256).toInt().truncate(),
    Math.round(b * 256).toInt().truncate()
)

@Suppress("NOTHING_TO_INLINE")
private inline fun Int.truncate() = if (this >= 256) 255 else this