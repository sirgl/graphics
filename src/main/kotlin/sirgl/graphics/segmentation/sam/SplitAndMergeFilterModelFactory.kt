package sirgl.graphics.segmentation.sam

import sirgl.graphics.core.Filters
import sirgl.graphics.filter.FilterModelFactory
import sirgl.graphics.filter.loadIcon
import javax.swing.Icon

class SplitAndMergeFilterModelFactory : FilterModelFactory<SplitAndMergeFilterModel> {
    override fun create(filters: Filters): SplitAndMergeFilterModel {
        return SplitAndMergeFilterModel(this, filters)
    }

    override val icon: Icon = loadIcon("splitAndMerge.png")
    override val name = "Split and merge"
}