package sirgl.graphics.segmentation.norrm.slice

import sirgl.graphics.core.Filters
import sirgl.graphics.filter.FilterModelFactory
import sirgl.graphics.filter.loadIcon
import javax.swing.Icon

class NormalizedCutFilterModelFactory : FilterModelFactory<NormalizedCutFilterModel> {
    override fun create(filters: Filters) = NormalizedCutFilterModel(this)

    override val icon: Icon = loadIcon("normCut.png")
    override val name: String = "Normalized cut"
}