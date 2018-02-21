package sirgl.graphics.core

import sirgl.graphics.filter.ImageFilter
import sirgl.graphics.observable.Observable
import java.awt.image.BufferedImage

class FilterPipeline (private val filtersObservable: Observable<MutableList<ImageFilter>>) {
    /**
     * src and res have same size
     */
    fun transform(src: BufferedImage, res: BufferedImage) {
        var currentSrc = src
        val filters = filtersObservable.value ?: return
        for (filter in filters) {
            filter.transform(currentSrc, res)
            currentSrc = res
        }
    }
}