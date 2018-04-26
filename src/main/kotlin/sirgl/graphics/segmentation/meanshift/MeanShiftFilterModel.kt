package sirgl.graphics.segmentation.meanshift

import sirgl.graphics.filter.FilterModel
import sirgl.graphics.filter.ImageFilter
import sirgl.graphics.filter.Presentable
import sirgl.graphics.ui.InvisiblePanel
import javax.swing.JPanel

class MeanShiftFilterModel(val presentable: Presentable) : FilterModel, Presentable by presentable {
    override val filter: ImageFilter = MeanShift2()
    override val panel: JPanel = InvisiblePanel()
}