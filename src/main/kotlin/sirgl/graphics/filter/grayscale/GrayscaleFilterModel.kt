package sirgl.graphics.filter.grayscale

import sirgl.graphics.filter.FilterModel
import sirgl.graphics.filter.Presentable
import sirgl.graphics.ui.InvisiblePanel

class GrayscaleFilterModel(presentable: Presentable) : FilterModel, Presentable by presentable {
    override val filter = GrayscaleFilter()
    override val panel = InvisiblePanel()
}