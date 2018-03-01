package sirgl.graphics.core

import sirgl.graphics.canvas.MouseDraggedEvt
import sirgl.graphics.canvas.Point
import sirgl.graphics.conversion.*
import sirgl.graphics.core.GistType.*
import sirgl.graphics.filter.FilterModel
import sirgl.graphics.gist.Gist
import sirgl.graphics.observable.Observable
import sirgl.graphics.observable.SimpleObservable
import sirgl.graphics.observable.filter
import sirgl.graphics.observable.map
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.StringWriter
import javax.swing.JPanel
import kotlin.math.max
import kotlin.math.min

object RefreshAllEvent

enum class GistType { L, A, B }

object RedrawRequestEvent

@Suppress("RemoveExplicitTypeArguments")
class App {
    var internalImage = BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)

    val filtersObservable: Observable<MutableList<FilterModel>> = SimpleObservable(mutableListOf())
    private val filterPipeline = FilterPipeline(filtersObservable)
    val selectedFilterPanel: Observable<JPanel> = SimpleObservable<JPanel>(null)


    val imageObservable: Observable<BufferedImage> = SimpleObservable<BufferedImage>(null)
    val imageToDrawObservable: Observable<BufferedImage> = SimpleObservable(imageObservable, this::transformImage)
    val imageChangeObservable: Observable<RedrawRequestEvent> = SimpleObservable(RedrawRequestEvent)

    fun addFilter(filterModel: FilterModel) {
        val filters = filtersObservable.value ?: mutableListOf()
        filters.add(filterModel)
        filtersObservable.value = filters
    }

    fun clearFilters() {
        filtersObservable.value = mutableListOf()
    }

    private fun transformImage(src: BufferedImage?): BufferedImage? {
        src ?: return null
        val width = src.width
        val height = src.height
        val resultImg = if (width != internalImage.width || height != internalImage.width) {
            BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        } else {
            internalImage
        }
        filterPipeline.transform(src, resultImg)
        return resultImg
    }

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
        if (!it.isInside(image)) return@map null
        return@map Color(image.getRGB(it.x, it.y))
    }

    val currentHSV: Observable<HSV> = SimpleObservable(currentRGB).map { (it ?: return@map null).toHsv() }
    val currentLAB: Observable<LAB> = SimpleObservable(currentRGB).map { (it ?: return@map null).toLab() }

    fun init() {
        imageToDrawObservable.recomputeOnChange(
                observables = *arrayOf(filtersObservable, imageChangeObservable),
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

    fun imageToDrawChanged() {
        imageChangeObservable.value = RedrawRequestEvent
    }

    private fun <T> Observable<T>.recomputeOnChange(vararg observables: Observable<*>, recomputeAction: () -> T?) {
        for (observable in observables) {
            observable.subscribe {
                value = recomputeAction()
            }
        }
    }

    // TODO here actually not needed to create float array every time, consumer function is enough
    private fun recomputeGist(img: BufferedImage): Gist? {
        val values = FloatArray(img.width * img.height)
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

// Parallelize:

//    class Strip(
//            val src: BufferedImage,
//            val startY: Int,
//            val endY: Int
//    ) {
//        fun transform() {
//
//        }
//    }
//
//    fun BufferedImage.split(count: Int): List<Strip> {
//        val stripSize = height / count
//        val strips = mutableListOf<Strip>()
//        var startY = 0
//        for (i in (0..(count - 2))) {
//            val endY = startY + stripSize
//            strips.add(Strip(this, startY, endY))
//            startY = endY + 1
//        }
//        strips.add(Strip(this, startY, height - 1))
//        return strips
//    }


}