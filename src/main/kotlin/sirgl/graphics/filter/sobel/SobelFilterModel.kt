package sirgl.graphics.filter.sobel

import sirgl.graphics.filter.FilterModel
import sirgl.graphics.filter.Presentable
import sirgl.graphics.ui.InvisiblePanel

class SobelFilterModel(presentable: Presentable) : FilterModel, Presentable by presentable {
    override val filter = SobelFilter()
    override val panel = InvisiblePanel()
}