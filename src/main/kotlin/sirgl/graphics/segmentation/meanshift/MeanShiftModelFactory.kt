package sirgl.graphics.segmentation.meanshift

import sirgl.graphics.core.Filters
import sirgl.graphics.filter.FilterModelFactory
import sirgl.graphics.filter.loadIcon
import javax.swing.Icon

class MeanShiftModelFactory : FilterModelFactory<MeanShiftFilterModel> {
    override fun create(filters: Filters) = MeanShiftFilterModel(this)

    override val icon: Icon = loadIcon("meanShift.png")
    override val name: String = "Mean shift"
}