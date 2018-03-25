package sirgl.graphics.segmentation.sam

import sirgl.graphics.core.Filters
import sirgl.graphics.filter.FilterModel
import sirgl.graphics.filter.Presentable
import sirgl.graphics.observable.SimpleObservable
import sirgl.graphics.observable.transmitTo

class SplitAndMergeFilterModel(presentable: Presentable, filters: Filters) : FilterModel, Presentable by presentable {
    val thresholdObservable = SimpleObservable(5f)
    override val filter = SplitAndMergeFilter(thresholdObservable)
    override val panel = SplitAndMergeFilterPanel()
    init {
        panel.thresholdObservable.transmitTo(thresholdObservable)
        thresholdObservable.subscribe {
            filters.filterConfigurationChanged()
        }
    }

}