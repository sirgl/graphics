package sirgl.graphics.filter.sobel

import sirgl.graphics.core.Filters
import sirgl.graphics.filter.FilterModelFactory
import sirgl.graphics.filter.loadIcon

class SobelFilterModelFactory : FilterModelFactory<SobelFilterModel> {
    override fun create(filters: Filters) = SobelFilterModel(this)
    override val name = "Sobel"
    override val icon = loadIcon("sobel.png")
}