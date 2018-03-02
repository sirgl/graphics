package sirgl.graphics.filter.grayscale

import sirgl.graphics.conversion.constructRgbI
import sirgl.graphics.conversion.getBlue
import sirgl.graphics.conversion.getGreen
import sirgl.graphics.conversion.getRed
import sirgl.graphics.filter.SinglePixelTransformingFilter
import kotlin.math.roundToInt

class GrayscaleFilter : SinglePixelTransformingFilter() {
    override fun transformPixel(rgb: Int): Int {
        val red = getRed(rgb)
        val green = getGreen(rgb)
        val blue = getBlue(rgb)
//        val gray = red + green + blue / 3
        val gray = (red.toFloat() * 0.3f + 0.59f * green.toFloat() + 0.11f * blue).roundToInt()
        return constructRgbI(gray, gray, gray)
    }
}