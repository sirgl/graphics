package sirgl.graphics.filter.hsv

import sirgl.graphics.core.FFilters
import sirgl.graphics.filter.FilterModel
import sirgl.graphics.filter.Presentable
import sirgl.graphics.observable.SimpleObservable

class HSVFilterModel(filters: FFilters, presentable: Presentable) : FilterModel, Presentable by presentable {
    val h = SimpleObservable(50)
    val s = SimpleObservable(50)
    val v = SimpleObservable(50)

    override val filter: HSVImageFilter = HSVImageFilter(h, s, v)
    override val panel = HSVFilterPanel(h, s, v)

    init {
        for (observable in arrayOf(h, s, v)) {
            observable.subscribe {
                filters.filterConfigurationChanged()
            }
        }
    }
}