package sirgl.graphics.filter.hsv

import sirgl.graphics.core.App
import sirgl.graphics.filter.FilterModel
import sirgl.graphics.filter.Presentable

class HSVFilterModel(app: App, presentable: Presentable) : FilterModel, Presentable by presentable {
    override val filter: HSVImageFilter = HSVImageFilter(app.hSliderPosition, app.sSliderPosition, app.vSliderPosition)
    override val panel = HSVFilterPanel()
}