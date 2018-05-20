package sirgl.graphics.segmentation.norrm.slice

import sirgl.graphics.filter.FilterModel
import sirgl.graphics.filter.ImageFilter
import sirgl.graphics.filter.Presentable
import sirgl.graphics.segmentation.meanshift.MeanShift2Filter
import sirgl.graphics.ui.InvisiblePanel
import javax.swing.JPanel

class NormalizedCutFilterModel(val presentable: Presentable) : FilterModel, Presentable by presentable  {
    override val filter: ImageFilter = MeanShift2Filter({ Thread.sleep((it.height * it.width * 2).toLong()) })
    override val panel: JPanel = InvisiblePanel()
}