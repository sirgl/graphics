package sirgl.graphics.filter.grayscale

import sirgl.graphics.filter.FilterModel
import sirgl.graphics.filter.Presentable
import javax.swing.JPanel

class GrayscaleFilterModel(presentable: Presentable) : FilterModel, Presentable by presentable {
    override val filter = GrayscaleFilter()
    override val panel = JPanel()
}