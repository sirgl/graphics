package sirgl.graphics.filter.sobel

import sirgl.graphics.filter.FilterModel
import sirgl.graphics.filter.Presentable
import javax.swing.JPanel

class SobelFilterModel(presentable: Presentable) : FilterModel, Presentable by presentable {
    override val filter = SobelFilter()
    override val panel = JPanel()
}