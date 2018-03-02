package sirgl.graphics.filter.hsv

import sirgl.graphics.conversion.HSV
import sirgl.graphics.conversion.toHsv
import sirgl.graphics.conversion.toRgbI
import sirgl.graphics.filter.SinglePixelTransformingFilter
import sirgl.graphics.observable.Observable

class HSVImageFilter(
        private val hSliderPos: Observable<Int>,
        private val sSliderPos: Observable<Int>,
        private val vSliderPos: Observable<Int>
) : SinglePixelTransformingFilter() {
    private val srcHsvBuffer = HSV(0f, 0f, 0f)
    private val transformedHsvBuffer = HSV(0f, 0f, 0f)

    override fun transformPixel(rgb: Int): Int {
        val hValue = hSliderPos.value ?: return rgb
        val sValue = sSliderPos.value ?: return rgb
        val vValue = vSliderPos.value ?: return rgb
        toHsv(rgb, srcHsvBuffer)
        transformHSV(hValue, sValue, vValue, srcHsvBuffer, transformedHsvBuffer)
        return transformedHsvBuffer.toRgbI()
    }

    private fun transformHSV(hSliderPos: Int, sSliderPos: Int, vSliderPos: Int, src: HSV, target: HSV) {
        target.h = transformValue(src.h, hSliderPos)
        target.s = transformValue(src.s, sSliderPos)
        target.v = transformValue(src.v, vSliderPos)
    }

    private fun transformValue(v: Float, sliderVal: Int) = when {
        sliderVal < 50 -> (sliderVal.toFloat() / 50) * v
        else -> (sliderVal.toFloat() / 50 - 1.0f) * (1.0f - v) + v
    }
}