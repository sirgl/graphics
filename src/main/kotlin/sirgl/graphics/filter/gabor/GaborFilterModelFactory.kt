package sirgl.graphics.filter.gabor

import sirgl.graphics.core.Filters
import sirgl.graphics.filter.FilterModelFactory
import sirgl.graphics.filter.loadIcon
import javax.swing.Icon

class GaborFilterModelFactory : FilterModelFactory<GaborFilterModel> {
    override fun create(filters: Filters) = GaborFilterModel(this, filters)

    override val icon: Icon = loadIcon("gabor.png")
    override val name = "Gabor"

}