package sirgl.graphics.filter.grayscale

import sirgl.graphics.core.Filters
import sirgl.graphics.filter.FilterModelFactory
import sirgl.graphics.filter.loadIcon
import javax.swing.Icon

class GrayscaleFilterModelFactory : FilterModelFactory<GrayscaleFilterModel> {
    override fun create(filters: Filters) = GrayscaleFilterModel(this)

    override val icon: Icon? = loadIcon("grayscale.png")
    override val name = "Grayscale"

}