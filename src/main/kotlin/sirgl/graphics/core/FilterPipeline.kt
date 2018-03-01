package sirgl.graphics.core

import sirgl.graphics.filter.FilterModel
import sirgl.graphics.observable.Observable
import java.awt.image.BufferedImage


class FilterPipeline(
        private val filtersObservable: Observable<MutableList<FilterModel>>
) {
    /**
     * src and res have same size
     */
    fun transform(src: BufferedImage, res: BufferedImage) {
        var currentSrc = src
        val filterModels = filtersObservable.value ?: return
        var modified = false
        for (model in filterModels) {
            modified = true
            model.filter.transform(currentSrc, res)
            currentSrc = res
        }
        if (!modified) {
            copyPixels(src, res)
        }
    }
}

fun copyPixels(src: BufferedImage, target: BufferedImage) {
    val width = src.width
    val height = src.height
    for (y in (0 until height)) {
        for (x in (0 until width)) {
            target.setRGB(x, y, src.getRGB(x, y))
        }
    }
}