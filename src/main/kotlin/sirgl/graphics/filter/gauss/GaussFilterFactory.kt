package sirgl.graphics.filter.gauss

import sirgl.graphics.core.Filters
import sirgl.graphics.filter.FilterModelFactory
import sirgl.graphics.filter.loadIcon

class GaussFilterFactory : FilterModelFactory<GaussFilterModel> {
    override fun create(filters: Filters) = GaussFilterModel(this, filters)

    override val icon = loadIcon("gauss.png")
    override val name = "Gauss"

}