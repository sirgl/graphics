package sirgl.graphics.core

import sirgl.graphics.canvas.MouseDraggedEvt
import sirgl.graphics.canvas.Point
import sirgl.graphics.conversion.*
import sirgl.graphics.gist.Gist
import sirgl.graphics.observable.Observable
import sirgl.graphics.observable.SimpleObservable
import sirgl.graphics.observable.map
import java.awt.Color
import java.awt.image.BufferedImage

object RefreshAllEvent

@Suppress("RemoveExplicitTypeArguments")
class App {
    val imageObservable: Observable<BufferedImage> = SimpleObservable<BufferedImage>(null)
    val imageToDrawObservable: Observable<BufferedImage> = SimpleObservable(imageObservable, this::transformImage)

    val hSliderPosition: Observable<Int> = SimpleObservable(50)
    val sSliderPosition: Observable<Int> = SimpleObservable(50)
    val vSliderPosition: Observable<Int> = SimpleObservable(50)

    val isSelectionMode: Observable<Boolean> = SimpleObservable(false)

    val saveTypeObservable: Observable<FormatType> = SimpleObservable(FormatType.RGB)

    val repaintAllObservable: Observable<RefreshAllEvent> = SimpleObservable<RefreshAllEvent>(null)
    fun repaintAll() {
        repaintAllObservable.value = RefreshAllEvent
    }

    val gistObservable: Observable<Gist> = SimpleObservable<Gist>(null)

    val currentPositionObservable: Observable<Point> = SimpleObservable<Point>(null)
    val mouseDraggedObservable: Observable<MouseDraggedEvt> = SimpleObservable<MouseDraggedEvt>(null)

    val currentRGB: Observable<Color> = SimpleObservable(currentPositionObservable).map {
        it ?: return@map null
        val image = imageToDrawObservable.value ?: return@map null
        return@map Color(image.getRGB(it.x, it.y))
    }

    val currentHSV: Observable<HSV> = SimpleObservable(currentRGB).map { (it ?: return@map null).toHsv() }
    val currentLAB: Observable<LAB> = SimpleObservable(currentRGB).map { (it ?: return@map null).toLab() }


    fun transformImage(image: BufferedImage?): BufferedImage? {
        image ?: return null
        val height = image.height
        val width = image.width
        val transformed = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val hSliderPos = hSliderPosition.value ?: return null
        val sSliderPos = sSliderPosition.value ?: return null
        val vSliderPos = vSliderPosition.value ?: return null
        for (y in 0 until height) {
            for (x in 0 until width) {
                val original = Color(image.getRGB(x, y))
                val hsv = original.toHsv()
                val transformedHsv = hsv.transformHSV(hSliderPos, sSliderPos, vSliderPos)
                val transformedRgb = transformedHsv.toRgb()
                transformed.setRGB(x, y, transformedRgb.rgb)
            }
        }
        return transformed
    }

    private fun HSV.transformHSV(hSliderPos: Int, sSliderPos: Int, vSliderPos: Int) = HSV(
            transformValue(h, hSliderPos),
            transformValue(s, sSliderPos),
            transformValue(v, vSliderPos)
    )

    private fun transformValue(v: Double, sliderVal: Int): Double {
        return when {
            sliderVal < 50 -> (sliderVal.toDouble() / 50) * v
            else -> (sliderVal.toDouble() / 50 - 1.0) * (1.0 - v) + v
        }
    }
}