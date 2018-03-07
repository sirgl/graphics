package sirgl.graphics.core

import sirgl.graphics.canvas.MouseDraggedEvt
import sirgl.graphics.canvas.Point
import sirgl.graphics.conversion.*
import sirgl.graphics.observable.Observable
import sirgl.graphics.observable.SimpleObservable
import sirgl.graphics.observable.filter
import sirgl.graphics.observable.map
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.StringWriter
import kotlin.math.max
import kotlin.math.min

object RefreshAllEvent

enum class GistType { L, A, B }

interface ImageChangeListener {
    /**
     * To drop buffers
     */
    fun notifyOriginalImageChanged()
}

@Suppress("RemoveExplicitTypeArguments")
class App {
    // Image related data

    var internalImage = BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)


    val imageObservable: Observable<BufferedImage> = SimpleObservable<BufferedImage>(null)
    val imageToDrawObservable: Observable<BufferedImage> = SimpleObservable<BufferedImage>(null)

    private fun transformImage(src: BufferedImage?): BufferedImage? {
        src ?: return null
        val width = src.width
        val height = src.height
        val resultImg = if (width != internalImage.width || height != internalImage.width) {
            BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        } else {
            internalImage
        }
        filters.filterPipeline.transform(src, resultImg)
        return resultImg
    }

    // end image related data

    val filters: AppFilters = Filters()
    val gists: Gists = Gists(imageToDrawObservable)

    init {
        println("original image subscribed")
        imageObservable.subscribe {
            filters.notifyOriginalImageChanged()
            gists.notifyOriginalImageChanged()
        }
    }


    // pixel/area related data

    val currentPositionObservable: Observable<Point> = SimpleObservable<Point>(null)
    val mouseDraggedObservable: Observable<MouseDraggedEvt> = SimpleObservable<MouseDraggedEvt>(null)

    val selectedRegionText: Observable<String> = mouseDraggedObservable.filter {
        it ?: return@filter false
        val img = imageToDrawObservable.value ?: return@filter false
        return@filter it.newPoint.isInside(img) && it.oldPoint.isInside(img)
    }.map(this::createTextForDrag)

    private fun createTextForDrag(it: MouseDraggedEvt?): String? {
        it ?: return null
        val minX = min(it.newPoint.x, it.oldPoint.x)
        val maxX = max(it.newPoint.x, it.oldPoint.x)
        val minY = min(it.newPoint.y, it.oldPoint.y)
        val maxY = max(it.newPoint.y, it.oldPoint.y)
        val writer = StringWriter()
        val img = imageToDrawObservable.value ?: return null
        val saveType = saveTypeObservable.value ?: return null
        img.write(saveType, writer, minX, minY, maxX, maxY)
        return writer.toString()
    }

    val currentRGB: Observable<Color> = SimpleObservable(currentPositionObservable).map {
        it ?: return@map null
        val image = imageToDrawObservable.value ?: return@map null
        if (!it.isInside(image)) return@map null
        return@map Color(image.getRGB(it.x, it.y))
    }

    val currentHSV: Observable<HSV> = SimpleObservable(currentRGB).map { (it ?: return@map null).toHsv() }
    val currentLAB: Observable<LAB> = SimpleObservable(currentRGB).map { (it ?: return@map null).toLab() }

    val saveTypeObservable: Observable<FormatType> = SimpleObservable(FormatType.RGB)

    // End pixel/area related data

    init {
        println("recompute in change listeners")
        imageToDrawObservable.recomputeOnChange(observables = *arrayOf(filters.filterConfigurationObservable, imageObservable)) {
            val img = imageObservable.value ?: return@recomputeOnChange null
            transformImage(img)
        }
    }

    private fun <T> Observable<T>.recomputeOnChange(vararg observables: Observable<*>, recomputeAction: () -> T?) {
        for (observable in observables) {
            observable.subscribe {
                value = recomputeAction()
            }
        }
    }


    // Why is this needed?
    val repaintAllObservable: Observable<RefreshAllEvent> = SimpleObservable<RefreshAllEvent>(null)

    fun repaintAll() {
        repaintAllObservable.value = RefreshAllEvent
    }


}