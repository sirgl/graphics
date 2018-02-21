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
    override fun transformPixel(rgb: Int) : Int {
        val hValue = hSliderPos.value ?: return rgb
        val sValue = sSliderPos.value ?: return rgb
        val vValue = vSliderPos.value ?: return rgb
        val hsv = toHsv(rgb).transformHSV(hValue, sValue, vValue)
        return hsv.toRgbI()
    }

    private fun HSV.transformHSV(hSliderPos: Int, sSliderPos: Int, vSliderPos: Int) = HSV(
            transformValue(h, hSliderPos),
            transformValue(s, sSliderPos),
            transformValue(v, vSliderPos)
    )

    private fun transformValue(v: Float, sliderVal: Int) = when {
        sliderVal < 50 -> (sliderVal.toFloat() / 50) * v
        else -> (sliderVal.toFloat() / 50 - 1.0f) * (1.0f - v) + v
    }
}