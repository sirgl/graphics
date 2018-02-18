package sirgl.graphics.core

import sirgl.graphics.canvas.MouseDraggedEvt
import sirgl.graphics.canvas.Point
import sirgl.graphics.conversion.*
import sirgl.graphics.core.GistType.*
import sirgl.graphics.gist.Gist
import sirgl.graphics.observable.*
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.StringWriter
import kotlin.math.max
import kotlin.math.min

object RefreshAllEvent

enum class GistType { L, A, B }

@Suppress("RemoveExplicitTypeArguments")
class App {
    val imageObservable: Observable<BufferedImage> = SimpleObservable<BufferedImage>(null)
    val imageToDrawObservable: Observable<BufferedImage> = SimpleObservable(imageObservable, this::transformImage)

    val hSliderPosition: Observable<Int> = SimpleObservable(50)
    val sSliderPosition: Observable<Int> = SimpleObservable(50)
    val vSliderPosition: Observable<Int> = SimpleObservable(50)

    val saveTypeObservable: Observable<FormatType> = SimpleObservable(FormatType.RGB)

    val repaintAllObservable: Observable<RefreshAllEvent> = SimpleObservable<RefreshAllEvent>(null)
    fun repaintAll() {
        repaintAllObservable.value = RefreshAllEvent
    }

    val gistObservable: Observable<Gist> = imageToDrawObservable.map { recomputeGist(it ?: return@map null) }
    val gistTypeObservable: Observable<GistType> = SimpleObservable<GistType>(L)

    val currentPositionObservable: Observable<Point> = SimpleObservable<Point>(null)
    val mouseDraggedObservable: Observable<MouseDraggedEvt> = SimpleObservable<MouseDraggedEvt>(null)

    val selectedRegionText: Observable<String> = mouseDraggedObservable.filter {
        it ?: return@filter false
        val img = imageToDrawObservable.value ?: return@filter false
        return@filter it.newPoint.isInside(img) && it.oldPoint.isInside(img)
    }.map {
                it ?: return@map null
                val minX = min(it.newPoint.x, it.oldPoint.x)
                val maxX = max(it.newPoint.x, it.oldPoint.x)
                val minY = min(it.newPoint.y, it.oldPoint.y)
                val maxY = max(it.newPoint.y, it.oldPoint.y)
                val writer = StringWriter()
                val img = imageToDrawObservable.value ?: return@map null
                val saveType = saveTypeObservable.value ?: return@map null
                img.write(saveType, writer, minX, minY, maxX, maxY)
                return@map writer.toString()
            }

    val currentRGB: Observable<Color> = SimpleObservable(currentPositionObservable).map {
        it ?: return@map null
        val image = imageToDrawObservable.value ?: return@map null
        return@map Color(image.getRGB(it.x, it.y))
    }

    val currentHSV: Observable<HSV> = SimpleObservable(currentRGB).map { (it ?: return@map null).toHsv() }
    val currentLAB: Observable<LAB> = SimpleObservable(currentRGB).map { (it ?: return@map null).toLab() }

    fun init() {
        imageToDrawObservable.recomputeOnChange(
                observables = *arrayOf(hSliderPosition, sSliderPosition, vSliderPosition),
                recomputeAction = { transformImage(imageObservable.value) }
        )

        gistObservable.recomputeOnChange(
                observables = *arrayOf(gistTypeObservable),
                recomputeAction = {
                    val img = imageObservable.value ?: return@recomputeOnChange null
                    return@recomputeOnChange recomputeGist(img)
                }
        )
    }

    private fun <T> Observable<T>.recomputeOnChange(vararg observables: Observable<*>, recomputeAction: () -> T?) {
        for (observable in observables) {
            observable.subscribe {
                value = recomputeAction()
            }
        }
    }

    private fun recomputeGist(img: BufferedImage): Gist? {
        val values = DoubleArray(img.width * img.height)
        val gistType = gistTypeObservable.value ?: return null
        var counter = 0
        for (y in 0 until img.height) {
            for (x in 0 until img.width) {
                val lab = Color(img.getRGB(x, y)).toLab()
                val value = when (gistType) {
                    L -> lab.l
                    A -> lab.a
                    B -> lab.b
                }
                values[counter] = value
                counter++
            }
        }
        return Gist(values)
    }

    private fun transformImage(image: BufferedImage?): BufferedImage? {
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